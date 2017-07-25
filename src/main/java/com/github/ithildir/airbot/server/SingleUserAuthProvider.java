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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import java.util.Objects;

/**
 * @author Andrea Di Giorgi
 */
public class SingleUserAuthProvider implements AuthProvider {

	public SingleUserAuthProvider(String username, String password) {
		_username = Objects.requireNonNull(username);
		_password = Objects.requireNonNull(password);

		_user = new AbstractUser() {

			@Override
			public JsonObject principal() {
				JsonObject jsonObject = new JsonObject();

				jsonObject.put("username", _username);

				return jsonObject;
			}

			@Override
			public void setAuthProvider(AuthProvider authProvider) {
			}

			@Override
			protected void doIsPermitted(
				String permission, Handler<AsyncResult<Boolean>> handler) {

				handler.handle(Future.succeededFuture(Boolean.TRUE));
			}

		};
	}

	@Override
	public void authenticate(
		JsonObject authInfoJsonObject, Handler<AsyncResult<User>> handler) {

		String username = authInfoJsonObject.getString("username");

		if (username == null) {
			handler.handle(
				Future.failedFuture(
					"Unable to get username in authentication info"));

			return;
		}

		String password = authInfoJsonObject.getString("password");

		if (password == null) {
			handler.handle(
				Future.failedFuture(
					"Unable to get password in authentication info"));

			return;
		}

		if (!_username.equals(username) || !_password.equals(password)) {
			handler.handle(Future.failedFuture("Invalid username/password"));

			return;
		}

		handler.handle(Future.succeededFuture(_user));
	}

	private final String _password;
	private final User _user;
	private final String _username;

}