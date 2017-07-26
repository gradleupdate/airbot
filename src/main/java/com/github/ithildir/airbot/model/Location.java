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

package com.github.ithildir.airbot.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author Andrea Di Giorgi
 */
@DataObject(generateConverter = true)
public class Location {

	public Location(double latitude, double longitude, String country) {
		_latitude = latitude;
		_longitude = longitude;
		_country = country;
	}

	public Location(JsonObject jsonObject) {
		LocationConverter.fromJson(jsonObject, this);
	}

	public String getCountry() {
		return _country;
	}

	public double getDistance(Location location) {
		double lat1 = Math.toRadians(getLatitude());
		double lon1 = Math.toRadians(getLongitude());

		double lat2 = Math.toRadians(location.getLatitude());
		double lon2 = Math.toRadians(location.getLongitude());

		return Math.acos(
			Math.sin(lat1) * Math.sin(lat2) +
				Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2)) *
				_EARTH_RADIUS;
	}

	public double getLatitude() {
		return _latitude;
	}

	public double getLongitude() {
		return _longitude;
	}

	public void setCountry(String country) {
		_country = country;
	}

	public void setLatitude(double latitude) {
		_latitude = latitude;
	}

	public void setLongitude(double longitude) {
		_longitude = longitude;
	}

	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();

		LocationConverter.toJson(this, jsonObject);

		return jsonObject;
	}

	private static final double _EARTH_RADIUS = 6371;

	private String _country;
	private double _latitude;
	private double _longitude;

}