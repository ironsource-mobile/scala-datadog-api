package com.supersonic.datadog

import io.circe.Decoder

final case class TimeboardCreateResponse(title: String,
                                         id: Int,
                                         url: String,
                                         resource: String)

final case class TimeboardDescription(title: String,
                                      description: String,
                                      id: Int,
                                      resource: String)

final case class SimpleResponse(status: String)

final case class AllTimeboardsResponse(timeboards: List[TimeboardDescription])

final class BadStatusCode(code: Int) extends Exception(s"Bad status code $code")

final class MissingBody(reason: String) extends Exception(s"Missing response body: $reason")

sealed trait AddTimeboardIfMissingResponse
object AddTimeboardIfMissingResponse {
  case object AlreadyPresent extends AddTimeboardIfMissingResponse
  case class New(response: TimeboardCreateResponse) extends AddTimeboardIfMissingResponse
}

object TimeboardCreateResponse {
  implicit val decoder: Decoder[TimeboardCreateResponse] = Decoder.instance { c =>
    for {
      title <- c.downField("dash").downField("title").as[String]
      id <- c.downField("dash").downField("id").as[Int]
      url <- c.downField("url").as[String]
      resource <- c.downField("resource").as[String]
    } yield TimeboardCreateResponse(title = title, id = id, url = url, resource = resource)
  }
}

object TimeboardDescription {
  implicit val decoder: Decoder[TimeboardDescription] = Decoder.instance { c =>
    for {
      title <- c.downField("title").as[String]
      description <- c.downField("description").as[Option[String]]
      id <- c.downField("id").as[String]
      resource <- c.downField("resource").as[String]
    } yield TimeboardDescription(
      title = title,
      description = description.getOrElse(""),
      id = id.toInt,
      resource)
  }
}

object AllTimeboardsResponse {
  implicit val decoder: Decoder[AllTimeboardsResponse] = Decoder.instance { c =>
    c.downField("dashes").as[List[TimeboardDescription]].map(AllTimeboardsResponse.apply)
  }
}

object SimpleResponse {
  implicit val decoder: Decoder[SimpleResponse] = Decoder.instance { c =>
    for {
      status <- c.downField("status").as[String]
    } yield SimpleResponse(status = status)
  }
}
