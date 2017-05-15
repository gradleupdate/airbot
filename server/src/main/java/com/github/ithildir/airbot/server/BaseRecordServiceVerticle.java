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

package com.github.ithildir.airbot.server;

import com.github.ithildir.airbot.server.service.RecordService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.Objects;

/**
 * @author Andrea Di Giorgi
 */
public abstract class BaseRecordServiceVerticle<T extends RecordService>
	extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		JsonObject configJsonObject = config();

		configJsonObject = Objects.requireNonNull(
			configJsonObject.getJsonObject(getConfigKey()));

		ProxyHelper.registerService(
			getServiceInterface(), vertx, getService(configJsonObject),
			getServiceAddress());

		long delay = configJsonObject.getLong("delay");

		vertx.setPeriodic(
			delay,
			timerId -> {
				_init();
			});

		Future<Void> initFuture = _init();

		initFuture.setHandler(startFuture.completer());
	}

	protected abstract String getConfigKey();

	protected abstract T getService(JsonObject configJsonObject);

	protected abstract String getServiceAddress();

	protected abstract Class<T> getServiceInterface();

	private Future<Void> _init() {
		Future<Void> future = Future.future();

		T service = ProxyHelper.createProxy(
			getServiceInterface(), vertx, getServiceAddress());

		service.init(future.completer());

		return future;
	}

}