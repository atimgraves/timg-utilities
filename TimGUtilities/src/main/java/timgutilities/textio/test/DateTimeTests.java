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
