/*Copyright (c) 2026 Tim Graves.

The Universal Permissive License (UPL), Version 1.0

Subject to the condition set forth below, permission is hereby granted to any
person obtaining a copy of this software, associated documentation and/or data
(collectively the "Software"), free of charge and under any and all copyright
rights in the Software, and any and all patent rights owned or freely
licensable by each licensor hereunder covering either (i) the unmodified
Software as contributed to or provided by such licensor, or (ii) the Larger
Works (as defined below), to deal in both

(a) the Software, and
(b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
one is included with the Software (each a "Larger Work" to which the Software
is contributed by such licensors),

without restriction, including without limitation the rights to copy, create
derivative works of, display, perform, and distribute the Software and make,
use, sell, offer for sale, import, export, have made, and have sold the
Software and the Larger Work(s), and to sublicense the foregoing rights on
either these or other terms.

This license is subject to the following condition:
The above copyright notice and either this complete permission notice or at
a minimum a reference to the UPL must be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package timgutilities.textio.test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import timgutilities.textio.TextIOUtils;

public class DateTimeTests {

	public final static void main(String args[]) throws IOException {
		TextIOUtils.doOutput("Selected date is "
				+ TextIOUtils.getISODate("price date", LocalDate.now().minusYears(1), LocalDate.now().plusYears(1)));

		TextIOUtils.doOutput("Selected time is "
				+ TextIOUtils.getISOTime("price time", LocalTime.of(9, 30, 45), LocalTime.of(23, 30, 15)));

		TextIOUtils
				.doOutput("Selected TZ id is " + TextIOUtils.getTimeZoneNameByName("Please chose a time zone", false));

		TextIOUtils.doOutput(
				"Selected TZ id is " + TextIOUtils.getISOTimeZoneOffset("Please chose a timezone offset", true));

		TextIOUtils.doOutput(
				"Selected DTG id is " + TextIOUtils.getISODateTimeTimeZone("Please enter a date, time and TZ"));

		TextIOUtils.doOutput("Selected DTG id is " + TextIOUtils.getISODateTimeTimeZone("price dtg with TZ", true, true,
				true, LocalDateTime.now().minusYears(1), LocalDateTime.now().plusYears(1)));

		TextIOUtils.doOutput("Selected DTG id is "
				+ TextIOUtils.getISOLocalDateTime("price dtg no TZ").format(DateTimeFormatter.ISO_DATE_TIME));
	}
}
