package com.supersonic.datadog

import io.circe.parser.parse

object JSONTestUtil {

  val multipleSeriesWithMetadataExpectedJSON = parse {
    s"""
        {
          "autoscale": true,
          "title": "Test timeboard",
          "description": "Some timeboard",
          "graphs": [
            {
              "title": "CPU",
              "definition": {
                "requests": [
                  {
                    "q": "avg:system.cpu.user{service:test-service, $$host, $$env} by {host}, avg:aws.ec2.host_ok{service:test-service, $$host, $$env} by {host}",
                    "style": null,
                    "type": "line",
                    "metadata" : {
                      "avg:system.cpu.user{service:test-service, $$host, $$env} by {host}" : {
                        "alias" : "first - numOfServices"
                      },
                      "avg:aws.ec2.host_ok{service:test-service, $$host, $$env} by {host}" : {
                        "alias" : "second - numOfServices"
                      }
                    }
                  }
                ],
                "vis": "timeseries",
                "yaxis": null,
                "events": [
                  {
                    "q": "deploy-event tags:a-tag:some-value"
                  }
                ]
              }
            }
          ],
          "template_variables": [
            {
              "name": "host",
              "prefix": "host",
              "default": "*"
            }
          ],
          "autoscale" : true
        }
        """
  }.right.get // because it's a test and guaranteed to succeed

  val seriesWithMetadataExpectedJSON = parse {
    s"""
        {
          "autoscale": true,
          "title": "Test timeboard",
          "description": "Some timeboard",
          "graphs": [
            {
              "title": "CPU",
              "definition": {
                "requests": [
                  {
                    "q": "avg:system.cpu.user{service:test-service, $$host, $$env} by {host}",
                    "style": null,
                    "type": "line",
                    "metadata" : {
                      "avg:system.cpu.user{service:test-service, $$host, $$env} by {host}" : {
                        "alias" : "numOfServices"
                      }
                    }
                  }
                ],
                "vis": "timeseries",
                "yaxis": null,
                "events": [
                  {
                    "q": "deploy-event tags:a-tag:some-value"
                  }
                ]
              }
            }
          ],
          "template_variables": [
            {
              "name": "host",
              "prefix": "host",
              "default": "*"
            }
          ],
          "autoscale" : true
        }
        """
  }.right.get // because it's a test and guaranteed to succeed

  val timeboardExpectedJSONT = parse {
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
                    "q" : "deploy-event tags:a-tag:some-value"
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

}
