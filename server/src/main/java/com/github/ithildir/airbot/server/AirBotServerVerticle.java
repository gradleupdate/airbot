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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;

/**
 * @author Andrea Di Giorgi
 */
public class AirBotServerVerticle extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		DeploymentOptions deploymentOptions = new DeploymentOptions();

		deploymentOptions.setConfig(config());

		CompositeFuture initCompositeFuture = CompositeFuture.all(
			_deployVerticle(AreaServiceVerticle.class, deploymentOptions),
			_deployVerticle(GeoServiceVerticle.class, deploymentOptions),
			_deployVerticle(ReportServiceVerticle.class, deploymentOptions));

		initCompositeFuture.setHandler(
			compositeFuture -> {
				if (compositeFuture.succeeded()) {
					startFuture.complete();
				}
				else {
					startFuture.fail(compositeFuture.cause());
				}
			});
	}

	private Future<String> _deployVerticle(
		Class<? extends Verticle> clazz, DeploymentOptions deploymentOptions) {

		Future<String> future = Future.future();

		vertx.deployVerticle(
			clazz.getName(), deploymentOptions, future.completer());

		return future;
	}

}