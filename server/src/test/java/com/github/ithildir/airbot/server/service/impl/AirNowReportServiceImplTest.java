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

import com.github.ithildir.airbot.server.util.StringUtils;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.text.ParseException;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Andrea Di Giorgi
 */
@RunWith(VertxUnitRunner.class)
public class AirNowReportServiceImplTest
	extends BaseRecordServiceImplTestCase<AirNowReportServiceImpl> {

	@Test
	public void testGetForecastReport(TestContext testContext) {
		Async async = testContext.async(2);

		AirNowReportServiceImpl airNowReportServiceImpl =
			getRecordServiceImpl();

		airNowReportServiceImpl.getForecastReport(
			"Baton Rouge Area", 1,
			asyncResult -> {
				testContext.assertTrue(asyncResult.succeeded());

				JsonObject jsonObject = asyncResult.result();

				testContext.assertTrue(jsonObject.getBoolean("actionDay"));
				testContext.assertEquals(101, jsonObject.getInteger("aqi"));
				testContext.assertEquals(
					"OZONE", jsonObject.getString("mainPollutant"));
				testContext.assertEquals(
					"CDT", jsonObject.getString("timeZoneId"));
				testContext.assertEquals(
					1494738000000L, jsonObject.getLong("validDate"));
				testContext.assertNotNull(jsonObject.getString("discussion"));

				async.countDown();
			});

		airNowReportServiceImpl.getForecastReport(
			"Baton Rouge Area", 5,
			asyncResult -> {
				testContext.assertTrue(asyncResult.failed());

				async.countDown();
			});

		async.awaitSuccess();
	}

	@Test
	public void testGetObservationReport(TestContext testContext) {
		Async async = testContext.async(2);

		AirNowReportServiceImpl airNowReportServiceImpl =
			getRecordServiceImpl();

		airNowReportServiceImpl.getObservationReport(
			"Albuquerque",
			asyncResult -> {
				testContext.assertTrue(asyncResult.succeeded());

				JsonObject jsonObject = asyncResult.result();

				testContext.assertFalse(jsonObject.getBoolean("actionDay"));
				testContext.assertEquals(44, jsonObject.getInteger("aqi"));
				testContext.assertEquals(
					"OZONE", jsonObject.getString("mainPollutant"));
				testContext.assertEquals(
					"MDT", jsonObject.getString("timeZoneId"));
				testContext.assertEquals(
					1494777600000L, jsonObject.getLong("validDate"));
				testContext.assertTrue(
					StringUtils.isBlank(jsonObject.getString("discussion")));

				async.countDown();
			});

		airNowReportServiceImpl.getObservationReport(
			"foo",
			asyncResult -> {
				testContext.assertTrue(asyncResult.failed());

				async.countDown();
			});

		async.awaitSuccess();
	}

	@Test
	public void testGetValidDate(TestContext testContext)
		throws ParseException {

		_testGetValidDate(testContext, 1494633600000L, "05/13/17", null, "GMT");
		_testGetValidDate(testContext, 1494658800000L, "05/13/17", null, "PDT");
		_testGetValidDate(testContext, 1494633600000L, "05/13/17", "", "GMT");
		_testGetValidDate(testContext, 1494658800000L, "05/13/17", "", "PDT");
		_testGetValidDate(
			testContext, 1494644760000L, "05/13/17", "3:06", "GMT");
		_testGetValidDate(
			testContext, 1494669960000L, "05/13/17", "3:06", "PDT");
		_testGetValidDate(
			testContext, 1494644760000L, "05/13/17", "03:06", "GMT");
		_testGetValidDate(
			testContext, 1494669960000L, "05/13/17", "03:06", "PDT");
		_testGetValidDate(
			testContext, 1494711900000L, "05/13/17", "21:45", "GMT");
		_testGetValidDate(
			testContext, 1494737100000L, "05/13/17", "21:45", "PDT");
	}

	@Test(expected = ParseException.class)
	public void testGetValidDateError(TestContext testContext)
		throws ParseException {

		_testGetValidDate(
			testContext, 1494737100000L, "05/13/17", "21:4x", "PDT");
	}

	@Override
	protected JsonObject createConfigJsonObject(String url) {
		JsonObject configJsonObject = super.createConfigJsonObject(url);

		configJsonObject.put(
			AirNowReportServiceImpl.CONFIG_KEY_VALUE_DELIMITER_PATTERN, "\\|");

		return configJsonObject;
	}

	@Override
	protected AirNowReportServiceImpl createRecordServiceImpl(
		Vertx vertx, JsonObject configJsonObject) {

		return new AirNowReportServiceImpl(vertx, configJsonObject);
	}

	@Override
	protected String getFileName() {
		return "com/github/ithildir/airbot/server/service/impl/dependencies" +
			"/reportingarea.dat";
	}

	private void _testGetValidDate(
			TestContext testContext, long expectedTime, String dateString,
			String timeString, String timeZoneId)
		throws ParseException {

		Date date = AirNowReportServiceImpl.getValidDate(
			dateString, timeString, timeZoneId);

		testContext.assertEquals(expectedTime, date.getTime());
	}

}