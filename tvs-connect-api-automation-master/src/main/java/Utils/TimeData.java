package Utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeData {
    static ZonedDateTime currentutcTime = ZonedDateTime.now(ZoneId.of("UTC"));
    static ZonedDateTime fromDateTime = currentutcTime.minusHours(2);
    static ZonedDateTime toDateTime = currentutcTime.minusMinutes(0);
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static String starttime= fromDateTime.format(formatter);
    public static String endtime = toDateTime.format(formatter);

    static LocalDate fromDate = LocalDate.now();
    public static String fromdate = fromDate.toString();
    public static String todate = fromDate.plusDays(5).toString();
    public static String updatedfromdate = fromDate.plusDays(7).toString();
    public static String updatedtodate = fromDate.plusDays(9).toString();

}
