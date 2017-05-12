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

package com.github.ithildir.airbot.server.model;

import io.vertx.core.shareddata.Shareable;

import java.util.Date;

/**
 * @author Andrea Di Giorgi
 */
public class Report implements Shareable {

	public Report(
		int aqi, String mainPollutant, Date validDate, String timeZoneId,
		boolean actionDay, String discussion) {

		_aqi = aqi;
		_mainPollutant = mainPollutant;
		_validDate = validDate;
		_timeZoneId = timeZoneId;
		_actionDay = actionDay;
		_discussion = discussion;
	}

	public int getAQI() {
		return _aqi;
	}

	public String getDiscussion() {
		return _discussion;
	}

	public String getMainPollutant() {
		return _mainPollutant;
	}

	public String getTimeZoneId() {
		return _timeZoneId;
	}

	public Date getValidDate() {
		return _validDate;
	}

	public boolean isActionDay() {
		return _actionDay;
	}

	private final boolean _actionDay;
	private final int _aqi;
	private final String _discussion;
	private final String _mainPollutant;
	private final String _timeZoneId;
	private final Date _validDate;

}