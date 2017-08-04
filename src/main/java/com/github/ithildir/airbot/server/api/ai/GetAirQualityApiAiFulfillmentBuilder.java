/**
 * Copyright (c) 2017 Andrea Di Giorgi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.ithildir.airbot.server.api.ai;

import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import ai.api.model.Result;

import com.github.ithildir.airbot.model.Location;
import com.github.ithildir.airbot.model.Measurement;
import com.github.ithildir.airbot.service.GeoService;
import com.github.ithildir.airbot.service.MeasurementService;
import com.github.ithildir.airbot.service.UserService;
import com.github.ithildir.airbot.util.AirBotUtil;
import com.github.ithildir.airbot.util.LanguageUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.ocpsoft.prettytime.PrettyTime;

/**
 * @author Andrea Di Giorgi
 */
public class GetAirQualityApiAiFulfillmentBuilder
	implements ApiAiFulfillmentBuilder {

	public GetAirQualityApiAiFulfillmentBuilder(
		GeoService geoService,
		Map<String, MeasurementService> measurementServices,
		UserService userService) {

		_geoService = geoService;
		_measurementServices = measurementServices;
		_userService = userService;
	}

	@Override
	public Future<Fulfillment> build(
		AIResponse aiResponse, JsonObject responseJsonObject) {

		Locale locale = new Locale(aiResponse.getLang());
		String query = _getQuery(aiResponse);

		if (query != null) {
			return _buildQueryFulfillment(locale, query);
		}
		else {
			return _buildUserFulfillment(
				locale, aiResponse, responseJsonObject);
		}
	}

	@Override
	public String getAction() {
		return "GET_AIR_QUALITY";
	}

	private Future<Fulfillment> _buildFulfillment(
		Locale locale, String query, Location location) {

		MeasurementService measurementService = _measurementServices.get(
			location.getCountry());

		if (measurementService == null) {
			measurementService = _measurementServices.get(null);
		}

		Future<Measurement> measurementFuture = Future.future();

		measurementService.getMeasurement(
			location.getLatitude(), location.getLongitude(), measurementFuture);

		return measurementFuture.compose(
			measurement -> {
				Fulfillment fulfillment = _buildFulfillment(
					locale, query, measurement);

				return Future.succeededFuture(fulfillment);
			});
	}

	private Fulfillment _buildFulfillment(
		Locale locale, String city, Measurement measurement) {

		String speech;

		if (city == null) {
			city = measurement.getCity();
		}

		if (measurement != null) {
			String aqiLevel = AirBotUtil.getAQILevel(
				measurement.getAqi(), locale);
			String mainPollutant = LanguageUtil.get(
				locale, "pollutant-" + measurement.getMainPollutant());

			PrettyTime prettyTime = new PrettyTime(locale);

			String time = prettyTime.format(new Date(measurement.getTime()));

			speech = LanguageUtil.format(
				locale,
				"the-air-quality-index-in-x-was-x-x-x-with-x-as-main-pollutant",
				city, measurement.getAqi(), aqiLevel, time, mainPollutant);
		}
		else {
			speech = LanguageUtil.format(
				locale, "the-air-quality-measurement-for-x-is-not-available",
				city);
		}

		Fulfillment fulfillment = new Fulfillment();

		fulfillment.setSpeech(speech);

		return fulfillment;
	}

	private Fulfillment _buildGooglePermissionFulfillment(
		Locale locale, String key, String permission) {

		Fulfillment fulfillment = new Fulfillment();

		JsonObject dataJsonObject = new JsonObject();

		dataJsonObject.addProperty(
			"@type",
			"type.googleapis.com/google.actions.v2.PermissionValueSpec");
		dataJsonObject.addProperty("optContext", LanguageUtil.get(locale, key));

		JsonArray permissionsJsonArray = new JsonArray(1);

		permissionsJsonArray.add(permission);

		dataJsonObject.add("permissions", permissionsJsonArray);

		JsonObject systemIntentJsonObject = new JsonObject();

		systemIntentJsonObject.addProperty(
			"intent", "actions.intent.PERMISSION");
		systemIntentJsonObject.add("data", dataJsonObject);

		JsonObject googleJsonObject = new JsonObject();

		googleJsonObject.addProperty("expectUserResponse", true);
		googleJsonObject.add("systemIntent", systemIntentJsonObject);

		fulfillment.setData(
			Collections.singletonMap("google", googleJsonObject));

		fulfillment.setSpeech("Speechless");

		return fulfillment;
	}

	private Future<Fulfillment> _buildQueryFulfillment(
		Locale locale, String query) {

		Future<Location> locationFuture = Future.future();

		_geoService.getLocationByQuery(query, locationFuture);

		return locationFuture.compose(
			location -> {
				return _buildFulfillment(locale, query, location);
			});
	}

	private Future<Fulfillment> _buildUserFulfillment(
		Locale locale, AIResponse aiResponse, JsonObject responseJsonObject) {

		String userId = aiResponse.getSessionId();

		Future<Location> locationFuture = _getResponseLocation(
			responseJsonObject);

		locationFuture = locationFuture.compose(
			location -> {
				if (location != null) {
					return Future.succeededFuture(location);
				}

				Future<Location> future = Future.future();

				_userService.getUserLocation(userId, future);

				return future;
			});

		return locationFuture.compose(
			location -> {
				if (location != null) {
					_userService.updateUserLocation(
						userId, location.getLatitude(), location.getLongitude(),
						location.getCountry(),
						asyncResult -> {
							if (asyncResult.failed()) {
								_logger.error(
									"Unable to update user location",
									asyncResult.cause());
							}
						});

					return _buildFulfillment(locale, null, location);
				}

				Fulfillment fulfillment = _buildGooglePermissionFulfillment(
					locale,
					"in-order-to-get-information-about-the-air-quality-" +
						"around-you",
					"DEVICE_PRECISE_LOCATION");

				return Future.succeededFuture(fulfillment);
			});
	}

	private String _getQuery(AIResponse aiResponse) {
		Result result = aiResponse.getResult();

		Map<String, JsonElement> parameters = result.getParameters();

		JsonElement jsonElement = parameters.get("location");

		if ((jsonElement == null) || !jsonElement.isJsonObject()) {
			return null;
		}

		JsonObject jsonObject = jsonElement.getAsJsonObject();

		jsonElement = jsonObject.get("business-name");

		if (jsonElement == null) {
			jsonElement = jsonObject.get("city");
		}

		return jsonElement.getAsString();
	}

	private Future<Location> _getResponseLocation(
		JsonObject responseJsonObject) {

		JsonObject originalRequestJsonObject =
			responseJsonObject.getAsJsonObject("originalRequest");

		JsonObject dataJsonObject = originalRequestJsonObject.getAsJsonObject(
			"data");

		JsonObject deviceJsonObject = dataJsonObject.getAsJsonObject("device");

		if (deviceJsonObject == null) {
			return Future.succeededFuture();
		}

		JsonObject locationJsonObject = deviceJsonObject.getAsJsonObject(
			"location");

		if (locationJsonObject == null) {
			return Future.succeededFuture();
		}

		JsonObject coordinatesJsonObject = locationJsonObject.getAsJsonObject(
			"coordinates");

		if (coordinatesJsonObject == null) {
			return Future.succeededFuture();
		}

		JsonElement latitudeJsonElement = coordinatesJsonObject.get("latitude");
		JsonElement longitudeJsonElement = coordinatesJsonObject.get(
			"longitude");

		Future<Location> future = Future.future();

		_geoService.getLocationByCoordinates(
			latitudeJsonElement.getAsDouble(),
			longitudeJsonElement.getAsDouble(), future);

		return future;
	}

	private static final Logger _logger = LoggerFactory.getLogger(
		GetAirQualityApiAiFulfillmentBuilder.class);

	private final GeoService _geoService;
	private final Map<String, MeasurementService> _measurementServices;
	private final UserService _userService;

}