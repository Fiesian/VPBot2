package de.zwemkefa.vpbot.util;

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

    public static EmbedObject formatTimetableMessage(Timetable t, String className) {
        List<Boolean> emptyDayList = Arrays.asList(t.getEmptyDays());
        EmbedBuilder e = new EmbedBuilder();
        e.withFooterText("Aktualisiert am " + DATE_FORMATTER.format(LocalDateTime.now()) + " um " + TIME_FORMATTER.format(LocalDateTime.now()));
        e.withTitle("Vertretungsplan " + className);
        if (t.getPeriods().isEmpty()) {
            if (!emptyDayList.contains(Boolean.TRUE)) {
                e.withDescription("Der  Vertretungsplan ist leer.");
                e.withColor(Color.GREEN);
                return e.build();
            } else if (!emptyDayList.contains(Boolean.FALSE)) {
                e.withDescription("Es findet kein Unterricht statt.");
                e.withColor(Color.YELLOW);
                return e.build();
            }
        }
        e.withColor(Color.YELLOW);
        //Stream<Timetable.Period> periods = t.getPeriods().stream().sorted(Comparator.comparing(Timetable.Period::getStart));
        Iterator<Timetable.Period> periods = t.getPeriods().iterator();

        StringBuilder b = null;
        int loopDay = -1;
        while (periods.hasNext()) {
            Timetable.Period p = periods.next();
            int d = p.getStart().getDayOfWeek().getValue();
            while (loopDay < d) {
                if (b != null) {
                    b.setLength(b.length() - 1);
                    e.appendField(DAY_NAMES[loopDay - 1], b.toString(), false);
                    b = null;
                }
                if (emptyDayList.get(++loopDay) == true) {
                    if (b == null) {
                        b = new StringBuilder();
                    }
                    b.append("Am " + DAY_NAMES[loopDay - 1] + " findet kein Unterricht statt.\n");
                }

            }
            if (b == null) {
                b = new StringBuilder();
            }
            String sub = t.getSubjectNames().containsKey(p.getSubject()) ? t.getSubjectNames().get(p.getSubject()) : "Etwas ";
            switch (p.getCellState()) {
                case CANCEL:
                    b.append(sub + " wird zwischen " + TIME_FORMATTER.format(p.getStart()) + " und " + TIME_FORMATTER.format(p.getEnd()) + " ausfallen.\n");
                    break;

                case SUBSTITUTION:
                    b.append(sub + " wird zwischen " + TIME_FORMATTER.format(p.getStart()) + " und " + TIME_FORMATTER.format(p.getEnd()) + " vertreten werden.\n");
                    break;

                case ADDITIONAL:
                    b.append(sub + " wird zwischen " + TIME_FORMATTER.format(p.getStart()) + " und " + TIME_FORMATTER.format(p.getEnd()) + " zusätzlich stattfinden.\n");
                    break;

                case FREE:
                    b.append(sub + " wird zwischen " + TIME_FORMATTER.format(p.getStart()) + " und " + TIME_FORMATTER.format(p.getEnd()) + " nicht stattfinden.\n");
                    break;

                case ROOMSUBSTITUTION:
                    b.append(sub + " wird zwischen " + TIME_FORMATTER.format(p.getStart()) + " und " + TIME_FORMATTER.format(p.getEnd()) + " in einem anderen Raum stattfinden.\n");
                    break;
            }
            if (p.getPeriodText().isPresent() && !p.getPeriodText().get().equals("")) {
                b.append("Anmerkung: \"" + p.getPeriodText().get() + "\"\n");
            }
        }
        if (b != null) {
            b.setLength(b.length() - 1);
            e.appendField(DAY_NAMES[loopDay - 1], b.toString(), false);
            //b = null;
        }
        return e.build();
    }
}
