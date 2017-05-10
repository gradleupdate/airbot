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

import com.github.ithildir.airbot.server.test.util.TestUtil;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.io.IOException;

import java.net.ServerSocket;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Andrea Di Giorgi
 */
@RunWith(VertxUnitRunner.class)
public class GeoServiceImplTest {

	@BeforeClass
	public static void setUpClass(TestContext testContext) {
		_vertx = Vertx.vertx();

		Future<HttpServer> httpServerFuture = TestUtil.serveFiles(
			_vertx, _httpServerRequestsCounter, _DB_FILE_NAME);

		Future<Void> initFuture = httpServerFuture.compose(
			httpServer -> {
				_httpServer = httpServer;

				Future<Void> future = Future.future();

				JsonObject configJsonObject = _createConfigJsonObject(
					"http://localhost:" + _httpServer.actualPort() + "/" +
						_DB_FILE_NAME);

				_geoServiceImpl = new GeoServiceImpl(_vertx, configJsonObject);

				_geoServiceImpl.init(future.completer());

				return future;
			});

		initFuture.setHandler(testContext.asyncAssertSuccess());
	}

	@AfterClass
	public static void tearDownClass(TestContext testContext) {
		_httpServer.close(testContext.asyncAssertSuccess());
	}

	@Test
	public void testGetState(TestContext testContext) {
		Async async = testContext.async(2);

		_geoServiceImpl.getState(
			"00705",
			asyncResult -> {
				testContext.assertEquals("PR", asyncResult.result());

				async.countDown();
			});

		_geoServiceImpl.getState(
			"00000",
			asyncResult -> {
				testContext.assertTrue(asyncResult.failed());

				async.countDown();
			});

		async.awaitSuccess();
	}

	@Test
	public void testGetZipCode(TestContext testContext) {
		Async async = testContext.async(2);

		_geoServiceImpl.getZipCode(
			"aibonito", "pr",
			asyncResult -> {
				testContext.assertEquals("00705", asyncResult.result());

				async.countDown();
			});

		_geoServiceImpl.getZipCode(
			"foo", "ca",
			asyncResult -> {
				testContext.assertTrue(asyncResult.failed());

				async.countDown();
			});

		async.awaitSuccess();
	}

	@Test
	public void testHeaderSkipped(TestContext testContext) {
		Async async = testContext.async();

		_geoServiceImpl.getState(
			"Zipcode",
			asyncResult -> {
				testContext.assertTrue(asyncResult.failed());

				async.countDown();
			});

		async.awaitSuccess();
	}

	@Test
	public void testInitSkip(TestContext testContext) {
		Async async = testContext.async();

		_httpServerRequestsCounter.set(0);

		_geoServiceImpl.init(
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

		JsonObject configJsonObject = _createConfigJsonObject(
			"http://localhost:" + port);

		GeoServiceImpl geoServiceImpl = new GeoServiceImpl(
			_vertx, configJsonObject);

		geoServiceImpl.init(testContext.asyncAssertFailure());
	}

	@Test
	public void testInitWrongDbUrlMethod(TestContext testContext) {
		JsonObject configJsonObject = _createConfigJsonObject(
			"http://localhost:" + _httpServer.actualPort() + "/" +
				_DB_FILE_NAME + "?erroredMethod=get");

		GeoServiceImpl geoServiceImpl = new GeoServiceImpl(
			_vertx, configJsonObject);

		geoServiceImpl.init(testContext.asyncAssertFailure());
	}

	@Test
	public void testInitWrongDbUrlPath(TestContext testContext) {
		JsonObject configJsonObject = _createConfigJsonObject(
			"http://localhost:" + _httpServer.actualPort() + "/foo");

		GeoServiceImpl geoServiceImpl = new GeoServiceImpl(
			_vertx, configJsonObject);

		geoServiceImpl.init(testContext.asyncAssertFailure());
	}

	private static JsonObject _createConfigJsonObject(String url) {
		JsonObject configJsonObject = new JsonObject();

		configJsonObject.put("delimiter", System.lineSeparator());
		configJsonObject.put("url", url);
		configJsonObject.put("valueDelimiterPattern", ",");
		configJsonObject.put("valueIndexCity", 2);
		configJsonObject.put("valueIndexState", 3);
		configJsonObject.put("valueIndexZipCode", 0);

		return configJsonObject;
	}

	private static final String _DB_FILE_NAME =
		"com/github/ithildir/airbot/server/service/impl/dependencies/db.csv";

	private static GeoServiceImpl _geoServiceImpl;
	private static HttpServer _httpServer;
	private static final AtomicInteger _httpServerRequestsCounter =
		new AtomicInteger();
	private static Vertx _vertx;

}