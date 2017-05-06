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

package com.github.ithildir.airbot.server.test.util;

import io.netty.handler.codec.http.HttpResponseStatus;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Andrea Di Giorgi
 */
public class TestUtil {

	public static Future<HttpServer> serveFiles(
		Vertx vertx, String... fileNames) {

		Future<HttpServer> future = Future.future();

		HttpServer httpServer = vertx.createHttpServer();

		httpServer.requestHandler(
			httpServerRequest -> {
				HttpServerResponse httpServerResponse =
					httpServerRequest.response();

				String path = httpServerRequest.path();

				path = path.substring(1);

				if (ArrayUtils.contains(fileNames, path)) {
					httpServerResponse.sendFile(path);
				}
				else {
					httpServerResponse.setStatusCode(
						HttpResponseStatus.NOT_FOUND.code());

					httpServerResponse.end();
				}
			});

		httpServer.listen(0, future.completer());

		return future;
	}

}