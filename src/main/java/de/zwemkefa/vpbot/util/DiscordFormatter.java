package de.zwemkefa.vpbot.util;

import de.zwemkefa.vpbot.VPBot;
import de.zwemkefa.vpbot.timetable.Timetable;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordFormatter {
    private static final String[] DAY_NAMES = new String[]{"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag"};
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.u");
    private static final DateTimeFormatter DATE_FORMATTER_WO_YEAR = DateTimeFormatter.ofPattern("d.M.");

    public static EmbedObject formatTimetableMessage(Timetable t, String className, boolean filterByTime, LocalDateTime ttTime) {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Boolean> emptyDayList = Arrays.asList(t.getEmptyDays());

        EmbedBuilder e = new EmbedBuilder();

        int week = DateHelper.getWeekOfYear(ttTime.toLocalDate());
        LocalDate start = DateHelper.getDayByWeek(ttTime.getYear(), week, DayOfWeek.MONDAY);
        LocalDate end = DateHelper.getDayByWeek(ttTime.getYear(), week, DayOfWeek.FRIDAY);

        e.withFooterText("Aktualisiert am " + DATE_FORMATTER.format(currentTime) + " um " + TIME_FORMATTER.format(currentTime));
        e.withTitle("Vertretungsplan " + className);
        //e.withDescription("Der Vetretungsplan ist leer.\n\n`" + DATE_FORMATTER_WO_YEAR.format(start) + " bis " + DATE_FORMATTER_WO_YEAR.format(end) + "`\n\n");

        if (filterByTime ? t.getPeriods().stream().filter(p -> p.getEnd().isAfter(currentTime)).count() == 0 : t.getPeriods().isEmpty()) {
            if (!emptyDayList.contains(Boolean.TRUE)) {
                e.withDescription("Der Vertretungsplan ist leer.\n\n`" + DATE_FORMATTER_WO_YEAR.format(start) + " bis " + DATE_FORMATTER_WO_YEAR.format(end) + "`");
                e.withColor(Color.GREEN);
                t.getMessagesOfDay().forEach(s -> e.appendField("**Nachricht**", s.replaceAll("<b>", "**")
                        .replaceAll("</b>", "**")
                        .replaceAll("<i>", "*")
                        .replaceAll("</i>", "*")
                        .replaceAll("<u>", "__")
                        .replaceAll("</u>", "__")
                        .replaceAll("<del>", "~~")
                        .replaceAll("</del>", "~~")
                        .replaceAll("<br>", "\n")
                        .replaceAll("</br>", "\n"), false));
                return e.build();
            } else if (!emptyDayList.contains(Boolean.FALSE)) {
                e.withDescription("Es findet kein Unterricht statt.\n\n`" + DATE_FORMATTER_WO_YEAR.format(start) + " bis " + DATE_FORMATTER_WO_YEAR.format(end) + "`");
                e.withColor(Color.GREEN);
                t.getMessagesOfDay().forEach(s -> e.appendField("**Nachricht**", s.replaceAll("<b>", "**")
                        .replaceAll("</b>", "**")
                        .replaceAll("<i>", "*")
                        .replaceAll("</i>", "*")
                        .replaceAll("<u>", "__")
                        .replaceAll("</u>", "__")
                        .replaceAll("<del>", "~~")
                        .replaceAll("</del>", "~~")
                        .replaceAll("<br>", "\n")
                        .replaceAll("</br>", "\n"), false));
                return e.build();
            }
        }
        e.withDescription("`" + DATE_FORMATTER_WO_YEAR.format(start) + " bis " + DATE_FORMATTER_WO_YEAR.format(end) + "`");
        e.withColor(Color.YELLOW);
        //Stream<Timetable.Period> periods = t.getPeriods().stream().sorted(Comparator.comparing(Timetable.Period::getStart));
        Iterator<Timetable.Period> periods;
        if (filterByTime)
            periods = t.getPeriods().stream().filter(p -> p.getEnd().isAfter(LocalDateTime.now())).iterator();
        else
            periods = t.getPeriods().iterator();

        if (!periods.hasNext()) {
            for (int i = 0; i <= 4; i++) {
                if (emptyDayList.get(i)) {
                    e.appendField(DAY_NAMES[i], "Es findet kein Unterricht statt.", false);
                }
            }
        }

        StringBuilder b = null;
        int loopDay = -1;
        while (periods.hasNext()) {
            Timetable.Period p = periods.next();
            int d = p.getStart().getDayOfWeek().getValue() - 1;

            if (b != null && b.length() > 900) {
                b.setLength(b.length() - 1);
                e.appendField(DAY_NAMES[loopDay], b.toString(), true);
                b = new StringBuilder();
            }

            while (loopDay < d && loopDay < 4) {
                if (b != null) {
                    b.setLength(b.length() - 1);
                    e.appendField(DAY_NAMES[loopDay], b.toString(), false);
                    b = new StringBuilder();
                }
                if (emptyDayList.get(++loopDay)) {  //Change day here
                    b = new StringBuilder();
                    b.append("Es findet kein Unterricht statt.\n");
                }

            }

            if (b == null) {
                b = new StringBuilder();
            }

            String periodName = VPBot.getInstance().getPeriodResolver().getPeriod(p.getStart(), p.getEnd());
            if (periodName == null)
                b.append(TIME_FORMATTER.format(p.getStart())).append(" bis ").append(TIME_FORMATTER.format(p.getEnd())).append(": ");
            else
                b.append(periodName).append(". Std.: ");

            b.append(t.getSubjectNames().getOrDefault(p.getSubject(), "Eine Veranstaltung")).append(" wird");

            switch (p.getCellState()) {
                case CANCEL:
                    b.append(" ausfallen.\n");
                    break;

                case SUBSTITUTION:
                    b.append(" vertreten werden.\n");
                    break;

                case ADDITIONAL:
                    b.append(" zusätzlich stattfinden.\n");
                    break;

                case FREE:
                    b.append(" nicht stattfinden.\n");
                    break;

                case ROOMSUBSTITUTION:
                    b.append(" in einem anderen Raum stattfinden.\n");
                    break;

                case SHIFT:
                    b.append(" zusätzlich stattfinden.\n");
                    break;

                default:
                    b.append(" State: " + p.getCellState().name() + "\n");
                    break;
            }

            if(p.getRescheduleInfo().isPresent()){
                b.append("\u0009*")
                        .append(p.getRescheduleInfo().get().isSource() ? "Neuer Termin: " : "Ursprünglicher Termin: ")
                        .append(p.getRescheduleInfo().get().getStart().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMAN))
                        .append(", der ")
                        .append(DATE_FORMATTER_WO_YEAR.format(p.getRescheduleInfo().get().getStart()))
                        .append(", ");

                String rsPeriodName = VPBot.getInstance().getPeriodResolver().getPeriod(p.getRescheduleInfo().get().getStart(), p.getRescheduleInfo().get().getEnd());
                if (rsPeriodName == null)
                    b.append(TIME_FORMATTER.format(p.getStart())).append(" bis ").append(TIME_FORMATTER.format(p.getEnd()));
                else
                    b.append(rsPeriodName).append(". Std.");

                b.append("*\n");
            }

            if (p.getPeriodText().isPresent() && !p.getPeriodText().get().equals("")) {
                b.append("\u0009*Anmerkung:* \"").append(p.getPeriodText().get()).append("*\"*\n");
            }
        }
        if (b != null && !b.toString().equals("")) {
            b.setLength(b.length() - 1);
            e.appendField(DAY_NAMES[loopDay], b.toString(), false);
            //b = null;
        }

        t.getMessagesOfDay().forEach(s -> e.appendField("--Nachricht--", s.replaceAll("<b>", "**")
                .replaceAll("</b>", "**")
                .replaceAll("<i>", "*")
                .replaceAll("</i>", "*")
                .replaceAll("<u>", "__")
                .replaceAll("</u>", "__")
                .replaceAll("<del>", "~~")
                .replaceAll("</del>", "~~")
                .replaceAll("<br>", "\n")
                .replaceAll("</br>", "\n"), false));

        return e.build();
    }

    public static EmbedObject formatErrorMessage(Exception e) {
        EmbedBuilder b = new EmbedBuilder();
        b.withTitle("Ein Fehler ist aufgetreten.");
        b.appendDescription(e.getMessage());
        StackTraceElement[] trace = e.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            if (trace[i].getClassName().startsWith("de.zwemkefa")) {
                b.appendField("Stacktrace", "[" + i + "]" + " " + trace[i].getClassName() + "#" + trace[i].getMethodName() + "@" + trace[i].getLineNumber(), false);
                break;
            }
        }
        b.withColor(Color.RED);
        b.withFooterText(e.getClass().getName() + " am " + DATE_FORMATTER.format(LocalDateTime.now()) + " um " + TIME_FORMATTER.format(LocalDateTime.now()));
        return b.build();
    }

    public static EmbedObject formatErrorMessage(Collection<Exception> e) {
        EmbedBuilder b = new EmbedBuilder()
                .withColor(Color.RED)
                .withTitle("Mehrere Fehler sind aufgetreten.")
                .withFooterText("Letzter Fehler am " + DATE_FORMATTER.format(LocalDateTime.now()) + " um " + TIME_FORMATTER.format(LocalDateTime.now()));
        Map<Class, Long> map = e.stream().collect(Collectors.groupingBy(Exception::getClass, Collectors.counting()));
        map.keySet().stream().sorted(Comparator.comparingLong(map::get)).forEach((c) -> b.appendField(map.get(c) + "x", c.getName(), false));
        return b.build();
    }

    public static EmbedObject formatSocketErrorMessage(LocalDateTime since) {
        EmbedBuilder b = new EmbedBuilder()
                .withColor(Color.RED)
                .withTitle("Netzwerkfehler")
                .withDescription("Bitte eine Weile abwarten oder https://goo.gl/5wSdcL nutzen.")
                .withFooterText("Aktualisiert am " + DATE_FORMATTER.format(LocalDateTime.now()) + " um " + TIME_FORMATTER.format(LocalDateTime.now()) + " | Störung besteht seit dem " + DATE_FORMATTER.format(since) + " um " + TIME_FORMATTER.format(since));
        return b.build();
    }
}
