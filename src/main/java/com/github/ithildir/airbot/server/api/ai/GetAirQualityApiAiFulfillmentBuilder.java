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
import com.github.ithildir.airbot.util.AirBotUtil;
import com.github.ithildir.airbot.util.LanguageUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.vertx.core.Future;

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
		Map<String, MeasurementService> measurementServices) {

		_geoService = geoService;
		_measurementServices = measurementServices;
	}

	@Override
	public Future<Fulfillment> build(
		AIResponse aiResponse, JsonObject responseJsonObject) {

		Locale locale = new Locale(aiResponse.getLang());
		String query = _getLocationQuery(aiResponse);

		Future<Location> locationFuture = Future.future();

		_geoService.getLocationByQuery(query, locationFuture);

		Future<Measurement> measurementFuture = locationFuture.compose(
			location -> {
				Future<Measurement> future = Future.future();

				MeasurementService measurementService =
					_measurementServices.get(location.getCountry());

				if (measurementService == null) {
					measurementService = _measurementServices.get(null);
				}

				measurementService.getMeasurement(
					location.getLatitude(), location.getLongitude(), future);

				return future;
			});

		return measurementFuture.compose(
			measurement -> {
				return _getFulfillment(locale, query, measurement);
			});
	}

	@Override
	public String getAction() {
		return "GET_AIR_QUALITY";
	}

	private Future<Fulfillment> _getFulfillment(
		Locale locale, String location, Measurement measurement) {

		String speech;

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
				location, measurement.getAqi(), aqiLevel, time, mainPollutant);
		}
		else {
			speech = LanguageUtil.format(
				locale, "the-air-quality-measurement-for-x-is-not-available",
				location);
		}

		Fulfillment fulfillment = new Fulfillment();

		fulfillment.setSpeech(speech);

		return Future.succeededFuture(fulfillment);
	}

	private String _getLocationQuery(AIResponse aiResponse) {
		Result result = aiResponse.getResult();

		Map<String, JsonElement> parameters = result.getParameters();

		JsonObject jsonObject = _toJsonObject(parameters.get("location"));

		JsonElement jsonElement = jsonObject.get("business-name");

		if (jsonElement == null) {
			jsonElement = jsonObject.get("city");
		}

		return jsonElement.getAsString();
	}

	private JsonObject _toJsonObject(JsonElement jsonElement) {
		return jsonElement.getAsJsonObject();
	}

	private final GeoService _geoService;
	private final Map<String, MeasurementService> _measurementServices;

}