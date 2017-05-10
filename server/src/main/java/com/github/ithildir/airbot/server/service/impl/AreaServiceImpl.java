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

import com.github.ithildir.airbot.server.service.AreaService;
import com.github.ithildir.airbot.server.util.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.CharUtils;

/**
 * @author Andrea Di Giorgi
 */
public class AreaServiceImpl
	extends BaseRecordServiceImpl implements AreaService {

	public AreaServiceImpl(Vertx vertx, JsonObject configJsonObject) {
		super(vertx, configJsonObject);

		SharedData sharedData = vertx.sharedData();

		_zipCodeAreasMap = sharedData.getLocalMap(
			AreaServiceImpl.class.getName() + ".zipCodeAreas");

		_valueIndexArea = Objects.requireNonNull(
			configJsonObject.getInteger(CONFIG_KEY_VALUE_INDEX_AREA));
		_valueIndexZipCode = Objects.requireNonNull(
			configJsonObject.getInteger(CONFIG_KEY_VALUE_INDEX_ZIP_CODE));
	}

	@Override
	public void getArea(String zipCode, Handler<AsyncResult<String>> handler) {
		String area = _zipCodeAreasMap.get(zipCode);

		if (StringUtils.isBlank(area)) {
			handler.handle(
				Future.failedFuture("Unable to find area of " + zipCode));

			return;
		}

		handler.handle(Future.succeededFuture(area));
	}

	@Override
	protected void init(String[] values) {
		String zipCode = values[_valueIndexZipCode];

		if (!CharUtils.isAsciiNumeric(zipCode.charAt(0))) {
			if (_logger.isDebugEnabled()) {
				_logger.debug("Ignoring {0}", Arrays.toString(values));
			}

			return;
		}

		String area = values[_valueIndexArea];

		_zipCodeAreasMap.put(zipCode, area);
	}

	protected static final String CONFIG_KEY_VALUE_INDEX_AREA =
		"valueIndexArea";

	protected static final String CONFIG_KEY_VALUE_INDEX_ZIP_CODE =
		"valueIndexZipCode";

	private static final Logger _logger = LoggerFactory.getLogger(
		AreaServiceImpl.class);

	private final int _valueIndexArea;
	private final int _valueIndexZipCode;
	private final LocalMap<String, String> _zipCodeAreasMap;

}