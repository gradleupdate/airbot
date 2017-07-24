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

package com.github.ithildir.airbot;

import com.github.ithildir.airbot.constants.ConfigKeys;
import com.github.ithildir.airbot.service.GeoService;
import com.github.ithildir.airbot.service.impl.MapQuestGeoServiceImpl;

import io.vertx.core.json.JsonObject;

/**
 * @author Andrea Di Giorgi
 */
public class MapQuestGeoServiceVerticle
	extends BaseServiceVerticle<GeoService> {

	@Override
	protected String getAddress() {
		return GeoService.ADDRESS;
	}

	@Override
	protected GeoService getServiceImpl(JsonObject configJsonObject) {
		String key = configJsonObject.getString(ConfigKeys.MAPQUEST_KEY);
		boolean open = configJsonObject.getBoolean(
			ConfigKeys.MAPQUEST_OPEN, _DEFAULT_MAPQUEST_OPEN);

		return new MapQuestGeoServiceImpl(vertx, key, open);
	}

	@Override
	protected Class<GeoService> getServiceInterface() {
		return GeoService.class;
	}

	private static final boolean _DEFAULT_MAPQUEST_OPEN = true;

}