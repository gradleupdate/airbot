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

import com.github.ithildir.airbot.server.util.StringUtils;

import io.netty.handler.codec.http.HttpResponseStatus;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import java.io.IOException;

import java.net.ServerSocket;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Andrea Di Giorgi
 */
public abstract class BaseRecordServiceImplTestCase
	<T extends BaseRecordServiceImpl> {

	@BeforeClass
	public static void setUpClass() {
		_vertx = Vertx.vertx();
	}

	@Before
	public void setUp(TestContext testContext) {
		_httpServerRequestsCounter = new AtomicInteger();

		Future<HttpServer> httpServerFuture = _startHttpServer();

		Future<Void> initFuture = httpServerFuture.compose(
			httpServer -> {
				_httpServer = httpServer;

				Future<Void> future = Future.future();

				JsonObject configJsonObject = createConfigJsonObject(
					"http://localhost:" + _httpServer.actualPort() + "/" +
						getFileName());

				_baseRecordServiceImpl = createRecordServiceImpl(
					_vertx, configJsonObject);

				_baseRecordServiceImpl.init(future.completer());

				return future;
			});

		initFuture.setHandler(testContext.asyncAssertSuccess());
	}

	@After
	public void tearDown(TestContext testContext) {
		_httpServer.close(testContext.asyncAssertSuccess());
	}

	@Test
	public void testInitSkip(TestContext testContext) {
		Async async = testContext.async();

		_httpServerRequestsCounter.set(0);

		_baseRecordServiceImpl.init(
			asyncResult -> {
				testContext.assertTrue(asyncResult.succeeded());

				testContext.assertEquals(1, _httpServerRequestsCounter.get());

				async.complete();
			});

		async.awaitSuccess();
	}

	@Test
	public void testInitWrongDbUrlHost(TestContext testContext)
		throws IOException {

		int port = 0;

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			port = serverSocket.getLocalPort();
		}

		JsonObject configJsonObject = createConfigJsonObject(
			"http://localhost:" + port);

		BaseRecordServiceImpl baseRecordServiceImpl = createRecordServiceImpl(
			_vertx, configJsonObject);

		baseRecordServiceImpl.init(testContext.asyncAssertFailure());
	}

	@Test
	public void testInitWrongDbUrlMethod(TestContext testContext) {
		JsonObject configJsonObject = createConfigJsonObject(
			"http://localhost:" + _httpServer.actualPort() + "/" +
				getFileName() + "?erroredMethod=get");

		BaseRecordServiceImpl baseRecordServiceImpl = createRecordServiceImpl(
			_vertx, configJsonObject);

		baseRecordServiceImpl.init(testContext.asyncAssertFailure());
	}

	@Test
	public void testInitWrongDbUrlPath(TestContext testContext) {
		JsonObject configJsonObject = createConfigJsonObject(
			"http://localhost:" + _httpServer.actualPort() + "/foo");

		BaseRecordServiceImpl baseRecordServiceImpl = createRecordServiceImpl(
			_vertx, configJsonObject);

		baseRecordServiceImpl.init(testContext.asyncAssertFailure());
	}

	protected JsonObject createConfigJsonObject(String url) {
		JsonObject configJsonObject = new JsonObject();

		configJsonObject.put(
			BaseRecordServiceImpl.CONFIG_KEY_DELIMITER, System.lineSeparator());
		configJsonObject.put(BaseRecordServiceImpl.CONFIG_KEY_URL, url);

		return configJsonObject;
	}

	protected abstract T createRecordServiceImpl(
		Vertx vertx, JsonObject configJsonObject);

	protected abstract String getFileName();

	protected T getRecordServiceImpl() {
		return _baseRecordServiceImpl;
	}

	private Future<HttpServer> _startHttpServer() {
		Future<HttpServer> future = Future.future();

		HttpServer httpServer = _vertx.createHttpServer();

		String eTag = String.valueOf(System.currentTimeMillis());
		String fileName = getFileName();

		httpServer.requestHandler(
			httpServerRequest -> {
				_httpServerRequestsCounter.incrementAndGet();

				HttpServerResponse httpServerResponse =
					httpServerRequest.response();

				String path = httpServerRequest.path();

				path = path.substring(1);

				String erroredMethod = httpServerRequest.getParam(
					"erroredMethod");

				HttpMethod httpMethod = httpServerRequest.method();

				if (StringUtils.equalsIgnoreCase(
						erroredMethod, httpMethod.name())) {

					httpServerResponse.setStatusCode(
						HttpResponseStatus.BAD_REQUEST.code());

					httpServerResponse.end();
				}
				else if (fileName.equals(path)) {
					httpServerResponse.putHeader(HttpHeaders.ETAG, eTag);

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

	private static Vertx _vertx;

	private T _baseRecordServiceImpl;
	private HttpServer _httpServer;
	private AtomicInteger _httpServerRequestsCounter;

}