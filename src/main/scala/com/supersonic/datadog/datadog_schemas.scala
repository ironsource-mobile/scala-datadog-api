package com.supersonic.datadog

import com.supersonic.datadog.Graph.AggregationMethod.{Average, Max, Min, Sum}
import com.supersonic.datadog.Graph.CountModifier.{Count, Rate}
import com.supersonic.datadog.Graph.EventOverlay._
import com.supersonic.datadog.Graph.Scope.{All, FromTemplateVariable, Tag}
import com.supersonic.datadog.Graph.Series.{CompoundSeries, Constant, SimpleSeries}

/** A description of a Datadog timeboard.
  * @see https://docs.datadoghq.com/api/#timeboards
  */
case class Timeboard(title: String,
                     description: String,
                     graphs: List[Graph],
                     templateVariables: List[TemplateVariable],
                     autoScale: Boolean = true)

case class Graph(title: String, definition: Graph.Definition)

case class TemplateVariable(name: String, prefix: String, default: Option[String])

object Graph {
  case class Definition(requests: List[Request],
                        visualization: Visualization,
                        yAxis: Option[YAxis] = None,
                        eventOverlay: Option[EventOverlay] = None)

  case class Request(series: List[Series],
                     style: Option[Style] = None,
                     visualizationType: Option[VisualizationType] = None)

  sealed trait Series {
    def render: String = this match {
      case SimpleSeries(metric, aggregationMethod, function, groups, countModifier) =>
        val renderedMetric = metric.render

        val renderedAggregationMethod =
          aggregationMethod.map(_.render).map(agg => s"$agg:").getOrElse("")

        val renderedFunction = function.map(_.name)

        val renderedGroup =
          if (groups.isEmpty) ""
          else groups
            .map(_.name)
            .mkString(start = " by {", sep = ", ", end = "}")

        val renderedCountModifier = countModifier.map(_.render).map(c => s".as_$c()").getOrElse("")

        val rawMetric =
          s"$renderedAggregationMethod$renderedMetric$renderedGroup$renderedCountModifier"

        renderedFunction.map(f => s"$f($rawMetric)").getOrElse(rawMetric)

      case Constant(value) => value.toString
      case CompoundSeries(series1, series2, op) => s"${series1.render} ${op.name} ${series2.render}"
    }
  }

  object Series {
    case class SimpleSeries(metric: Metric,
                            aggregationMethod: Option[AggregationMethod],
                            function: Option[Function] = None,
                            groups: List[Group] = List.empty,
                            countModifier: Option[CountModifier] = None) extends Series
    case class Constant(value: Double) extends Series
    case class CompoundSeries(series1: Series, series2: Series, op: Op) extends Series

    case class Op(name: String)
  }

  sealed trait VisualizationType

  object VisualizationType {
    case object Lines extends VisualizationType
    case object Areas extends VisualizationType
    case object Bars extends VisualizationType
  }

  case class Style(palette: Palette,
                   lineType: LineType,
                   lineWidth: LineWidth)

  sealed trait Palette
  object Palette {
    case object Classic extends Palette
    case object Cool extends Palette
    case object Warm extends Palette
    case object Purple extends Palette
    case object Orange extends Palette
    case object Grey extends Palette
  }

  sealed trait LineType
  object LineType {
    case object Solid extends LineType
    case object Dashed extends LineType
    case object Dotted extends LineType
  }

  sealed trait LineWidth
  object LineWidth {
    case object Normal extends LineWidth
    case object Thin extends LineWidth
    case object Thick extends LineWidth
  }


  sealed trait Visualization

  object Visualization {
    case object Timeseries extends Visualization
    case object QueryValue extends Visualization
    case object HeatMap extends Visualization
    case object Distribution extends Visualization
    case object TopList extends Visualization
    case object Change extends Visualization
    case object Hostmap extends Visualization
  }

  case class YAxis(min: Option[Double] = None,
                   max: Option[Double] = None,
                   scale: Option[YAxis.Scale] = None,
                   units: Option[Boolean] = None)

  object YAxis {
    sealed trait Scale
    object Scale {
      case object Linear extends Scale
      case object Log extends Scale
      case object SQRT extends Scale
      case class Pow(value: Double) extends Scale
    }
  }

  case class Metric(name: String, scopes: List[Scope]) {
    def render: String = {
      val renderedScopes = scopes.map(_.render).mkString("{", ", ", "}")

      s"$name$renderedScopes"
    }
  }

  case class Function(name: String)

  case class Group(name: String)

  sealed trait CountModifier {
    def render: String = this match {
      case Count => "count"
      case Rate => "rate"
    }
  }

  object CountModifier {
    case object Count extends CountModifier
    case object Rate extends CountModifier
  }

  sealed trait AggregationMethod {
    def render: String = this match {
      case Average => "avg"
      case Max => "max"
      case Min => "min"
      case Sum => "sum"
    }
  }

  object AggregationMethod {
    case object Average extends AggregationMethod
    case object Max extends AggregationMethod
    case object Min extends AggregationMethod
    case object Sum extends AggregationMethod
  }

  sealed trait Scope {
    def render: String = this match {
      case Tag(name, value) =>
        val renderedValue = value.map(v => s":$v").getOrElse("")
        s"$name$renderedValue"
      case FromTemplateVariable(value) => "$" + value.name
      case All => "*"
    }
  }

  object Scope {
    case class Tag(name: String, value: Option[String]) extends Scope
    case class FromTemplateVariable(value: TemplateVariable) extends Scope
    case object All extends Scope

    def apply(templateVariable: TemplateVariable): Scope = FromTemplateVariable(templateVariable)

    def apply(tag: String): Scope = Tag(tag, value = None)

    def apply(name: String, value: String): Scope = Tag(name, Some(value))

    def service(serviceName: String): Scope = Tag(name = "service", value = Some(serviceName))
  }


  case class EventOverlay(name: Option[EventName],
                          tag: Option[EventTag],
                          sources: Option[EventSources],
                          templateVariable: Option[EventFromTemplateVariable]) {

    def render: String = {
      val renderedName = name.map(_.eventName)
      val renderedSource = sources.map(_.source).map(str => s"sources:$str")
      val renderedTemplateVariable = templateVariable.map(_.value.name)
        .map(str => s"$$$str")
      val renderedTag =
        for {
          tagName <- tag.map(_.name)
          tagValue <- tag.flatMap(_.value)
        } yield s"$tagName:$tagValue"

      List(renderedName, renderedTag, renderedSource, renderedTemplateVariable).flatten.mkString(" ")
    }
  }

  object EventOverlay {
    case class EventTag(name: String, value: Option[String])
    case class EventSources(source: String)
    case class EventFromTemplateVariable(value: TemplateVariable)
    case class EventName(eventName: String)
  }

}
