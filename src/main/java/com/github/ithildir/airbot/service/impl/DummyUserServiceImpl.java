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
import com.github.ithildir.airbot.service.UserService;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Di Giorgi
 */
public class DummyUserServiceImpl implements UserService {

	@Override
	public void getUserLocation(
		String userId, Handler<AsyncResult<Location>> handler) {

		Location location = _userLocationsMap.get(userId);

		handler.handle(Future.succeededFuture(location));
	}

	@Override
	public void updateUserLocation(
		String userId, double latitude, double longitude, String country,
		Handler<AsyncResult<Void>> handler) {

		Location location = new Location(latitude, longitude, country);

		_userLocationsMap.put(userId, location);

		handler.handle(Future.succeededFuture());
	}

	private final Map<String, Location> _userLocationsMap = new HashMap<>();

}