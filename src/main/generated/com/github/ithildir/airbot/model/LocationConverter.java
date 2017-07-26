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
 * Converter for {@link com.github.ithildir.airbot.model.Location}.
 *
 * NOTE: This class has been automatically generated from the {@link com.github.ithildir.airbot.model.Location} original class using Vert.x codegen.
 */
public class LocationConverter {

  public static void fromJson(JsonObject json, Location obj) {
    if (json.getValue("country") instanceof String) {
      obj.setCountry((String)json.getValue("country"));
    }
    if (json.getValue("latitude") instanceof Number) {
      obj.setLatitude(((Number)json.getValue("latitude")).doubleValue());
    }
    if (json.getValue("longitude") instanceof Number) {
      obj.setLongitude(((Number)json.getValue("longitude")).doubleValue());
    }
  }

  public static void toJson(Location obj, JsonObject json) {
    if (obj.getCountry() != null) {
      json.put("country", obj.getCountry());
    }
    json.put("latitude", obj.getLatitude());
    json.put("longitude", obj.getLongitude());
  }
}