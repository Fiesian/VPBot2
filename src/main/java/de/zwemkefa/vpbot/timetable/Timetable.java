package de.zwemkefa.vpbot.timetable;

import de.zwemkefa.vpbot.util.ExceptionHandler;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Timetable {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("YYYYMMDD");

    private boolean[] emptyDays;
    private ArrayList<Period> periods;
    private Map<Integer, String> subjectNames;

    private Timetable(boolean[] emptyDays, ArrayList<Period> periods, Map<Integer, String> subjectNames) {
        this.emptyDays = emptyDays;
        this.periods = periods;
        this.subjectNames = subjectNames;
    }

    public boolean[] getEmptyDays() {
        return emptyDays;
    }

    public ArrayList<Period> getPeriods() {
        return periods;
    }

    public Map<Integer, String> getSubjectNames() {
        return subjectNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Timetable timetable = (Timetable) o;

        if (!Arrays.equals(emptyDays, timetable.emptyDays)) return false;
        return periods != null ? periods.equals(timetable.periods) : timetable.periods == null;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(emptyDays);
        result = 31 * result + (periods != null ? periods.hashCode() : 0);
        return result;
    }

    public static Timetable ofRawJSON(String raw, Optional<ExceptionHandler> exceptionHandler) {

        boolean[] emptyDays = new boolean[]{true, true, true, true, true};
        ArrayList<Period> periods = new ArrayList<>();
        HashMap<Integer, String> subjectNames = new HashMap<>();

        try {
            //Subject names
            JSONObject subjects = new JSONObject(raw)
                    .getJSONObject("data")
                    .getJSONObject("result")
                    .getJSONObject("data")
                    .getJSONObject("elements");

            for (Integer i = 0; subjects.has(i.toString()); i++) {
                JSONObject subject = subjects.getJSONObject(i.toString());
                if (subject.getInt("type") == 3) {
                    subjectNames.put(subject.getInt("id"), subject.getString("name"));
                }
            }

            //Periods
            JSONObject elementPeriods = new JSONObject(raw)
                    .getJSONObject("data")
                    .getJSONObject("result")
                    .getJSONObject("data")
                    .getJSONObject("elementPeriods");

            for (Integer i = 0; elementPeriods.has(i.toString()); i++) {
                JSONObject period = elementPeriods.getJSONObject(i.toString());
                LocalDate date = Timetable.toLocalDate(period.getString("date"));
                emptyDays[date.getDayOfWeek().getValue() - 1] = false;

                CellState state = CellState.valueOf(period.getString("cellState"));
                if (state != CellState.STANDARD) {
                    LocalDateTime startTime = Timetable.toLocalDateTime(date, period.getString("startTime"));
                    LocalDateTime endTime = Timetable.toLocalDateTime(date, period.getString("endTime"));
                    JSONObject elements = period.getJSONObject("elements");
                    int subject = 0;
                    for (Integer j = 0; elements.has(j.toString()); j++) {
                        JSONObject element = elements.getJSONObject(j.toString());
                        if (element.getInt("type") == 3) {
                            subject = element.getInt("id");
                            break;
                        }
                    }
                    periods.add(new Period(state, startTime, endTime, subject));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (exceptionHandler.isPresent())
                exceptionHandler.get().handleException(e);

            return null;
        }
        return new Timetable(emptyDays, periods, subjectNames);
    }

    private static LocalDate toLocalDate(String s) {
        return Timetable.DATE_TIME_FORMATTER.parse(s, LocalDate::from);
    }

    private static LocalDateTime toLocalDateTime(LocalDate date, String time) {
        int timeInt = Integer.parseInt(time);
        return date.atTime(timeInt / 100, timeInt % 100);
    }

    public enum CellState {
        STANDARD, ADDITIONAL, CANCEL, SUBSTITUTION;
    }

    public static class Period {
        private CellState cellState;
        private LocalDateTime start;
        private LocalDateTime end;
        private int subject;

        Period(CellState cellState, LocalDateTime start, LocalDateTime end, int subject) {
            this.cellState = cellState;
            this.start = start;
            this.end = end;
            this.subject = subject;
        }

        public CellState getCellState() {
            return cellState;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public int getSubject() {
            return subject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Period period = (Period) o;

            if (subject != period.subject) return false;
            if (cellState != period.cellState) return false;
            if (start != null ? !start.equals(period.start) : period.start != null) return false;
            return end != null ? end.equals(period.end) : period.end == null;
        }

        @Override
        public int hashCode() {
            int result = cellState != null ? cellState.hashCode() : 0;
            result = 31 * result + (start != null ? start.hashCode() : 0);
            result = 31 * result + (end != null ? end.hashCode() : 0);
            result = 31 * result + subject;
            return result;
        }
    }
}
