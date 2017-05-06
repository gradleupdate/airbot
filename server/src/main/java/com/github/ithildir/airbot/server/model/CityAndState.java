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

import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.text.WordUtils;

/**
 * @author Andrea Di Giorgi
 */
public class CityAndState implements Shareable {

	public CityAndState(String city, String state) {
		city = Objects.requireNonNull(city);
		state = Objects.requireNonNull(state);

		_city = WordUtils.capitalizeFully(city);
		_state = state.toUpperCase();
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}

		if (!(object instanceof CityAndState)) {
			return false;
		}

		CityAndState cityAndState = (CityAndState)object;

		EqualsBuilder equalsBuilder = new EqualsBuilder();

		equalsBuilder.append(_city, cityAndState.getName());
		equalsBuilder.append(_state, cityAndState.getState());

		return equalsBuilder.isEquals();
	}

	public String getName() {
		return _city;
	}

	public String getState() {
		return _state;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_city, _state);
	}

	@Override
	public String toString() {
		return _city + ", " + _state;
	}

	private final String _city;
	private final String _state;

}