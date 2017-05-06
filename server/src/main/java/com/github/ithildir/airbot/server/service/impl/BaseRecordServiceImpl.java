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

import com.github.ithildir.airbot.server.service.RecordService;

import io.netty.handler.codec.http.HttpResponseStatus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.RecordParser;

import java.util.Objects;

/**
 * @author Andrea Di Giorgi
 */
public abstract class BaseRecordServiceImpl implements RecordService {

	public BaseRecordServiceImpl(Vertx vertx, JsonObject configJsonObject) {
		_delimiter = Objects.requireNonNull(
			configJsonObject.getString(getConfigKeyDelimiter()));
		_url = Objects.requireNonNull(
			configJsonObject.getString(getConfigKeyUrl()));

		HttpClientOptions httpClientOptions = new HttpClientOptions(
			configJsonObject);

		_httpClient = vertx.createHttpClient(httpClientOptions);
	}

	@Override
	public void init(Handler<AsyncResult<Void>> handler) {
		HttpClientRequest httpClientRequest = _httpClient.getAbs(_url);

		RecordParser recordParser = RecordParser.newDelimited(
			_delimiter, this::init);

		httpClientRequest.endHandler(
			v -> {
				handler.handle(Future.succeededFuture());
			});

		httpClientRequest.handler(
			httpClientResponse -> {
				int statusCode = httpClientResponse.statusCode();

				if (statusCode != HttpResponseStatus.OK.code()) {
					handler.handle(
						Future.failedFuture(
							httpClientResponse.statusMessage()));

					return;
				}

				httpClientResponse.bodyHandler(recordParser::handle);
			});

		httpClientRequest.exceptionHandler(
			t -> {
				handler.handle(Future.failedFuture(t));
			});

		httpClientRequest.end();
	}

	protected abstract String getConfigKeyDelimiter();

	protected abstract String getConfigKeyUrl();

	protected abstract void init(Buffer buffer);

	private final String _delimiter;
	private final HttpClient _httpClient;
	private final String _url;

}