package com.supersonic.datadog

import com.supersonic.datadog.Graph.EventOverlay
import com.supersonic.datadog.Graph.EventOverlay.{EventFromTemplateVariable, EventName, EventSources, EventTag}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class DatadogSchemaTest extends AnyWordSpec with Matchers {

  val eventName: Option[EventName] = Some(EventName("event"))
  val eventTag: Option[EventTag] = Some(EventTag("tag-name", "tag-value"))
  val eventTemplate: Option[EventFromTemplateVariable] = Some(EventFromTemplateVariable(TemplateVariable("test", "", None)))
  val eventSources: Option[EventSources] = Some(EventSources("source"))

  "event overlay" should {
    "return the all event data" in {
      val event = EventOverlay(eventName, eventTag, eventSources, eventTemplate)
      val expectedOverlay = "event tags:tag-name:tag-value sources:source $test"
      event.render shouldBe expectedOverlay
    }

    "return the event name" in {
      val event = EventOverlay(eventName, None, None, None)
      val expectedOverlay = "event"
      event.render shouldBe expectedOverlay
    }

    "return the event tag" in {
      val event = EventOverlay(None, eventTag, None, None)
      val expectedOverlay = "tags:tag-name:tag-value"
      event.render shouldBe expectedOverlay
    }

    "return the event source" in {
      val event = EventOverlay(None, None, eventSources, None)
      val expectedOverlay = "sources:source"
      event.render shouldBe expectedOverlay
    }

    "return the event template variable" in {
      val event = EventOverlay(None, None, None, eventTemplate)
      val expectedOverlay = "$test"
      event.render shouldBe expectedOverlay
    }
  }
}
