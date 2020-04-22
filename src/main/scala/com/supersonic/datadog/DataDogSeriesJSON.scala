package com.supersonic.datadog

import io.circe.{Encoder, Json}
import io.circe.syntax._
object DataDogSeriesJSON {

  implicit val dataDogSeries: Encoder[DataDogSeries] = Encoder.instance { dataDogSeries =>
    Json.obj("series" := dataDogSeries.series)
  }

  implicit val dataDogSingleSeries: Encoder[DataDogSingleSeries] = Encoder.instance { dataDogSingleSeries =>
    Json.obj(
      "metric" := dataDogSingleSeries.metric,
      "points" := dataDogSingleSeries.points,
      "type" := dataDogSingleSeries.`type`,
      "tags" := dataDogSingleSeries.tags)
  }

}
