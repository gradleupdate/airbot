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

package com.github.ithildir.airbot.server;

import com.github.ithildir.airbot.server.service.ReportService;
import com.github.ithildir.airbot.server.service.impl.AirNowReportServiceImpl;

import io.vertx.core.json.JsonObject;

/**
 * @author Andrea Di Giorgi
 */
public class ReportServiceVerticle
	extends BaseRecordServiceVerticle<ReportService> {

	@Override
	protected String getConfigKey() {
		return "report";
	}

	@Override
	protected ReportService getService(JsonObject configJsonObject) {
		return new AirNowReportServiceImpl(vertx, configJsonObject);
	}

	@Override
	protected String getServiceAddress() {
		return ReportService.ADDRESS;
	}

	@Override
	protected Class<ReportService> getServiceInterface() {
		return ReportService.class;
	}

}