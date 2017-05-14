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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.util.Objects;

/**
 * @author Andrea Di Giorgi
 */
public abstract class BaseRecordServiceImpl {

	public BaseRecordServiceImpl(Vertx vertx, JsonObject configJsonObject) {
		_delimiter = Objects.requireNonNull(
			configJsonObject.getString(CONFIG_KEY_DELIMITER));
		_url = Objects.requireNonNull(
			configJsonObject.getString(CONFIG_KEY_URL));
		_valueDelimiterPattern = Objects.requireNonNull(
			configJsonObject.getString(CONFIG_KEY_VALUE_DELIMITER_PATTERN));

		HttpClientOptions httpClientOptions = new HttpClientOptions(
			configJsonObject);

		_httpClient = vertx.createHttpClient(httpClientOptions);

		Class<?> clazz = getClass();

		_serviceName = clazz.getName();

		SharedData sharedData = vertx.sharedData();

		_urlETags = sharedData.getLocalMap(
			BaseRecordServiceImpl.class.getName() + ".eTags");
	}

	public void init(Handler<AsyncResult<Void>> handler) {
		Future<String> eTagFuture = _getETag();

		eTagFuture = eTagFuture.compose(
			eTag -> {
				String previousETag = _urlETags.get(_url);

				if (StringUtils.equalsIgnoreCase(eTag, previousETag)) {
					if (_logger.isDebugEnabled()) {
						_logger.debug(
							"{0} data is already up-to-date with ETag {1}",
							_serviceName, eTag);
					}

					return Future.succeededFuture();
				}

				if (_logger.isInfoEnabled()) {
					_logger.info(
						"{0} data is out-of-date: previous ({1}) and current " +
							"({2}) ETags do not match",
						_serviceName, previousETag, eTag);
				}

				return _init();
			});

		eTagFuture.setHandler(
			asyncResult -> {
				if (asyncResult.failed()) {
					handler.handle(Future.failedFuture(asyncResult.cause()));

					return;
				}

				String eTag = asyncResult.result();

				if (StringUtils.isNotBlank(eTag)) {
					_urlETags.put(_url, eTag);
				}

				handler.handle(Future.succeededFuture());
			});
	}

	protected abstract void init(String[] values) throws Exception;

	protected static final String CONFIG_KEY_DELIMITER = "delimiter";

	protected static final String CONFIG_KEY_URL = "url";

	protected static final String CONFIG_KEY_VALUE_DELIMITER_PATTERN =
		"valueDelimiterPattern";

	private Future<String> _getETag() {
		Future<String> future = Future.future();

		HttpClientRequest httpClientRequest = _httpClient.headAbs(_url);

		httpClientRequest.handler(
			httpClientResponse -> {
				int statusCode = httpClientResponse.statusCode();

				if (statusCode != HttpResponseStatus.OK.code()) {
					future.fail(httpClientResponse.statusMessage());

					return;
				}

				String eTag = httpClientResponse.getHeader(HttpHeaders.ETAG);

				future.complete(eTag);
			});

		httpClientRequest.exceptionHandler(future::fail);

		httpClientRequest.end();

		return future;
	}

	private Future<String> _init() {
		Future<String> future = Future.future();

		HttpClientRequest httpClientRequest = _httpClient.getAbs(_url);

		httpClientRequest.handler(
			httpClientResponse -> {
				int statusCode = httpClientResponse.statusCode();

				if (statusCode != HttpResponseStatus.OK.code()) {
					future.fail(httpClientResponse.statusMessage());

					return;
				}

				String eTag = httpClientResponse.getHeader(HttpHeaders.ETAG);

				RecordParser recordParser = RecordParser.newDelimited(
					_delimiter, this::_init);

				httpClientResponse.handler(recordParser::handle);

				httpClientResponse.endHandler(
					v -> {
						if (_logger.isInfoEnabled()) {
							_logger.info(
								"{0} data updated based on ETag {1}",
								_serviceName, eTag);
						}

						future.complete(eTag);
					});

				httpClientResponse.exceptionHandler(future::fail);
			});

		httpClientRequest.exceptionHandler(future::fail);

		httpClientRequest.end();

		return future;
	}

	private void _init(Buffer buffer) {
		String line = buffer.toString();

		String[] values = line.split(_valueDelimiterPattern);

		try {
			init(values);
		}
		catch (Exception e) {
			_logger.error("Unable to parse line: {0}", line);
		}
	}

	private static final Logger _logger = LoggerFactory.getLogger(
		BaseRecordServiceImpl.class);

	private final String _delimiter;
	private final HttpClient _httpClient;
	private final String _serviceName;
	private final String _url;
	private final LocalMap<String, String> _urlETags;
	private final String _valueDelimiterPattern;

}