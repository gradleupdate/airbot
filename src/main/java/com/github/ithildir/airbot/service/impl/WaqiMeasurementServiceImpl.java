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

import io.netty.handler.codec.http.HttpResponseStatus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.serviceproxy.ServiceException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Andrea Di Giorgi
 */
public class WaqiMeasurementServiceImpl implements MeasurementService {

	public WaqiMeasurementServiceImpl(Vertx vertx, String key) {
		_key = Objects.requireNonNull(key);

		WebClientOptions webClientOptions = new WebClientOptions();

		webClientOptions.setDefaultHost("api.waqi.info");
		webClientOptions.setDefaultPort(443);
		webClientOptions.setSsl(true);

		_webClient = WebClient.create(vertx, webClientOptions);
	}

	@Override
	public void getMeasurement(
		double latitude, double longitude,
		Handler<AsyncResult<Measurement>> handler) {

		HttpRequest<Buffer> httpRequest = _webClient.get(
			"/feed/geo:" + latitude + ";" + longitude + "/");

		httpRequest.setQueryParam("token", _key);

		httpRequest.send(
			asyncResult -> {
				HttpResponse<Buffer> httpResponse = _handleHttpResponse(
					asyncResult, handler);

				if (httpResponse == null) {
					return;
				}

				Measurement measurement = _getMeasurement(
					httpResponse.bodyAsJsonObject());

				handler.handle(Future.succeededFuture(measurement));
			});
	}

	@Override
	public void init(Handler<AsyncResult<Void>> handler) {
		handler.handle(Future.succeededFuture());
	}

	private Measurement _getMeasurement(JsonObject jsonObject) {
		String status = jsonObject.getString("status");

		if (!"ok".equals(status)) {
			_logger.warn("Unable to use response {0}", jsonObject);

			return null;
		}

		JsonObject dataJsonObject = jsonObject.getJsonObject("data");

		JsonObject cityJsonObject = dataJsonObject.getJsonObject("city");

		String city = cityJsonObject.getString("name");

		JsonObject timeJsonObject = dataJsonObject.getJsonObject("time");

		String dateTime = timeJsonObject.getString("s");
		String dateTimeOffset = timeJsonObject.getString("tz");

		String date = dateTime.substring(0, 10);
		String time = dateTime.substring(11);

		TemporalAccessor temporalAccessor =
			DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(
				date + "T" + time + dateTimeOffset);

		Instant instant = Instant.from(temporalAccessor);

		int aqi = dataJsonObject.getInteger("aqi");
		String mainPollutant = dataJsonObject.getString("dominentpol");

		Map<String, Double> values = new HashMap<>();

		JsonObject valuesJsonObject = dataJsonObject.getJsonObject("iaqi");

		for (String pollutant : valuesJsonObject.fieldNames()) {
			JsonObject pollutantJsonObject = valuesJsonObject.getJsonObject(
				pollutant);

			double value = pollutantJsonObject.getDouble("v");

			values.put(pollutant, value);
		}

		return new Measurement(
			city, instant.toEpochMilli(), aqi, mainPollutant, values, null);
	}

	private <R, T> HttpResponse<T> _handleHttpResponse(
		AsyncResult<HttpResponse<T>> asyncResult,
		Handler<AsyncResult<R>> handler) {

		if (asyncResult.failed()) {
			handler.handle(Future.failedFuture(asyncResult.cause()));

			return null;
		}

		HttpResponse<T> httpResponse = asyncResult.result();

		int statusCode = httpResponse.statusCode();

		if (statusCode != HttpResponseStatus.OK.code()) {
			JsonObject jsonObject = httpResponse.bodyAsJsonObject();

			handler.handle(
				ServiceException.fail(
					statusCode, jsonObject.getString("message"), jsonObject));

			return null;
		}

		return httpResponse;
	}

	private static final Logger _logger = LoggerFactory.getLogger(
		WaqiMeasurementServiceImpl.class);

	private final String _key;
	private final WebClient _webClient;

}