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

package com.github.ithildir.airbot.service.impl;

import com.github.ithildir.airbot.model.Location;
import com.github.ithildir.airbot.service.GeoService;

import io.netty.handler.codec.http.HttpResponseStatus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.serviceproxy.ServiceException;

import java.util.Objects;

/**
 * @author Andrea Di Giorgi
 */
public class MapQuestGeoServiceImpl implements GeoService {

	public MapQuestGeoServiceImpl(Vertx vertx, String key, boolean open) {
		_key = Objects.requireNonNull(key);

		WebClientOptions webClientOptions = new WebClientOptions();

		String defaultHost = "www.mapquestapi.com";

		if (open) {
			defaultHost = "open.mapquestapi.com";
		}

		webClientOptions.setDefaultHost(defaultHost);
		webClientOptions.setDefaultPort(443);
		webClientOptions.setSsl(true);

		_webClient = WebClient.create(vertx, webClientOptions);
	}

	@Override
	public void getLocationByCoordinates(
		double latitude, double longitude,
		Handler<AsyncResult<Location>> handler) {

		HttpRequest<Buffer> httpRequest = _webClient.get(
			"/geocoding/v1/reverse");

		httpRequest.setQueryParam("location", latitude + "," + longitude);

		_getLocation(httpRequest, handler);
	}

	@Override
	public void getLocationByQuery(
		String query, Handler<AsyncResult<Location>> handler) {

		HttpRequest<Buffer> httpRequest = _webClient.get(
			"/geocoding/v1/address");

		httpRequest.setQueryParam("ignoreLatLngInput", "true");
		httpRequest.setQueryParam("location", query);
		httpRequest.setQueryParam("maxResults", "1");
		httpRequest.setQueryParam("thumbMaps", "false");

		_getLocation(httpRequest, handler);
	}

	private void _getLocation(
		HttpRequest<Buffer> httpRequest,
		Handler<AsyncResult<Location>> handler) {

		httpRequest.setQueryParam("key", _key);

		httpRequest.send(
			asyncResult -> {
				HttpResponse<Buffer> httpResponse = _handleHttpResponse(
					asyncResult, handler);

				if (httpResponse == null) {
					return;
				}

				Location location = _getLocation(
					httpResponse.bodyAsJsonObject());

				handler.handle(Future.succeededFuture(location));
			});
	}

	private Location _getLocation(JsonObject jsonObject) {
		JsonArray resultsJsonArray = jsonObject.getJsonArray("results");

		JsonObject resultJsonObject = resultsJsonArray.getJsonObject(0);

		JsonArray locationsJsonArray = resultJsonObject.getJsonArray(
			"locations");

		JsonObject locationJsonObject = locationsJsonArray.getJsonObject(0);

		JsonObject latLngJsonObject = locationJsonObject.getJsonObject(
			"latLng");

		double latitude = latLngJsonObject.getDouble("lat");
		double longitude = latLngJsonObject.getDouble("lng");

		String country = locationJsonObject.getString("adminArea1");

		return new Location(latitude, longitude, country);
	}

	private <R, T> HttpResponse<T> _handleHttpResponse(
		AsyncResult<HttpResponse<T>> asyncResult,
		Handler<AsyncResult<R>> handler) {

		if (asyncResult.failed()) {
			handler.handle(Future.failedFuture(asyncResult.cause()));

			return null;
		}

		HttpResponse<T> httpResponse = asyncResult.result();

		int statusCode = httpResponse.statusCode();

		if (statusCode != HttpResponseStatus.OK.code()) {
			handler.handle(
				ServiceException.fail(statusCode, httpResponse.bodyAsString()));

			return null;
		}

		return httpResponse;
	}

	private final String _key;
	private final WebClient _webClient;

}