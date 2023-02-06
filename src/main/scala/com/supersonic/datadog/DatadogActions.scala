package com.supersonic.datadog

import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.supersonic.SttpSyntax._
import com.supersonic.datadog.TimeboardJSON._
import io.circe
import io.circe.{Decoder, Encoder}

/** Basic actions against the Datadog API. */
trait DatadogActions[F[_]] {
  /** Adds the given timeboard to Datadog. */
  def addTimeboard(timeboard: Timeboard): F[TimeboardCreateResponse]

  /** Fetches all the currently available timeboards. */
  def getAllTimeboards(): F[AllTimeboardsResponse]

  def gauge(key: String, value: Long, tags: Map[String, String]): F[SimpleResponse]

}

/** Helper actions that can be derived derived from [[DatadogActions]]. */
class DatadogDerivedActions[F[_]](actions: DatadogActions[F])
                                 (implicit monad: MonadError[F]) {

  import actions._

  /** Adds a timeboard to Datadog, but only if its not already present in the full timeboards list.
    * The existence check is done by using the timeboard's name (assuming that they are unique).
    */
  def addTimeboardIfMissing(timeboard: Timeboard): F[AddTimeboardIfMissingResponse] = {
    for {
      timeboardsResponse <- getAllTimeboards()
      timeboards = timeboardsResponse.timeboards
      alreadyPresent = timeboards.exists(_.title == timeboard.title)
      response <-
        if (alreadyPresent)
          monad.unit(AddTimeboardIfMissingResponse.AlreadyPresent)
        else addTimeboard(timeboard).map(AddTimeboardIfMissingResponse.New)
    } yield response
  }
}

/** An STTP-based implementation of the Datadog actions. Communicates with the Datadog API.
  *
  * @see https://docs.datadoghq.com/api
  */
final class SttpDatadogActions[F[_]](httpBackend: SttpBackend[F, Nothing])
                                    (apiKey: String,
                                     appKey: String,
                                     url: String) extends DatadogActions[F] {
  private implicit val backend: SttpBackend[F, Nothing] = httpBackend

  private implicit val monad: MonadError[F] = httpBackend.responseMonad

  def addTimeboard(timeboard: Timeboard): F[TimeboardCreateResponse] =
    post[Timeboard, TimeboardCreateResponse]("dash", timeboard)

  def getAllTimeboards(): F[AllTimeboardsResponse] =
    get[AllTimeboardsResponse]("dash")

  def gauge(key: String, value: Long, tags: Map[String, String]): F[SimpleResponse] = {
    val now = System.currentTimeMillis / 1000
    val dataDogSeries =
      DataDogSeries(
        List(DataDogSingleSeries(
          metric = key,
          List(List(now, value)),
          `type` = "gauge",
          tags = tags)))

    post[DataDogSeries, SimpleResponse]("series", dataDogSeries)(DataDogSeriesJSON.dataDogSeries, SimpleResponse.decoder)
  }

  private def withCredentials(path: String) =
    uri"$url/api/v1/$path?api_key=$apiKey&application_key=$appKey"

  private def get[A: Decoder](path: String): F[A] =
    parseResponse {
      sttp
        .get(withCredentials(path))
        .response(asJson[A])
        .send()
    }

  private def post[A: Encoder, B: Decoder](path: String, payload: A): F[B] =
    parseResponse {
      sttp
        .post(withCredentials(path))
        .body(payload)
        .response(asJson[B])
        .send()
    }

  private def parseResponse[A: Decoder](maybeResponse: F[Response[Either[DeserializationError[circe.Error], A]]]): F[A] = {
    maybeResponse.flatMap { response =>
      if (!Set(200, 202).contains(response.code)) monad.error[Either[DeserializationError[circe.Error], A]](new BadStatusCode(response.code))
      else response.body.fold(
        reason => monad.error[Either[DeserializationError[circe.Error], A]](new MissingBody(reason)),
        s => monad.unit(s))
    }.flatMap(_.fold(deserializationError => monad.error[A](deserializationError.error), monad.unit))
  }
}