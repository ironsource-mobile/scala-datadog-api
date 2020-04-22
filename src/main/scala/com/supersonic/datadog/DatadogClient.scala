package com.supersonic.datadog

import com.softwaremill.sttp._

/** A simple wrapper for requests against the Datadog API.
  *
  * @see https://docs.datadoghq.com/api
  */
trait DatadogClient[F[_]] {
  /** @see [[DatadogActions.addTimeboard]] */
  def addTimeboard(timeboard: Timeboard): F[TimeboardCreateResponse]

  /** @see [[DatadogActions.getAllTimeboards]] */
  def getAllTimeboards(): F[AllTimeboardsResponse]

  /** @see [[DatadogDerivedActions.addTimeboardIfMissing]] */
  def addTimeboardIfMissing(timeboard: Timeboard): F[AddTimeboardIfMissingResponse]

  def gauge(key: String, value: Long, tags: Map[String, String] = Map.empty): F[SimpleResponse]

  /** Closes the client. */
  def close(): Unit
}

/** A [[DatadogClient]] implementations based on [[DatadogActions]]. */
abstract class ActionsBasedDatadogClient[F[_]](actions: DatadogActions[F])
                                              (implicit monad: MonadError[F]) extends DatadogClient[F] {
  private val derivedActions = new DatadogDerivedActions[F](actions)

  def addTimeboard(timeboard: Timeboard): F[TimeboardCreateResponse] =
    actions.addTimeboard(timeboard)

  def getAllTimeboards(): F[AllTimeboardsResponse] = actions.getAllTimeboards()

  def addTimeboardIfMissing(timeboard: Timeboard): F[AddTimeboardIfMissingResponse] =
    derivedActions.addTimeboardIfMissing(timeboard)

  def gauge(key: String, value: Long, tags: Map[String, String]): F[SimpleResponse] =
    actions.gauge(key, value, tags)
}

object DatadogClient {
  /** Creates an STTP-based Datadog client.
    *
    * @see [[SttpDatadogActions]]
    */
  def newSttpClient[F[_]](httpBackend: SttpBackend[F, Nothing])
                         (apiKey: String,
                          appKey: String,
                          url: String = "https://app.datadoghq.com"): DatadogClient[F] = {
    val actions = new SttpDatadogActions(httpBackend)(apiKey = apiKey, appKey = appKey, url = url)

    new ActionsBasedDatadogClient(actions)(httpBackend.responseMonad) {
      def close(): Unit = httpBackend.close()
    }
  }
}

