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
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.io.IOException;

import java.net.ServerSocket;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Andrea Di Giorgi
 */
@RunWith(VertxUnitRunner.class)
public class GeoServiceImplTest {

	@ClassRule
	public static final RunTestOnContext runTestOnContext =
		new RunTestOnContext();

	@BeforeClass
	public static void setUpClass(TestContext testContext) {
		Vertx vertx = runTestOnContext.vertx();

		Future<HttpServer> httpServerFuture = TestUtil.serveFiles(
			vertx, _DB_FILE_NAME);

		Future<Void> initFuture = httpServerFuture.compose(
			httpServer -> {
				_httpServer = httpServer;

				Future<Void> future = Future.future();

				JsonObject configJsonObject = new JsonObject();

				configJsonObject.put("dbLineDelimiter", System.lineSeparator());
				configJsonObject.put(
					"dbUrl",
					"http://localhost:" + _httpServer.actualPort() + "/" +
						_DB_FILE_NAME);
				configJsonObject.put("dbValueDelimiterPattern", ",");
				configJsonObject.put("dbValueIndexCity", 2);
				configJsonObject.put("dbValueIndexState", 3);
				configJsonObject.put("dbValueIndexZipCode", 0);

				_geoServiceImpl = new GeoServiceImpl(vertx, configJsonObject);

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
	public void testInitException(TestContext testContext) throws IOException {
		Vertx vertx = runTestOnContext.vertx();

		int port = 0;

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			port = serverSocket.getLocalPort();
		}

		JsonObject configJsonObject = new JsonObject();

		configJsonObject.put("dbLineDelimiter", System.lineSeparator());
		configJsonObject.put("dbUrl", "http://localhost:" + port);
		configJsonObject.put("dbValueDelimiterPattern", ",");
		configJsonObject.put("dbValueIndexCity", 2);
		configJsonObject.put("dbValueIndexState", 3);
		configJsonObject.put("dbValueIndexZipCode", 0);

		GeoServiceImpl geoServiceImpl = new GeoServiceImpl(
			vertx, configJsonObject);

		geoServiceImpl.init(testContext.asyncAssertFailure());
	}

	@Test
	public void testInitWrongDbUrl(TestContext testContext) {
		Vertx vertx = runTestOnContext.vertx();

		JsonObject configJsonObject = new JsonObject();

		configJsonObject.put("dbLineDelimiter", System.lineSeparator());
		configJsonObject.put(
			"dbUrl", "http://localhost:" + _httpServer.actualPort() + "/foo");
		configJsonObject.put("dbValueDelimiterPattern", ",");
		configJsonObject.put("dbValueIndexCity", 2);
		configJsonObject.put("dbValueIndexState", 3);
		configJsonObject.put("dbValueIndexZipCode", 0);

		GeoServiceImpl geoServiceImpl = new GeoServiceImpl(
			vertx, configJsonObject);

		geoServiceImpl.init(testContext.asyncAssertFailure());
	}

	private static final String _DB_FILE_NAME =
		"com/github/ithildir/airbot/server/service/impl/dependencies/db.csv";

	private static GeoServiceImpl _geoServiceImpl;
	private static HttpServer _httpServer;

}