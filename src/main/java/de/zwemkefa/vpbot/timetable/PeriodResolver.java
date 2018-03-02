package de.zwemkefa.vpbot.timetable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.HashMap;

public class PeriodResolver {

    private HashMap<Integer, String> map = new HashMap<>();

    public PeriodResolver(String rawJson) {
        JSONArray json = new JSONObject(rawJson)
                .getJSONObject("result")
                .getJSONArray("units");
        for (int i = 0; i < json.length(); i++) {
            JSONObject unit = json.getJSONObject(i);
            this.map.put(unit.getInt("startTime") * 2400 + unit.getInt("endTime"), unit.getString("name"));
        }
    }

    public String getPeriod(LocalDateTime start, LocalDateTime end) {
        return this.map.get(((start.getHour() * 100) + start.getMinute()) * 2400 + end.getHour() * 100 + end.getMinute());
    }
}
