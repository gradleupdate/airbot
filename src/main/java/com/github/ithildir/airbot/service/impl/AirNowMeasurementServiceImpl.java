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

import com.github.ithildir.airbot.model.Location;
import com.github.ithildir.airbot.model.Measurement;
import com.github.ithildir.airbot.service.MeasurementService;
import com.github.ithildir.airbot.util.RecordParserWriteStream;

import io.netty.handler.codec.http.HttpResponseStatus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.serviceproxy.ServiceException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Andrea Di Giorgi
 */
public class AirNowMeasurementServiceImpl implements MeasurementService {

	public AirNowMeasurementServiceImpl(Vertx vertx) {
		WebClientOptions webClientOptions = new WebClientOptions();

		webClientOptions.setDefaultHost("files.airnowtech.org");
		webClientOptions.setDefaultPort(443);
		webClientOptions.setSsl(true);

		_webClient = WebClient.create(vertx, webClientOptions);
	}

	@Override
	public void getMeasurement(
		double latitude, double longitude,
		Handler<AsyncResult<Measurement>> handler) {

		Measurement measurement = null;

		String reportingArea = _getReportingArea(latitude, longitude);

		if (reportingArea != null) {
			measurement = _reportingAreaMeasurements.get(reportingArea);
		}

		handler.handle(Future.succeededFuture(measurement));
	}

	@Override
	public void init(Handler<AsyncResult<Void>> handler) {
		if (StringUtils.isBlank(_reportingAreaETag)) {
			_initReportingAreaRecords(handler);

			return;
		}

		HttpRequest<?> httpRequest = _webClient.head(_REPORTING_AREA_URI);

		httpRequest = httpRequest.as(BodyCodec.none());

		httpRequest.send(
			asyncResult -> {
				HttpResponse<?> httpResponse = _handleHttpResponse(
					asyncResult, handler);

				if (httpResponse == null) {
					return;
				}

				String etag = httpResponse.getHeader(
					HttpHeaders.ETAG.toString());

				if (_reportingAreaETag.equals(etag)) {
					if (_logger.isDebugEnabled()) {
						_logger.debug(
							"Reporting area records are already up-to-date " +
								"with ETag {0}",
							etag);
					}

					return;
				}

				_initReportingAreaRecords(handler);
			});
	}

	private String _getReportingArea(double latitude, double longitude) {
		Location coordinates = new Location(latitude, longitude, "US");

		double distance = Double.MAX_VALUE;
		String reportingArea = null;

		for (Map.Entry<String, Location> entry :
				_reportingAreaCoordinates.entrySet()) {

			Location curCoordinates = entry.getValue();

			double curDistance = coordinates.getDistance(curCoordinates);

			if ((reportingArea == null) || (curDistance < distance)) {
				distance = curDistance;
				reportingArea = entry.getKey();
			}
		}

		return reportingArea;
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
			handler.handle(
				ServiceException.fail(
					statusCode, httpResponse.statusMessage()));

			return null;
		}

		return httpResponse;
	}

	private void _initReportingAreaRecord(Buffer buffer) {
		String[] values = StringUtils.split(buffer.toString(), '|');

		char dataType = CharUtils.toChar(values[5]);

		if (dataType != 'O') {
			return;
		}

		boolean primary = BooleanUtils.toBoolean(values[6]);

		if (!primary) {
			return;
		}

		String stateCode = values[8];

		if (StringUtils.isBlank(stateCode)) {
			return;
		}

		long time = _parseTime(values[1], values[2], values[3]);
		String reportingArea = values[7];
		double latitude = Double.parseDouble(values[9]);
		double longitude = Double.parseDouble(values[10]);
		String mainPollutant = _parsePollutant(values[11]);
		int aqi = Integer.parseInt(values[12]);
		String comments = values[15];

		Location coordinates = new Location(latitude, longitude, "US");

		_reportingAreaCoordinates.put(reportingArea, coordinates);

		Measurement measurement = new Measurement(
			reportingArea, time, aqi, mainPollutant, Collections.emptyMap(),
			comments);

		_reportingAreaMeasurements.put(reportingArea, measurement);
	}

	private void _initReportingAreaRecords(Handler<AsyncResult<Void>> handler) {
		HttpRequest<?> httpRequest = _webClient.get(_REPORTING_AREA_URI);

		RecordParser recordParser = RecordParser.newDelimited(
			"\n", this::_initReportingAreaRecord);

		httpRequest = httpRequest.as(
			BodyCodec.pipe(new RecordParserWriteStream(recordParser)));

		httpRequest.send(
			asyncResult -> {
				HttpResponse<?> httpResponse = _handleHttpResponse(
					asyncResult, handler);

				if (httpResponse == null) {
					return;
				}

				_reportingAreaETag = httpResponse.getHeader(
					HttpHeaders.ETAG.toString());

				if (_logger.isDebugEnabled()) {
					_logger.debug(
						"Reporting area records updated with ETag {0}",
						_reportingAreaETag);
				}

				handler.handle(Future.succeededFuture());
			});
	}

	private String _parsePollutant(String pollutant) {
		if (pollutant.equalsIgnoreCase("ozone")) {
			pollutant = "o3";
		}
		else if (pollutant.equalsIgnoreCase("pm2.5")) {
			pollutant = "pm25";
		}
		else {
			pollutant = pollutant.toLowerCase();
		}

		return pollutant;
	}

	private long _parseTime(
		String dateString, String timeString, String timeZoneString) {

		int year = Integer.parseInt(dateString.substring(6)) + 2000;
		int month = Integer.parseInt(dateString.substring(0, 2));
		int dayOfMonth = Integer.parseInt(dateString.substring(3, 5));
		int hour = Integer.parseInt(
			timeString.substring(0, timeString.length() - 3));
		int minute = Integer.parseInt(
			timeString.substring(timeString.length() - 2));

		ZoneId zoneId = ZoneId.of(timeZoneString, _shortZoneIds);

		ZonedDateTime zonedDateTime = ZonedDateTime.of(
			year, month, dayOfMonth, hour, minute, 0, 0, zoneId);

		Instant instant = zonedDateTime.toInstant();

		return instant.toEpochMilli();
	}

	private static final String _REPORTING_AREA_URI =
		"/airnow/today/reportingarea.dat";

	private static Logger _logger = LoggerFactory.getLogger(
		AirNowMeasurementServiceImpl.class);

	private static final Map<String, String> _shortZoneIds;

	static {
		_shortZoneIds = new HashMap<>(ZoneId.SHORT_IDS);

		_shortZoneIds.put("ADT", "US/Alaska");
		_shortZoneIds.put("CDT", "America/Chicago");
		_shortZoneIds.put("COT", "America/Bogota");
		_shortZoneIds.put("EDT", "America/New_York");
		_shortZoneIds.put("MDT", "America/Denver");
		_shortZoneIds.put("PDT", "America/Los_Angeles");
	}

	private final Map<String, Location> _reportingAreaCoordinates =
		new HashMap<>();
	private String _reportingAreaETag;
	private final Map<String, Measurement> _reportingAreaMeasurements =
		new HashMap<>();
	private final WebClient _webClient;

}