package com.supersonic.datadog

import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.superonic.SttpSyntax._
import com.supersonic.datadog.TimeboardJSON._
import io.circe
import io.circe.{Decoder, Encoder}

/** A simple wrapper for HTTP requests against the Datadog API.
  *
  * @see https://docs.datadoghq.com/api
  */
final class DatadogClient[F[_]](httpBackend: SttpBackend[F, Nothing])
                               (apiKey: String,
                                appKey: String,
                                url: String = "https://app.datadoghq.com") {

  private implicit val monad: MonadError[F] = httpBackend.responseMonad
  private implicit val backend: SttpBackend[F, Nothing] = httpBackend

  /** Adds the given timeboard to Datadog. */
  def addTimeboard(timeboard: Timeboard): F[TimeboardCreateResponse] =
    post[Timeboard, TimeboardCreateResponse]("dash", timeboard)

  /** Fetches all the currently available timeboards. */
  def getAllTimeboards(): F[AllTimeboardsResponse] =
    get[AllTimeboardsResponse]("dash")

  /** Adds a timeboard to Datadog, but only if its not already present in the full timeboards list.
    * The existance check is done by using the timeboard's name (assuming that they are unique).
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

  private def parseResponse[A: Decoder](maybeResponse: F[Response[Either[circe.Error, A]]]): F[A] = {
    maybeResponse.flatMap { response =>
      if (response.code != 200) monad.error[Either[circe.Error, A]](new BadStatusCode(response.code))
      else response.body.fold(
        reason => monad.error[Either[circe.Error, A]](new MissingBody(reason)),
        s => monad.unit(s))

    }.flatMap(_.fold(monad.error[A](_), monad.unit))
  }

  def close(): Unit = httpBackend.close()
}


