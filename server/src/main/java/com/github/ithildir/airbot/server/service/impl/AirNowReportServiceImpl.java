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

package com.github.ithildir.airbot.server.service.impl;

import com.github.ithildir.airbot.server.model.Report;
import com.github.ithildir.airbot.server.service.ReportService;
import com.github.ithildir.airbot.server.util.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.text.ParseException;
import java.text.ParsePosition;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * @author Andrea Di Giorgi
 */
public class AirNowReportServiceImpl
	extends BaseRecordServiceImpl implements ReportService {

	public AirNowReportServiceImpl(Vertx vertx, JsonObject configJsonObject) {
		super(vertx, configJsonObject);

		SharedData sharedData = vertx.sharedData();

		_areaForecastReports = sharedData.getLocalMap(
			AirNowReportServiceImpl.class.getName() + ".areaForecastReports");
		_areaObservationReports = sharedData.getLocalMap(
			AirNowReportServiceImpl.class.getName() +
				".areaObservationReports");
	}

	@Override
	public void getForecastReport(
		String area, int offsetDays, Handler<AsyncResult<JsonObject>> handler) {

		Report report = _areaForecastReports.get(
			_getObservationReportKey(area, offsetDays));

		if (report == null) {
			handler.handle(
				Future.failedFuture(
					"Unable to find forecast report of " + area));

			return;
		}

		handler.handle(Future.succeededFuture(JsonObject.mapFrom(report)));
	}

	@Override
	public void getObservationReport(
		String area, Handler<AsyncResult<JsonObject>> handler) {

		Report report = _areaObservationReports.get(area);

		if (report == null) {
			handler.handle(
				Future.failedFuture(
					"Unable to find observation report of " + area));

			return;
		}

		handler.handle(Future.succeededFuture(JsonObject.mapFrom(report)));
	}

	protected static Date getValidDate(
			String validDateString, String validTimeString, String timeZoneId)
		throws ParseException {

		StringBuilder sb = new StringBuilder();

		sb.append(validDateString);
		sb.append(' ');

		if (StringUtils.isNotBlank(validTimeString)) {
			sb.append(validTimeString);
		}
		else {
			sb.append("00:00");
		}

		sb.append(' ');
		sb.append(timeZoneId);

		ParsePosition parsePosition = new ParsePosition(0);

		Date date = _validDateFastDateFormat.parse(
			sb.toString(), parsePosition);

		if (parsePosition.getErrorIndex() >= 0) {
			throw new ParseException(
				"Unable to parse valid date " + sb,
				parsePosition.getErrorIndex());
		}

		return date;
	}

	@Override
	protected void init(String[] values) throws Exception {
		char dataType = Character.toUpperCase(CharUtils.toChar(values[5]));

		if ((dataType != 'F') && (dataType != 'O')) {
			return;
		}

		boolean primary = BooleanUtils.toBoolean(values[6]);

		if (!primary) {
			return;
		}

		String area = values[7];

		String validDateString = values[1];
		String validTimeString = values[2];
		String timeZoneId = values[3];
		String aqiString = values[12];
		String aqiCategory = values[13];
		String mainPollutant = values[11];
		boolean actionDay = BooleanUtils.toBoolean(values[14]);
		String discussion = values[15];

		int aqi = -1;

		if (StringUtils.isNotBlank(aqiString)) {
			aqi = Integer.parseInt(aqiString);
		}

		Date validDate = getValidDate(
			validDateString, validTimeString, timeZoneId);

		Report report = new Report(
			aqi, aqiCategory, mainPollutant, validDate, timeZoneId, actionDay,
			discussion);

		if (dataType == 'F') {
			int offsetDays = Integer.parseInt(values[4]);

			_areaForecastReports.put(
				_getObservationReportKey(area, offsetDays), report);
		}
		else {
			_areaObservationReports.put(area, report);
		}
	}

	private String _getObservationReportKey(String area, int offsetDays) {
		return area + offsetDays;
	}

	private static final FastDateFormat _validDateFastDateFormat =
		FastDateFormat.getInstance("MM/dd/yy H:mm z", Locale.US);

	private final LocalMap<String, Report> _areaForecastReports;
	private final LocalMap<String, Report> _areaObservationReports;

}