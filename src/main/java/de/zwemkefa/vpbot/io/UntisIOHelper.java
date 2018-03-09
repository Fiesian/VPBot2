package de.zwemkefa.vpbot.io;

import de.zwemkefa.vpbot.util.ExceptionHandler;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UntisIOHelper {
    private static final String HOST = "mese.webuntis.com";
    private static final String SCHOOLNAME = "_a3NoLXN0LiBhbnNnYXI=";
    private static final byte[] PERIOD_POST_DATA = "{\"id\":3,\"method\":\"getTimegrid\",\"params\":[3],\"jsonrpc\":\"2.0\"}".getBytes(StandardCharsets.UTF_8);

    public static String getTimetableRaw(int classID, ExceptionHandler exceptionHandler) {
        try {
            HttpsURLConnection con = (HttpsURLConnection) new URL("https", UntisIOHelper.HOST, "/WebUntis/api/public/timetable/weekly/data?elementType=1&elementId=" + classID + "&date=" + UntisIOHelper.getDateString(true) + "&formatId=1").openConnection();
            con.setRequestProperty("Cookie", "schoolname=" + UntisIOHelper.SCHOOLNAME);
            con.connect();
            StringBuilder answer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                answer.append(line);
            }
            reader.close();

            return answer.toString();
        } catch (Exception e) {
            exceptionHandler.handleException(e);
            e.printStackTrace();
        }

        return null;
    }

    public static String getClassesRaw(ExceptionHandler exceptionHandler) {
        try {
            HttpsURLConnection con = (HttpsURLConnection) new URL("https", UntisIOHelper.HOST, "/WebUntis/api/public/timetable/weekly/pageconfig?type=1&id=123&date=" + UntisIOHelper.getDateString(true) + "&formatId=1").openConnection();
            con.setRequestProperty("Cookie", "schoolname=" + UntisIOHelper.SCHOOLNAME);
            con.connect();
            StringBuilder answer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                answer.append(line);
            }
            reader.close();

            return answer.toString();
        } catch (Exception e) {
            exceptionHandler.handleException(e);
            e.printStackTrace();
        }

        return null;
    }

    public static String getNewsRaw(ExceptionHandler exceptionHandler) {
        try {
            HttpsURLConnection con = (HttpsURLConnection) new URL("https", UntisIOHelper.HOST, "/WebUntis/api/public/news/newsWidgetData?date=" + UntisIOHelper.getDateString(false)).openConnection();
            con.setRequestProperty("Cookie", "schoolname=" + UntisIOHelper.SCHOOLNAME);
            con.connect();
            StringBuilder answer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                answer.append(line);
            }
            reader.close();

            return answer.toString();
        } catch (Exception e) {
            exceptionHandler.handleException(e);
            e.printStackTrace();
        }

        return null;
    }

    public static String getPeriodsRaw(ExceptionHandler exceptionHandler) {
        try {


            HttpsURLConnection con = (HttpsURLConnection) new URL("https", UntisIOHelper.HOST, "/WebUntis/jsonrpc_web/jsonTimegridService").openConnection();
            con.setRequestProperty("Cookie", "schoolname=" + UntisIOHelper.SCHOOLNAME);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(UntisIOHelper.PERIOD_POST_DATA.length));
            new DataOutputStream(con.getOutputStream()).write(UntisIOHelper.PERIOD_POST_DATA);
            con.connect();
            StringBuilder answer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                answer.append(line);
            }
            reader.close();

            return answer.toString();
        } catch (Exception e) {
            exceptionHandler.handleException(e);
            e.printStackTrace();
        }

        return null;
    }

    private static String getDateString(boolean includeHyphen) {
        LocalDateTime d = LocalDateTime.now();
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

        return includeHyphen ? DateTimeFormatter.ISO_LOCAL_DATE.format(d) : DateTimeFormatter.BASIC_ISO_DATE.format(d);
    }
}
