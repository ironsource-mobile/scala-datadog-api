package com.supersonic.datadog

import com.supersonic.datadog.Graph.EventOverlay._
import com.supersonic.datadog.Graph.Series.SimpleSeries
import com.supersonic.datadog.Graph._
import com.supersonic.datadog.TimeboardJSON._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.{Matchers, WordSpec}

class TimeboardJSONTest extends WordSpec with Matchers {
  val host = TemplateVariable(name = "host", prefix = "host", default = Some("*"))
  val env = TemplateVariable(name = "env", prefix = "environment", default = None)

  "Rendering a timeboard as JSON" should {
    "produce the correct JSON in the format expected by Datadog" in {
      val cpu =
        Graph(
          title = "CPU",
          definition =
            Graph.Definition(
              requests = List(
                Request(
                  series = List(
                    SimpleSeries(
                      metric = Metric(
                        name = "system.cpu.user",
                        scopes = List(
                          Scope.service("test-service"),
                          Scope(host),
                          Scope(env))),
                      aggregationMethod = Some(AggregationMethod.Average),
                      groups = List(Group("host")))),
                  visualizationType = Some(VisualizationType.Lines))),
              visualization = Visualization.Timeseries,
              eventOverlays = Some(List(
                EventOverlay(
                  Some(EventName("deploy-event")),
                  tag = Some(EventTag("a-tag", value = "some-value")),
                  sources = None,
                  templateVariable = None)))))

      val messages = {
        def rate(style: Style, function: Option[Function]) =
          Request(
            series = List(
              SimpleSeries(
                metric = Metric(
                  name = "incoming.messages",
                  scopes = List(Scope(env))),
                aggregationMethod = Some(AggregationMethod.Sum),
                function = function,
                countModifier = Some(CountModifier.Rate))),
            visualizationType = Some(VisualizationType.Lines),
            style = Some(style))

        Graph(
          title = s"Incoming rate",
          definition =
            Graph.Definition(
              requests = List(
                rate(Style(Palette.Classic, LineType.Solid, LineWidth.Normal), function = None),
                rate(Style(Palette.Classic, LineType.Dotted, LineWidth.Thin), Some(Function("week_before")))),
              visualization = Visualization.Timeseries))
      }

      val timeboard = Timeboard(
        title = s"Test timeboard",
        description = s"Some timeboard",
        graphs = List(cpu, messages),
        templateVariables = List(host, env))

      val expected = parse {
        s"""
        {
          "title" : "Test timeboard",
          "description" : "Some timeboard",
          "graphs" : [
            {
              "title" : "CPU",
              "definition" : {
                "requests" : [
                  {
                    "q" : "avg:system.cpu.user{service:test-service, $$host, $$env} by {host}",
                    "style" : null,
                    "type" : "line"
                  }
                ],
                "vis" : "timeseries",
                "yaxis" : null,
                "events" : [
                  {
                    "q" : "deploy-event a-tag:some-value"
                  }
                ]
              }
            },
            {
              "title" : "Incoming rate",
              "definition" : {
                "requests" : [
                  {
                    "q" : "sum:incoming.messages{$$env}.as_rate()",
                    "style" : {
                      "palette" : "dog_classic",
                      "type" : "solid",
                      "width" : "normal"
                    },
                    "type" : "line"
                  },
                  {
                    "q" : "week_before(sum:incoming.messages{$$env}.as_rate())",
                    "style" : {
                      "palette" : "dog_classic",
                      "type" : "dotted",
                      "width" : "thin"
                    },
                    "type" : "line"
                  }
                ],
                "vis" : "timeseries",
                "yaxis" : null,
                "events" : null
              }
            }
          ],
          "template_variables" : [
            {
              "name" : "host",
              "prefix" : "host",
              "default" : "*"
            },
            {
              "name" : "env",
              "prefix" : "environment",
              "default" : null
            }
          ],
          "autoscale" : true
        }
        """
      }.right.get // because it's a test and guaranteed to succeed

      val rendered = timeboard.asJson

      rendered shouldBe expected
    }
  }
}
