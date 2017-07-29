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

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.core.streams.WriteStream;

/**
 * @author Andrea Di Giorgi
 */
public class RecordParserWriteStream implements WriteStream<Buffer> {

	public RecordParserWriteStream(RecordParser recordParser) {
		_recordParser = recordParser;
	}

	@Override
	public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
		return this;
	}

	@Override
	public void end() {

		// Nothing to do

	}

	@Override
	public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
		return this;
	}

	@Override
	public WriteStream<Buffer> setWriteQueueMaxSize(int writeQueueMaxSize) {
		return this;
	}

	@Override
	public WriteStream<Buffer> write(Buffer buffer) {
		_recordParser.handle(buffer);

		return this;
	}

	@Override
	public boolean writeQueueFull() {
		return false;
	}

	private final RecordParser _recordParser;

}