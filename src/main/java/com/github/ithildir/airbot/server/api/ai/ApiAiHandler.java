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

package com.github.ithildir.airbot.server.api.ai;

import ai.api.GsonFactory;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import ai.api.model.Result;

import com.google.gson.Gson;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.net.HttpURLConnection;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Di Giorgi
 */
public class ApiAiHandler implements Handler<RoutingContext> {

	public ApiAiHandler(ApiAiFulfillmentBuilder... apiAiFulfillmentBuilders) {
		for (ApiAiFulfillmentBuilder apiAiFulfillmentBuilder :
				apiAiFulfillmentBuilders) {

			_apiAiFulfillmentBuilders.put(
				apiAiFulfillmentBuilder.getAction(), apiAiFulfillmentBuilder);
		}
	}

	@Override
	public void handle(RoutingContext routingContext) {
		AIResponse aiResponse = _gson.fromJson(
			routingContext.getBodyAsString(), AIResponse.class);

		Result result = aiResponse.getResult();

		String action = result.getAction();

		ApiAiFulfillmentBuilder apiAiFulfillmentBuilder =
			_apiAiFulfillmentBuilders.get(action);

		Future<Fulfillment> future = apiAiFulfillmentBuilder.build(aiResponse);

		HttpServerResponse httpServerResponse = routingContext.response();

		future.setHandler(
			asyncResult -> {
				if (asyncResult.failed()) {
					_logger.error(
						"Unable to handle API.AI request {0}",
						asyncResult.cause(), aiResponse);

					routingContext.fail(HttpURLConnection.HTTP_INTERNAL_ERROR);

					return;
				}

				httpServerResponse.end(_gson.toJson(asyncResult.result()));
			});
	}

	private static final Logger _logger = LoggerFactory.getLogger(
		ApiAiHandler.class);

	private static final Gson _gson;

	static {
		GsonFactory gsonFactory = GsonFactory.getDefaultFactory();

		_gson = gsonFactory.getGson();
	}

	private final Map<String, ApiAiFulfillmentBuilder>
		_apiAiFulfillmentBuilders = new HashMap<>();

}