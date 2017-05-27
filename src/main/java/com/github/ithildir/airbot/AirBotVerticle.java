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
import com.github.ithildir.airbot.server.api.ai.ApiAiHandler;
import com.github.ithildir.airbot.server.api.ai.GetAirQualityApiAiFulfillmentBuilder;
import com.github.ithildir.airbot.service.GeoService;
import com.github.ithildir.airbot.service.MeasurementService;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @author Andrea Di Giorgi
 */
public class AirBotVerticle extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		ConfigRetriever configRetriever = ConfigRetriever.create(vertx);

		Future<JsonObject> configFuture = ConfigRetriever.getConfigAsFuture(
			configRetriever);

		Future<HttpServer> httpServerFuture = configFuture.compose(
			this::_startHttpServer);

		CompositeFuture compositeFuture = CompositeFuture.all(
			_deployVerticle(MapQuestGeoServiceVerticle.class),
			_deployVerticle(WaqiMeasurementServiceVerticle.class),
			httpServerFuture);

		compositeFuture.setHandler(
			asyncResult -> {
				if (asyncResult.failed()) {
					startFuture.fail(asyncResult.cause());

					return;
				}

				startFuture.complete();
			});
	}

	private void _addHttpRouteApiAi(Router router) {
		Route route = router.route(HttpMethod.POST, "/apiai");

		route.consumes("application/json");
		route.produces("application/json");

		GeoService geoService = GeoService.getInstance(vertx);
		MeasurementService measurementService = MeasurementService.getInstance(
			vertx);

		route.handler(
			new ApiAiHandler(
				new GetAirQualityApiAiFulfillmentBuilder(
					geoService, measurementService)));
	}

	private Future<String> _deployVerticle(Class<? extends Verticle> clazz) {
		Future<String> future = Future.future();

		vertx.deployVerticle(clazz.getName(), future);

		return future;
	}

	private Future<HttpServer> _startHttpServer(JsonObject configJsonObject) {
		Future<HttpServer> future = Future.future();

		HttpServer httpServer = vertx.createHttpServer();

		Router router = Router.router(vertx);

		Route route = router.route();

		route.handler(BodyHandler.create());

		_addHttpRouteApiAi(router);

		httpServer.requestHandler(router::accept);

		int port = configJsonObject.getInteger(ConfigKeys.PORT, _DEFAULT_PORT);

		httpServer.listen(port, future);

		return future;
	}

	private static final int _DEFAULT_PORT = 8080;

}