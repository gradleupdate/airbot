/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.github.ithildir.airbot.model;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link com.github.ithildir.airbot.model.Measurement}.
 *
 * NOTE: This class has been automatically generated from the {@link com.github.ithildir.airbot.model.Measurement} original class using Vert.x codegen.
 */
public class MeasurementConverter {

  public static void fromJson(JsonObject json, Measurement obj) {
    if (json.getValue("aqi") instanceof Number) {
      obj.setAqi(((Number)json.getValue("aqi")).intValue());
    }
    if (json.getValue("city") instanceof String) {
      obj.setCity((String)json.getValue("city"));
    }
    if (json.getValue("comments") instanceof String) {
      obj.setComments((String)json.getValue("comments"));
    }
    if (json.getValue("mainPollutant") instanceof String) {
      obj.setMainPollutant((String)json.getValue("mainPollutant"));
    }
    if (json.getValue("time") instanceof Number) {
      obj.setTime(((Number)json.getValue("time")).longValue());
    }
    if (json.getValue("values") instanceof JsonObject) {
      java.util.Map<String, java.lang.Double> map = new java.util.LinkedHashMap<>();
      json.getJsonObject("values").forEach(entry -> {
        if (entry.getValue() instanceof Number)
          map.put(entry.getKey(), ((Number)entry.getValue()).doubleValue());
      });
      obj.setValues(map);
    }
  }

  public static void toJson(Measurement obj, JsonObject json) {
    json.put("aqi", obj.getAqi());
    if (obj.getCity() != null) {
      json.put("city", obj.getCity());
    }
    if (obj.getComments() != null) {
      json.put("comments", obj.getComments());
    }
    if (obj.getMainPollutant() != null) {
      json.put("mainPollutant", obj.getMainPollutant());
    }
    json.put("time", obj.getTime());
    if (obj.getValues() != null) {
      JsonObject map = new JsonObject();
      obj.getValues().forEach((key,value) -> map.put(key, value));
      json.put("values", map);
    }
  }
}