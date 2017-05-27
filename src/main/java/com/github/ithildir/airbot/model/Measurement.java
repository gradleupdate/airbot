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

import java.util.Map;

/**
 * @author Andrea Di Giorgi
 */
@DataObject(generateConverter = true)
public class Measurement {

	public Measurement(JsonObject jsonObject) {
		MeasurementConverter.fromJson(jsonObject, this);
	}

	public Measurement(
		String city, long time, int aqi, String mainPollutant,
		Map<String, Double> values) {

		_city = city;
		_time = time;
		_aqi = aqi;
		_mainPollutant = mainPollutant;
		_values = values;
	}

	public int getAqi() {
		return _aqi;
	}

	public String getCity() {
		return _city;
	}

	public String getMainPollutant() {
		return _mainPollutant;
	}

	public long getTime() {
		return _time;
	}

	public Map<String, Double> getValues() {
		return _values;
	}

	public void setAqi(int aqi) {
		_aqi = aqi;
	}

	public void setCity(String city) {
		_city = city;
	}

	public void setMainPollutant(String mainPollutant) {
		_mainPollutant = mainPollutant;
	}

	public void setTime(long time) {
		_time = time;
	}

	public void setValues(Map<String, Double> values) {
		_values = values;
	}

	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();

		MeasurementConverter.toJson(this, jsonObject);

		return jsonObject;
	}

	private int _aqi;
	private String _city;
	private String _mainPollutant;
	private long _time;
	private Map<String, Double> _values;

}