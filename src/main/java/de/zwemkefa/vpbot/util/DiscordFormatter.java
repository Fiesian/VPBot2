package de.zwemkefa.vpbot.util;

import de.zwemkefa.vpbot.VPBot;
import de.zwemkefa.vpbot.timetable.Timetable;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DiscordFormatter {
    private static final String[] DAY_NAMES = new String[]{"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag"};
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.u");

    public static EmbedObject formatTimetableMessage(Timetable t, String className, boolean filterByTime) {
        List<Boolean> emptyDayList = Arrays.asList(t.getEmptyDays());
        EmbedBuilder e = new EmbedBuilder();
        e.withFooterText("Aktualisiert am " + DATE_FORMATTER.format(LocalDateTime.now()) + " um " + TIME_FORMATTER.format(LocalDateTime.now()));
        e.withTitle("Vertretungsplan " + className);
        if (filterByTime ? t.getPeriods().stream().filter(p -> p.getEnd().isAfter(LocalDateTime.now())).count() == 0 : t.getPeriods().isEmpty()) {
            if (!emptyDayList.contains(Boolean.TRUE)) {
                e.withDescription("Der Vertretungsplan ist leer.");
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
                e.withDescription("Es findet kein Unterricht statt.");
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
        e.withColor(Color.YELLOW);
        //Stream<Timetable.Period> periods = t.getPeriods().stream().sorted(Comparator.comparing(Timetable.Period::getStart));
        Iterator<Timetable.Period> periods;
        if (filterByTime)
            periods = t.getPeriods().stream().filter(p -> p.getEnd().isAfter(LocalDateTime.now())).iterator();
        else
            periods = t.getPeriods().iterator();

        StringBuilder b = null;
        int loopDay = -1;
        while (periods.hasNext()) {
            Timetable.Period p = periods.next();
            int d = p.getStart().getDayOfWeek().getValue() - 1;
            while (loopDay < d && loopDay < 4) {
                if (b != null) {
                    b.setLength(b.length() - 1);
                    e.appendField(DAY_NAMES[loopDay], b.toString(), false);
                    b = null;
                }
                if (emptyDayList.get(++loopDay)) {  //Change day here
                    b = new StringBuilder();
                    b.append("Am ").append(DAY_NAMES[loopDay]).append(" findet kein Unterricht statt.\n");
                }

            }
            if (b == null) {
                b = new StringBuilder();
            } else if (b.length() > 950) {
                b.setLength(b.length() - 1);
                e.appendField(DAY_NAMES[loopDay - 1], b.toString(), true);
                b = new StringBuilder();
            }

            String periodName = VPBot.getInstance().getPeriodResolver().getPeriod(p.getStart(), p.getEnd());
            if (periodName == null)
                b.append(TIME_FORMATTER.format(p.getStart())).append(" bis ").append(TIME_FORMATTER.format(p.getEnd())).append(":");
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
            }
            if (p.getPeriodText().isPresent() && !p.getPeriodText().get().equals("")) {
                b.append("Anmerkung: \"").append(p.getPeriodText().get()).append("\"\n");
            }
        }
        if (b != null) {
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
}
