package de.zwemkefa.vpbot.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateHelper {
    public static LocalDateTime getDate(LocalDateTime d) {
        switch (d.getDayOfWeek()) {
            case FRIDAY:
                if (d.getHour() > 14)
                    d = d.plusDays(3);
                break;

            case SATURDAY:
            case SUNDAY:
                d = d.plusDays(2);
                break;
        }
        return d;
    }

    public static int getWeekOfYear(LocalDate time) {
        LocalDate b = LocalDate.ofYearDay(time.getYear(), 1);
        b = b.minusDays(b.getDayOfWeek().getValue() - 1); //Monday: 1 -> 0
        return (time.getDayOfYear() - b.getDayOfYear()) / 7;
    }

    public static LocalDate getDayByWeek(int year, int week, DayOfWeek day) {
        LocalDate b = LocalDate.ofYearDay(year, 1);
        b = b.minusDays(b.getDayOfWeek().getValue() - 1); //Monday: 1 -> 0
        return b.plusWeeks(week).plusDays(day.getValue() - 1);
    }
}
