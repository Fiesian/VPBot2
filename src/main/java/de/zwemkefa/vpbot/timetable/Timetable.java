package de.zwemkefa.vpbot.timetable;

import de.zwemkefa.vpbot.util.DateHelper;
import de.zwemkefa.vpbot.util.ExceptionHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Timetable {

    private Boolean[] emptyDays;
    private ArrayList<Period> periods;
    private Map<Integer, String> subjectNames;
    private ArrayList<String> messagesOfDay;

    private Timetable(Boolean[] emptyDays, ArrayList<Period> periods, Map<Integer, String> subjectNames, ArrayList<String> messagesOfDay) {
        this.emptyDays = emptyDays;
        this.periods = periods;
        this.subjectNames = subjectNames;
        this.messagesOfDay = messagesOfDay;
    }

    public static Timetable ofRawJSON(String raw, String newsRaw, ExceptionHandler exceptionHandler, Integer classID) {

        //HOTFIX 2.1.3 START
        if (raw == null || newsRaw == null) {
            return null;
        }
        //HOTFIX 2.1.3 END

        Boolean[] emptyDays = new Boolean[]{true, true, true, true, true};
        ArrayList<Period> periods = new ArrayList<>();
        HashMap<Integer, String> subjectNames = new HashMap<>();
        ArrayList<String> messagesOfDay = new ArrayList<>();

        try {
            //News
            JSONArray news = new JSONObject(newsRaw)
                    .getJSONObject("data")
                    .getJSONArray("messagesOfDay");

            for (Integer n = 0; n < news.length(); n++) {
                messagesOfDay.add(news.getJSONObject(n).getString("text"));
            }

            //Subject names
            JSONArray subjects = new JSONObject(raw)
                    .getJSONObject("data")
                    .getJSONObject("result")
                    .getJSONObject("data")
                    .getJSONArray("elements");

            for (Integer i = 0; i < subjects.length(); i++) {
                JSONObject subject = subjects.getJSONObject(i);
                if (subject.getInt("type") == 3) {
                    subjectNames.put(subject.getInt("id"), subject.getString("longName"));
                }
            }

            //Periods
            JSONArray elementPeriods = new JSONObject(raw)
                    .getJSONObject("data")
                    .getJSONObject("result")
                    .getJSONObject("data")
                    .getJSONObject("elementPeriods")
                    .getJSONArray(classID.toString());

            for (Integer i = 0; i < elementPeriods.length(); i++) {
                JSONObject period = elementPeriods.getJSONObject(i);
                LocalDate date = DateHelper.parseISODate(period.get("date").toString());
                emptyDays[date.getDayOfWeek().getValue() - 1] = false;

                CellState state = CellState.valueOf(period.getString("cellState"));
                if (state != CellState.STANDARD) {
                    LocalDateTime startTime = DateHelper.getDayAtTime(date, period.getInt("startTime"));
                    LocalDateTime endTime = DateHelper.getDayAtTime(date, period.getInt("endTime"));
                    JSONArray elements = period.getJSONArray("elements");
                    int subject = 0;
                    for (Integer j = 0; j < elements.length(); j++) {
                        JSONObject element = elements.getJSONObject(j);
                        if (element.getInt("type") == 3) {
                            subject = element.getInt("id");
                            break;
                        }
                    }

                    Optional<Period.RescheduleInfo> info = Optional.empty();
                    if(period.has("rescheduleInfo")){
                        JSONObject rsInfo = period.getJSONObject("rescheduleInfo");
                        LocalDate rsDate = DateHelper.parseISODate(rsInfo.get("date").toString());
                        LocalDateTime rsStartTime = DateHelper.getDayAtTime(rsDate, rsInfo.getInt("startTime"));
                        LocalDateTime rsEndTime = DateHelper.getDayAtTime(rsDate, rsInfo.getInt("endTime"));
                        boolean rsIsSource = rsInfo.getBoolean("isSource");
                        info = Optional.of(new Period.RescheduleInfo(rsStartTime, rsEndTime, rsIsSource));
                    }

                    periods.add(new Period(state, startTime, endTime, subject, period.has("periodText") ? Optional.of(period.getString("periodText")) : Optional.empty(), info));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            exceptionHandler.handleException(e);

            return null;
        }
        periods.sort(Comparator.comparing(Period::getStart));
        return new Timetable(emptyDays, periods, subjectNames, messagesOfDay);
    }

    public Boolean[] getEmptyDays() {
        return emptyDays;
    }

    public ArrayList<Period> getPeriods() {
        return periods;
    }

    public Map<Integer, String> getSubjectNames() {
        return subjectNames;
    }

    public ArrayList<String> getMessagesOfDay() {
        return messagesOfDay;
    }

    public boolean hasEvents(boolean includeEmptyDays) {
        return !periods.isEmpty() || (Arrays.asList(this.emptyDays).contains(true) && includeEmptyDays);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timetable timetable = (Timetable) o;
        return Arrays.equals(emptyDays, timetable.emptyDays) &&
                Objects.equals(periods, timetable.periods) &&
                Objects.equals(subjectNames, timetable.subjectNames) &&
                Objects.equals(messagesOfDay, timetable.messagesOfDay);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(periods, subjectNames, messagesOfDay);
        result = 31 * result + Arrays.hashCode(emptyDays);
        return result;
    }

    public enum CellState {
        STANDARD, ADDITIONAL, CANCEL, SUBSTITUTION, FREE, ROOMSUBSTITUTION, SHIFT
    }

    public static class Period {
        private CellState cellState;
        private LocalDateTime start;
        private LocalDateTime end;
        private int subject;
        private Optional<String> periodText;
        private Optional<RescheduleInfo> rescheduleInfo;

        Period(CellState cellState, LocalDateTime start, LocalDateTime end, int subject, Optional<String> periodText, Optional<RescheduleInfo> rescheduleInfo) {
            this.cellState = cellState;
            this.start = start;
            this.end = end;
            this.subject = subject;
            this.periodText = periodText;
            this.rescheduleInfo = rescheduleInfo;
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

        public Optional<String> getPeriodText() {
            return periodText;
        }

        public Optional<RescheduleInfo> getRescheduleInfo() {
            return rescheduleInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Period period = (Period) o;
            return subject == period.subject &&
                    cellState == period.cellState &&
                    Objects.equals(start, period.start) &&
                    Objects.equals(end, period.end) &&
                    Objects.equals(periodText, period.periodText) &&
                    Objects.equals(rescheduleInfo, period.rescheduleInfo);
        }

        @Override
        public int hashCode() {

            return Objects.hash(cellState, start, end, subject, periodText, rescheduleInfo);
        }

        public static class RescheduleInfo{
            private LocalDateTime start;
            private LocalDateTime end;
            private boolean isSource;

            RescheduleInfo(LocalDateTime start, LocalDateTime end, boolean isSource) {
                this.start = start;
                this.end = end;
                this.isSource = isSource;
            }

            public LocalDateTime getStart() {
                return start;
            }

            public LocalDateTime getEnd() {
                return end;
            }

            public boolean isSource() {
                return isSource;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                RescheduleInfo that = (RescheduleInfo) o;
                return isSource == that.isSource &&
                        Objects.equals(start, that.start) &&
                        Objects.equals(end, that.end);
            }

            @Override
            public int hashCode() {

                return Objects.hash(start, end, isSource);
            }
        }
    }
}
