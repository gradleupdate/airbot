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

import com.github.ithildir.airbot.server.model.CityAndState;
import com.github.ithildir.airbot.server.service.GeoService;
import com.github.ithildir.airbot.server.util.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andrea Di Giorgi
 */
public class GeoServiceImpl
	extends BaseRecordServiceImpl implements GeoService {

	public GeoServiceImpl(Vertx vertx, JsonObject configJsonObject) {
		super(vertx, configJsonObject);

		SharedData sharedData = vertx.sharedData();

		_cityAndStateZipCodesMap = sharedData.getLocalMap(
			GeoServiceImpl.class.getName() + ".cityAndStateZipCodes");
		_zipCodeCityAndStatesMap = sharedData.getLocalMap(
			GeoServiceImpl.class.getName() + ".zipCodeCityAndStates");

		_dbValueDelimiterPattern = Objects.requireNonNull(
			configJsonObject.getString("dbValueDelimiterPattern"));
		_dbValueIndexCity = Objects.requireNonNull(
			configJsonObject.getInteger("dbValueIndexCity"));
		_dbValueIndexState = Objects.requireNonNull(
			configJsonObject.getInteger("dbValueIndexState"));
		_dbValueIndexZipCode = Objects.requireNonNull(
			configJsonObject.getInteger("dbValueIndexZipCode"));
	}

	@Override
	public void getState(String zipCode, Handler<AsyncResult<String>> handler) {
		CityAndState cityAndState = _zipCodeCityAndStatesMap.get(zipCode);

		if (cityAndState == null) {
			handler.handle(
				Future.failedFuture("Unable to find state of " + zipCode));

			return;
		}

		handler.handle(Future.succeededFuture(cityAndState.getState()));
	}

	@Override
	public void getZipCode(
		String city, String state, Handler<AsyncResult<String>> handler) {

		CityAndState cityAndState = new CityAndState(city, state);

		String zipCode = _cityAndStateZipCodesMap.get(cityAndState);

		if (StringUtils.isBlank(zipCode)) {
			handler.handle(
				Future.failedFuture(
					"Unable to find ZIP code of " + cityAndState));

			return;
		}

		handler.handle(Future.succeededFuture(zipCode));
	}

	@Override
	protected String getConfigKeyDelimiter() {
		return "dbLineDelimiter";
	}

	@Override
	protected String getConfigKeyUrl() {
		return "dbUrl";
	}

	@Override
	protected void init(Buffer buffer) {
		String line = buffer.toString();

		String[] values = line.split(_dbValueDelimiterPattern);

		String zipCode = StringUtils.unquote(values[_dbValueIndexZipCode]);

		Matcher matcher = _zipCodePattern.matcher(zipCode);

		if (!matcher.matches()) {
			if (_logger.isDebugEnabled()) {
				_logger.debug("Ignoring line {0}", line);
			}

			return;
		}

		String city = StringUtils.unquote(values[_dbValueIndexCity]);
		String state = StringUtils.unquote(values[_dbValueIndexState]);

		CityAndState cityAndState = new CityAndState(city, state);

		_cityAndStateZipCodesMap.put(cityAndState, zipCode);
		_zipCodeCityAndStatesMap.put(zipCode, cityAndState);
	}

	private static final Logger _logger = LoggerFactory.getLogger(
		GeoServiceImpl.class);

	private static final Pattern _zipCodePattern = Pattern.compile("\\d{5}");

	private final LocalMap<CityAndState, String> _cityAndStateZipCodesMap;
	private final String _dbValueDelimiterPattern;
	private final int _dbValueIndexCity;
	private final int _dbValueIndexState;
	private final int _dbValueIndexZipCode;
	private final LocalMap<String, CityAndState> _zipCodeCityAndStatesMap;

}