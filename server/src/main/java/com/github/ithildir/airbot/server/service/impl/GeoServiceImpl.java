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
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.util.Objects;

import org.apache.commons.lang3.CharUtils;

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

		_valueIndexCity = Objects.requireNonNull(
			configJsonObject.getInteger(CONFIG_KEY_VALUE_INDEX_CITY));
		_valueIndexState = Objects.requireNonNull(
			configJsonObject.getInteger(CONFIG_KEY_VALUE_INDEX_STATE));
		_valueIndexZipCode = Objects.requireNonNull(
			configJsonObject.getInteger(CONFIG_KEY_VALUE_INDEX_ZIP_CODE));
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
	protected void init(String[] values) throws Exception {
		String zipCode = StringUtils.unquote(values[_valueIndexZipCode]);

		if (!CharUtils.isAsciiNumeric(zipCode.charAt(0))) {
			return;
		}

		String city = StringUtils.unquote(values[_valueIndexCity]);
		String state = StringUtils.unquote(values[_valueIndexState]);

		CityAndState cityAndState = new CityAndState(city, state);

		_cityAndStateZipCodesMap.put(cityAndState, zipCode);
		_zipCodeCityAndStatesMap.put(zipCode, cityAndState);
	}

	protected static final String CONFIG_KEY_VALUE_INDEX_CITY =
		"valueIndexCity";

	protected static final String CONFIG_KEY_VALUE_INDEX_STATE =
		"valueIndexState";

	protected static final String CONFIG_KEY_VALUE_INDEX_ZIP_CODE =
		"valueIndexZipCode";

	private final LocalMap<CityAndState, String> _cityAndStateZipCodesMap;
	private final int _valueIndexCity;
	private final int _valueIndexState;
	private final int _valueIndexZipCode;
	private final LocalMap<String, CityAndState> _zipCodeCityAndStatesMap;

}