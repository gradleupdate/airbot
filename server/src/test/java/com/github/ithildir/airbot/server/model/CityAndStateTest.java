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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrea Di Giorgi
 */
public class CityAndStateTest {

	@Test
	public void testEquals() {
		CityAndState cityAndState = new CityAndState("foo", "ca");

		Assert.assertTrue(cityAndState.equals(cityAndState));

		CityAndState cityAndState2 = new CityAndState("fOo", "cA");

		Assert.assertTrue(cityAndState.equals(cityAndState2));

		cityAndState2 = new CityAndState("bar baz", "wa");

		Assert.assertFalse(cityAndState.equals(cityAndState2));
	}

	@Test
	public void testToString() {
		_testToString("bar baz", "wa", "Bar Baz, WA");
		_testToString("foo", "ca", "Foo, CA");
	}

	private void _testToString(String city, String state, String s) {
		CityAndState cityAndState = new CityAndState(city, state);

		Assert.assertEquals(s, cityAndState.toString());
	}

}