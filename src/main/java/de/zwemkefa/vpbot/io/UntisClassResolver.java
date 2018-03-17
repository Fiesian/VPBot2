package de.zwemkefa.vpbot.io;

import de.zwemkefa.vpbot.util.DateHelper;
import de.zwemkefa.vpbot.util.ExceptionHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UntisClassResolver {
    private Map<String, Integer> classMap = new HashMap<>();

    private void handleJson(String json) throws JSONException {
        HashMap<String, Integer> map = new HashMap<>();
        JSONArray elements = new JSONObject(json)
                .getJSONObject("data")
                .getJSONArray("elements");
        for (int i = 0; i < elements.length(); i++) {
            JSONObject e = elements.getJSONObject(i);
            if (e.getInt("type") == 1) {
                map.put(e.getString("name"), e.getInt("id"));
            }
        }
        this.classMap = map;
    }

    public int resolve(String className, ExceptionHandler e) {
        try {
            if (this.classMap.containsKey(className)) {
                return this.classMap.get(className);
            } else {
                this.handleJson(UntisIOHelper.getClassesRaw(e, DateHelper.getDate(LocalDateTime.now())));
                if (this.classMap.containsKey(className)) {
                    return this.classMap.get(className);
                } else {
                    throw new IllegalArgumentException("Class \"" + className + "\" could not be resolved");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            e.handleException(ex);
        }
        return 0;
    }

    public Set<String> getCachedClasses() {
        return classMap.keySet();
    }
}
