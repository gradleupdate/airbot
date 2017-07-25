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

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Andrea Di Giorgi
 */
@RunWith(VertxUnitRunner.class)
public class SingleUserAuthProviderTest {

	@Test
	public void testAuthenticate(TestContext testContext) {
		String username = "foo";
		String password = "bar";

		AuthProvider authProvider = new SingleUserAuthProvider(
			username, password);

		authProvider.authenticate(
			_getAuthInfo(null, "wrong"), testContext.asyncAssertFailure());

		authProvider.authenticate(
			_getAuthInfo("wrong", null), testContext.asyncAssertFailure());

		authProvider.authenticate(
			_getAuthInfo("wrong", "wrong"), testContext.asyncAssertFailure());

		authProvider.authenticate(
			_getAuthInfo(username, "wrong"), testContext.asyncAssertFailure());

		authProvider.authenticate(
			_getAuthInfo("wrong", password), testContext.asyncAssertFailure());

		authProvider.authenticate(
			_getAuthInfo(username, password), testContext.asyncAssertSuccess());
	}

	private static JsonObject _getAuthInfo(String username, String password) {
		JsonObject jsonObject = new JsonObject();

		if (username != null) {
			jsonObject.put("username", username);
		}

		if (password != null) {
			jsonObject.put("password", password);
		}

		return jsonObject;
	}

}