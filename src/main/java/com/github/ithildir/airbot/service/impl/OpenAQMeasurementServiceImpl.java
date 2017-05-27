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

package com.github.ithildir.airbot.service.impl;

import com.github.ithildir.airbot.model.Measurement;
import com.github.ithildir.airbot.service.MeasurementService;
import com.github.ithildir.airbot.util.AirBotUtil;

import io.netty.handler.codec.http.HttpResponseStatus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.serviceproxy.ServiceException;

import java.time.Instant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Di Giorgi
 */
public class OpenAQMeasurementServiceImpl implements MeasurementService {

	public OpenAQMeasurementServiceImpl(Vertx vertx) {
		WebClientOptions webClientOptions = new WebClientOptions();

		webClientOptions.setDefaultHost("api.openaq.org");
		webClientOptions.setDefaultPort(443);
		webClientOptions.setSsl(true);

		_webClient = WebClient.create(vertx, webClientOptions);
	}

	@Override
	public void getMeasurement(
		double latitude, double longitude,
		Handler<AsyncResult<Measurement>> handler) {

		Future<String> locationFuture = _getLocation(latitude, longitude);

		Future<Measurement> measurementFuture = locationFuture.compose(
			this::_getMeasurement);

		measurementFuture.setHandler(
			asyncResult -> {
				if (asyncResult.failed()) {
					handler.handle(Future.failedFuture(asyncResult.cause()));

					return;
				}

				handler.handle(Future.succeededFuture(asyncResult.result()));
			});
	}

	private Future<String> _getLocation(double latitude, double longitude) {
		Future<String> future = Future.future();

		HttpRequest<Buffer> httpRequest = _webClient.get("/v1/locations");

		httpRequest.setQueryParam("coordinates", latitude + "," + longitude);
		httpRequest.setQueryParam("nearest", "1");

		httpRequest.send(
			asyncResult -> {
				HttpResponse<Buffer> httpResponse = _handleHttpResponse(
					future, asyncResult);

				if (httpResponse == null) {
					return;
				}

				String location = _getLocation(httpResponse.bodyAsJsonObject());

				future.complete(location);
			});

		return future;
	}

	private String _getLocation(JsonObject jsonObject) {
		JsonArray resultsJsonArray = jsonObject.getJsonArray("results");

		if (resultsJsonArray.isEmpty()) {
			return null;
		}

		JsonObject resultJsonObject = resultsJsonArray.getJsonObject(0);

		return resultJsonObject.getString("location");
	}

	private Measurement _getMeasurement(JsonObject jsonObject) {
		JsonArray resultsJsonArray = jsonObject.getJsonArray("results");

		if (resultsJsonArray.isEmpty()) {
			return null;
		}

		JsonObject resultJsonObject = resultsJsonArray.getJsonObject(0);

		String city = resultJsonObject.getString("city");
		JsonArray measurementsJsonArray = resultJsonObject.getJsonArray(
			"measurements");

		Instant instant = null;
		int aqi = -1;
		String mainPollutant = null;
		Map<String, Double> values = new HashMap<>();

		for (int i = 0; i < measurementsJsonArray.size(); i++) {
			JsonObject measurementJsonObject =
				measurementsJsonArray.getJsonObject(i);

			Instant parameterInstant = measurementJsonObject.getInstant(
				"lastUpdated");
			String parameter = measurementJsonObject.getString("parameter");
			String unit = measurementJsonObject.getString("unit");
			double value = measurementJsonObject.getDouble("value");

			if (unit.equals("ppm")) {
				value = value * 1000;
			}

			int parameterAqi = AirBotUtil.getAQI(parameter, value);

			if (parameterAqi > aqi) {
				aqi = parameterAqi;
				mainPollutant = parameter;
			}

			if ((instant == null) ||
				(parameterInstant.compareTo(instant) > 0)) {

				instant = parameterInstant;
			}

			values.put(parameter, value);
		}

		return new Measurement(
			city, instant.toEpochMilli(), aqi, mainPollutant, values);
	}

	private Future<Measurement> _getMeasurement(String location) {
		Future<Measurement> future = Future.future();

		HttpRequest<Buffer> httpRequest = _webClient.get("/v1/latest");

		httpRequest.setQueryParam("location", location);

		httpRequest.send(
			asyncResult -> {
				HttpResponse<Buffer> httpResponse = _handleHttpResponse(
					future, asyncResult);

				if (httpResponse == null) {
					return;
				}

				Measurement measurement = _getMeasurement(
					httpResponse.bodyAsJsonObject());

				future.complete(measurement);
			});

		return future;
	}

	private HttpResponse<Buffer> _handleHttpResponse(
		Future<?> future, AsyncResult<HttpResponse<Buffer>> asyncResult) {

		if (asyncResult.failed()) {
			future.fail(asyncResult.cause());

			return null;
		}

		HttpResponse<Buffer> httpResponse = asyncResult.result();

		int statusCode = httpResponse.statusCode();

		if (statusCode != HttpResponseStatus.OK.code()) {
			JsonObject jsonObject = httpResponse.bodyAsJsonObject();

			future.fail(
				new ServiceException(
					statusCode, jsonObject.getString("message")));

			return null;
		}

		return httpResponse;
	}

	private final WebClient _webClient;

}