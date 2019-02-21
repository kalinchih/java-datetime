import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.zone.ZoneRulesException;
import java.util.TimeZone;

public class Main {

    public static void main(String[] args) {
        Main main = new Main();
        //main.listZoneIds();
        //main.timezone();
        //main.endOfMonthDay();
        //main.mySQLDatetime();
    }

    private void mySQLDatetime() {
        ObjectMapper objectMapper = new ObjectMapper();
        // MySQL JDBC parameter: https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-configuration-properties.html
        // Set "useLegacyDatetimeCode = false" or set "program default timezone" to avoid timezone converted by JDBC between program and DB.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String connString = "jdbc:mysql://host:port/tmp?useLegacyDatetimeCode=false";
        String connUser = "user";
        String connPassword = "pwd";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        try (Connection conn = DriverManager.getConnection(connString, connUser, connPassword)) {
            try (PreparedStatement ppst = conn.prepareStatement("INSERT INTO date_test (date_time, date_time_3, date_time_6) VALUES (?, ?, ?)")) {
                long now = ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toEpochMilli();
                long now2 = 1551365999999L;
                ppst.setTimestamp(1, new java.sql.Timestamp(now2));
                ppst.setTimestamp(2, new java.sql.Timestamp(now2));
                ppst.setTimestamp(3, new java.sql.Timestamp(now2));
                //ppst.execute();
                try (PreparedStatement ppstQuery = conn.prepareStatement("SELECT * FROM date_test")) {
                    try (ResultSet rs = ppstQuery.executeQuery()) {
                        while (rs.next()) {
                            if (rs.getTimestamp("date_time_3") != null) {
                                ZonedDateTime utc = rs.getTimestamp("date_time_3").toInstant().atZone(ZoneId.of("UTC"));
                                ZonedDateTime local = utc.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));
                                System.out.println(String.format("UTC = %1s; local = %2s", utc, local));
                                System.out.println(objectMapper.writeValueAsString(parseResultset(rs)));
                            } else {
                                System.out.println(rs.getTimestamp("date_time_3"));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    private DateTimeObj parseResultset(ResultSet rs) throws SQLException {
        DateTimeObj obj = new DateTimeObj();
        obj.date_time = java.util.Date.from(rs.getTimestamp("date_time").toInstant());
        obj.date_time_3 = java.util.Date.from(rs.getTimestamp("date_time_3").toInstant());
        obj.date_time_6 = java.util.Date.from(rs.getTimestamp("date_time_6").toInstant());
        //System.out.println(obj.date_time_3.getTime());
        return obj;
    }

    private void listZoneIds() {
        int maxZoneIdLen = 0;
        for (String zoneId : ZoneId.getAvailableZoneIds()) {
            if (zoneId.length() > maxZoneIdLen) {
                maxZoneIdLen = zoneId.length();
            }
            System.out.println(String.format("%1s,%2s", zoneId, zoneId.length()));
        }
        System.out.println(String.format("Max zoneId length: %1s", maxZoneIdLen));
    }

    private void fromString() {
        // init zoned datetime by string
        ZonedDateTime d0 = ZonedDateTime.parse("2017-01-25T15:59:59.999Z");
        System.out.println(d0);
        ZonedDateTime d1 = ZonedDateTime.parse("2018-01-31T14:59:59.999Z");
        System.out.println(d1);
    }

    private void diffDates() {
        ZonedDateTime d0 = ZonedDateTime.parse("2017-01-25T15:59:59.999Z");
        ZonedDateTime d1 = ZonedDateTime.parse("2018-01-31T14:59:59.999Z");
        // calculate the diff days between 2 dates (same timezone)
        long daysDiff_D01 = ChronoUnit.DAYS.between(d0, d1);
        System.out.println(daysDiff_D01);
        // calculate the diff minutes between 2 dates (same timezone)
        long minutesDiff_D01 = ChronoUnit.MINUTES.between(d0, d1);
        System.out.println(minutesDiff_D01);
    }

    private void minusTime() {
        ZonedDateTime d1 = ZonedDateTime.parse("2018-01-31T14:59:59.999Z");
        ZonedDateTime d4 = d1.minusMinutes(534180);
        System.out.println(d4);
    }

    private void timezone() {
        // get syste timezone
        System.out.println(String.format("ZoneId.systemDefault() = %1s", ZoneId.systemDefault()));
        // utc now
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
        System.out.println(String.format("ZonedDateTime.now(ZoneId.of(\"UTC\")) = %1s", utcNow));
        // wrong zone id
        try {
            ZonedDateTime wrongZoneId = ZonedDateTime.now(ZoneId.of("wrongZoneId"));
            System.out.println(String.format("ZonedDateTime.now(ZoneId.of(\"wrongZoneId\")) = %1s", wrongZoneId));
        } catch (ZoneRulesException e) {
            System.err.println(e.toString());
        }
        // local date time
        LocalDateTime localDateTime = utcNow.toLocalDateTime();
        System.out.println(String.format("localDateTime = %1s (date no change!!)", localDateTime));
        // local date
        LocalDate localDate = utcNow.toLocalDate();
        System.out.println(String.format("LocalDate = %1s (date no change!!)", localDate));
        // to epoch
        long epochSec = utcNow.toEpochSecond();
        System.out.println(String.format("ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli() = %1s", utcNow.toInstant().toEpochMilli()));
        // change timezone
        ZonedDateTime d3 = utcNow.withZoneSameInstant(ZoneId.of("America/Los_Angeles"));
        System.out.println(String.format("America/Los_Angeles = %1s", d3));
    }

    private void endOfMonthDay() {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
        System.out.println(String.format("(UTC) Now = %1s", utcNow));
        // Change to local time
        ZonedDateTime localNow = utcNow.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));
        System.out.println(String.format("(Asia/Tokyo) Now = %1s", localNow));
        // Plus 1 year
        ZonedDateTime plus1YearDate = localNow.plusYears(1);
        System.out.println(String.format("(Asia/Tokyo) 1 year later = %1s", plus1YearDate));
        // End of month
        ZonedDateTime endOfMonth = plus1YearDate.with(TemporalAdjusters.lastDayOfMonth());
        System.out.println(String.format("(Asia/Tokyo) End of month = %1s", endOfMonth));
        // End of day
        ZonedDateTime endOfDay = endOfMonth.with(LocalTime.MAX);
        System.out.println(String.format("(Asia/Tokyo) End of day = %1s", endOfDay));
        // Epoch
        System.out.println(String.format("(Epoch) End of day = %1s", endOfDay.toInstant().toEpochMilli()));
    }

    class DateTimeObj {

        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public java.util.Date date_time;
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public java.util.Date date_time_3;
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public java.util.Date date_time_6;
    }
}
