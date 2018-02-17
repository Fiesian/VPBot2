package de.zwemkefa.vpbot.util;

import de.zwemkefa.vpbot.timetable.Timetable;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class DiscordFormatter {
    private static final String[] DAY_NAMES = new String[]{"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag"};
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static EmbedObject formatTimetableMessage(Timetable t, String className) {
        List<Boolean> emptyDayList = Arrays.asList(t.getEmptyDays());
        EmbedBuilder e = new EmbedBuilder();
        e.withFooterText("Aktualisiert am " + DateTimeFormatter.RFC_1123_DATE_TIME.format(LocalDateTime.now()));
        e.withTitle("Vertretungsplan " + className);
        if (t.getPeriods().isEmpty()) {
            if (!emptyDayList.contains(true)) {
                e.withDescription("Der  Vertretungsplan ist leer.");
                return e.build();
            } else if (!emptyDayList.contains(false)) {
                e.withDescription("Es findet kein Unterricht statt.");
                return e.build();
            }
        }
        Stream<Timetable.Period> periods = t.getPeriods().stream().sorted(Comparator.comparing(Timetable.Period::getStart));

        StringBuilder b = null;
        int loopDay = -1;
        Optional<Timetable.Period> p = periods.findFirst();
        while (p.isPresent()) {
            int d = p.get().getStart().getDayOfWeek().getValue();
            while (loopDay < d) {
                if (b != null) {
                    b.setLength(b.length() - 1);
                    e.appendField(DAY_NAMES[loopDay], b.toString(), false);
                    b = null;
                }
                if (emptyDayList.get(++loopDay) == true) {
                    if (b == null) {
                        b = new StringBuilder();
                    }
                    b.append("Am " + DAY_NAMES[loopDay] + " findet kein Unterricht statt.\n");
                }

            }
            if (b == null) {
                b = new StringBuilder();
            }
            String sub = t.getSubjectNames().containsKey(p.get().getSubject()) ? t.getSubjectNames().get(p.get().getSubject()) : "Etwas ";
            switch (p.get().getCellState()) {
                case CANCEL:
                    b.append(sub + " wird zwischen " + TIME_FORMATTER.format(p.get().getStart()) + " und " + TIME_FORMATTER.format(p.get().getEnd()) + " ausfallen.\n");
                    break;

                case SUBSTITUTION:
                    b.append(sub + " wird zwischen " + TIME_FORMATTER.format(p.get().getStart()) + " und " + TIME_FORMATTER.format(p.get().getEnd()) + " vertreten werden.\n");
                    break;

                case ADDITIONAL:
                    b.append(sub + " wird zwischen " + TIME_FORMATTER.format(p.get().getStart()) + " und " + TIME_FORMATTER.format(p.get().getEnd()) + " zusätzlich stattfinden.\n");
                    break;

                case FREE:
                    b.append(sub + " wird zwischen " + TIME_FORMATTER.format(p.get().getStart()) + " und " + TIME_FORMATTER.format(p.get().getEnd()) + " nicht stattfinden.\n");
                    break;

                case ROOMSUBSTITUTION:
                    b.append(sub + " wird zwischen " + TIME_FORMATTER.format(p.get().getStart()) + " und " + TIME_FORMATTER.format(p.get().getEnd()) + " in einem anderen Raum stattfinden.\n");
                    break;
            }
            if (p.get().getPeriodText().isPresent()) {
                b.append("Anmerkung: \"" + p.get().getPeriodText().get() + "\"\n");
            }
            p = periods.findFirst();
        }
        if (b != null) {
            b.setLength(b.length() - 1);
            e.appendField(DAY_NAMES[loopDay], b.toString(), false);
            b = null;
        }

        b.setLength(b.length() - 1);
        return e.build();
    }
}
