package com.supersonic.datadog

import com.supersonic.datadog.Graph.EventOverlay._
import com.supersonic.datadog.Graph.Series.SimpleSeries
import com.supersonic.datadog.Graph._
import com.supersonic.datadog.TimeboardJSON._
import io.circe.syntax._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SeriesWithMetadataJSONTest extends AnyWordSpec with Matchers {
  val host: TemplateVariable = TemplateVariable(name = "host", prefix = "host", default = Some("*"))
  val env: TemplateVariable = TemplateVariable(name = "env", prefix = "environment", default = None)

  "Rendering series with metadata as JSON" should {
    "produce the correct JSON in the format expected by Datadog, including metadata JSON" in {
      val cpu =
        Graph(
          title = "CPU",
          definition =
            Graph.Definition(
              requests = List(
                Request(
                  series = List(
                    SeriesWithMetadata(SimpleSeries(
                      metric = Metric(
                        name = "system.cpu.user",
                        scopes = List(
                          Scope.service("test-service"),
                          Scope(host),
                          Scope(env))),
                      aggregationMethod = Some(AggregationMethod.Average),
                      groups = List(Group("host"))),
                      Some(Metadata(Some("numOfServices"))))),
                  visualizationType = Some(VisualizationType.Lines))),
              visualization = Visualization.Timeseries,
              eventOverlays = Some(List(
                EventOverlay(
                  Some(EventName("deploy-event")),
                  tag = Some(EventTag("a-tag", value = "some-value")),
                  sources = None,
                  templateVariable = None)))))

      val timeboard = Timeboard(
        title = s"Test timeboard",
        description = s"Some timeboard",
        graphs = List(cpu),
        templateVariables = List(host))

      timeboard.asJson shouldBe JSONTestUtil.seriesWithMetadataExpectedJSON
    }
  }

  "Rendering multiple series with metadata as JSON" should {
    "produce the correct JSON in the format expected by Datadog, including multiple metadata JSONs" in {
      val cpu =
        Graph(
          title = "CPU",
          definition =
            Graph.Definition(
              requests = List(
                Request(
                  series = List(
                    SeriesWithMetadata(SimpleSeries(
                      metric = Metric(
                        name = "system.cpu.user",
                        scopes = List(
                          Scope.service("test-service"),
                          Scope(host),
                          Scope(env))),
                      aggregationMethod = Some(AggregationMethod.Average),
                      groups = List(Group("host"))),
                      Some(Metadata(Some("first - numOfServices")))),
                    SeriesWithMetadata(SimpleSeries(
                      metric = Metric(
                        name = "aws.ec2.host_ok",
                        scopes = List(
                          Scope.service("test-service"),
                          Scope(host),
                          Scope(env))),
                      aggregationMethod = Some(AggregationMethod.Average),
                      groups = List(Group("host"))),
                      Some(Metadata(Some("second - numOfServices"))))),
                  visualizationType = Some(VisualizationType.Lines))),
              visualization = Visualization.Timeseries,
              eventOverlays = Some(List(
                EventOverlay(
                  Some(EventName("deploy-event")),
                  tag = Some(EventTag("a-tag", value = "some-value")),
                  sources = None,
                  templateVariable = None)))))

      val timeboard = Timeboard(
        title = s"Test timeboard",
        description = s"Some timeboard",
        graphs = List(cpu),
        templateVariables = List(host))

      timeboard.asJson shouldBe JSONTestUtil.multipleSeriesWithMetadataExpectedJSON
    }
  }

}
