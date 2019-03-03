package com.supersonic.datadog

import com.supersonic.datadog.Graph.LineType.{Dashed, Dotted, Solid}
import com.supersonic.datadog.Graph.LineWidth.{Normal, Thick, Thin}
import com.supersonic.datadog.Graph.Palette._
import com.supersonic.datadog.Graph.Visualization._
import com.supersonic.datadog.Graph.VisualizationType.{Areas, Bars, Lines}
import com.supersonic.datadog.Graph.YAxis.Scale.{Linear, Log, Pow, SQRT}
import com.supersonic.datadog.Graph._
import io.circe._
import io.circe.syntax._

object TimeboardJSON {
  implicit val timeboardEncoder: Encoder[Timeboard] = Encoder.instance { timeboard =>
    Json.obj(
      "title" := timeboard.title,
      "description" := timeboard.description,
      "graphs" := timeboard.graphs,
      "template_variables" := timeboard.templateVariables,
      "autoscale" := timeboard.autoScale)
  }

  implicit val graphEncoder: Encoder[Graph] = Encoder.instance { graph =>
    Json.obj(
      "title" := graph.title,
      "definition" := graph.definition)
  }

  implicit val templateVariableEncoder: Encoder[TemplateVariable] = Encoder.instance { variable =>
    Json.obj(
      "name" := variable.name,
      "prefix" := variable.prefix,
      "default" := variable.default)
  }

  implicit val graphDefinitionEncoder: Encoder[Graph.Definition] = Encoder.instance { definition =>
    Json.obj(
      "requests" := definition.requests,
      "vis" := definition.visualization,
      "yaxis" := definition.yAxis,
      "events" := definition.eventOverlays)
  }

  implicit val requestEncoder: Encoder[Request] = Encoder.instance { request =>
    val metadataList = request.series.flatMap { series =>
      series.metadata.map(data => (series.series.render, data))
    }

    metadataList match {
      case Nil => Json.obj(
        "q" := request.series.map(_.series.render).mkString(", "),
        "style" := request.style,
        "type" := request.visualizationType)
      case _ => Json.obj(
        "q" := request.series.map(_.series.render).mkString(", "),
        "style" := request.style,
        "type" := request.visualizationType,
        "metadata" := metadataList.toMap)
    }
  }

  implicit val seriesEncoder: Encoder[Graph.Series] = Encoder.instance { series =>
    Json.fromString(series.render)
  }

  implicit val metadataEncoder: Encoder[Metadata] = Encoder.instance { metadata =>
    Json.obj("alias" := metadata.alias)
  }

  implicit val styleEncoder: Encoder[Graph.Style] = Encoder.instance { style =>
    Json.obj(
      "palette" := style.palette,
      "type" := style.lineType,
      "width" := style.lineWidth)
  }

  implicit val paletteEncoder: Encoder[Graph.Palette] = Encoder.instance { palette =>
    val name = palette match {
      case Classic => "dog_classic"
      case Cool => "cool"
      case Warm => "warm"
      case Purple => "purple"
      case Orange => "orange"
      case Grey => "grey"
    }

    Json.fromString(name)
  }

  implicit val lineTypeEncoder: Encoder[Graph.LineType] = Encoder.instance { lineType =>
    val name = lineType match {
      case Solid => "solid"
      case Dashed => "dashed"
      case Dotted => "dotted"
    }

    Json.fromString(name)
  }

  implicit val lineWidthEncoder: Encoder[Graph.LineWidth] = Encoder.instance { lineWidth =>
    val name = lineWidth match {
      case Normal => "normal"
      case Thin => "thin"
      case Thick => "thick"
    }

    Json.fromString(name)
  }

  implicit val visualizationTypeEncoder: Encoder[VisualizationType] = Encoder.instance { visualizationType =>
    val name = visualizationType match {
      case Lines => "line"
      case Areas => "area"
      case Bars => "bars"
    }

    Json.fromString(name)
  }

  implicit val visualizationEncoder: Encoder[Visualization] = Encoder.instance { visualization =>
    val name = visualization match {
      case Timeseries => "timeseries"
      case QueryValue => "query_value"
      case HeatMap => "heatmap"
      case Distribution => "distribution"
      case TopList => "toplist"
      case Change => "change"
      case Hostmap => "hostmap"
    }

    Json.fromString(name)
  }

  implicit val yAxisEncoder: Encoder[YAxis] = Encoder.instance { yAxis =>
    Json.obj(
      "min" := yAxis.min,
      "max" := yAxis.max,
      "scale" := yAxis.scale,
      "units" := yAxis.units)
  }

  implicit val yAxisScaleEncoder: Encoder[YAxis.Scale] = Encoder.instance { yAxisScale =>
    val name = yAxisScale match {
      case Linear => "linear"
      case Log => "log"
      case SQRT => "sqrt"
      case Pow(value) => s"pow$value"
    }

    Json.fromString(name)
  }

  implicit def eventOverlayEncoder: Encoder[EventOverlay] = Encoder.instance { eventOverlay =>
    Json.obj("q" := eventOverlay.render)
  }
}
