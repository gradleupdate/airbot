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

package com.github.ithildir.airbot.util;

import com.thanglequoc.aqicalculator.AQICalculator;
import com.thanglequoc.aqicalculator.PollutantCode;

import java.util.Locale;

/**
 * @author Andrea Di Giorgi
 */
public class AirBotUtil {

	public static int getAQI(String pollutant, double value) {
		PollutantCode pollutantCode = _getPollutantCode(pollutant);

		if (pollutantCode == null) {
			return -1;
		}

		AQICalculator aqiCalculator = AQICalculator.getAQICalculatorInstance();

		return aqiCalculator.getAQIforPollutant(
			pollutantCode.getLiteral(), value);
	}

	public static String getAQILevel(int aqi, Locale locale) {
		String key = "good";

		if ((aqi >= 51) && (aqi <= 100)) {
			key = "moderate";
		}
		else if ((aqi >= 101) && (aqi <= 150)) {
			key = "unhealthy-for-sensitive-groups";
		}
		else if ((aqi >= 151) && (aqi <= 200)) {
			key = "unhealthy";
		}
		else if ((aqi >= 201) && (aqi <= 300)) {
			key = "very-unhealthy";
		}
		else if (aqi > 300) {
			key = "hazardous";
		}

		return LanguageUtil.get(locale, key);
	}

	private static PollutantCode _getPollutantCode(String pollutant) {
		if (pollutant.equals("co")) {
			return PollutantCode.CO;
		}
		else if (pollutant.equals("no2")) {
			return PollutantCode.NO2;
		}
		else if (pollutant.equals("o3")) {
			return PollutantCode.O3;
		}
		else if (pollutant.equals("pm10")) {
			return PollutantCode.PM10;
		}
		else if (pollutant.equals("pm25")) {
			return PollutantCode.PM25;
		}
		else if (pollutant.equals("so2")) {
			return PollutantCode.SO2;
		}

		return null;
	}

}