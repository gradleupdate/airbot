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

package com.github.ithildir.airbot.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * @author Andrea Di Giorgi
 */
public class LanguageUtils {

	public static String get(String key) {
		MutableInt maxIndex = _languageKeyMaxIndexes.get(key);

		if (maxIndex == null) {
			throw new IllegalArgumentException(
				"Language key \"" + key + "\" not found");
		}

		int index = RandomUtils.nextInt(0, maxIndex.getValue());

		return _languageValues.get(key + index);
	}

	private LanguageUtils() {
	}

	private static Map<String, MutableInt> _languageKeyMaxIndexes =
		new HashMap<>();
	private static Map<String, String> _languageValues = new HashMap<>();

	static {
		ClassLoader classLoader = LanguageUtils.class.getClassLoader();

		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream(
						"content/Language.properties"),
					StandardCharsets.UTF_8))) {

			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				int pos = line.indexOf('=');

				String key = line.substring(0, pos);
				String value = line.substring(pos + 1);

				MutableInt maxIndex = _languageKeyMaxIndexes.get(key);

				if (maxIndex == null) {
					maxIndex = new MutableInt(1);

					_languageKeyMaxIndexes.put(key, maxIndex);
				}
				else {
					maxIndex.increment();
				}

				_languageValues.put(
					key + String.valueOf(maxIndex.getValue() - 1), value);
			}
		}
		catch (IOException ioe) {
			throw new ExceptionInInitializerError(ioe);
		}
	}

}