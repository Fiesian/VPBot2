package de.zwemkefa.vpbot.config;

import com.google.gson.annotations.Expose;
import de.zwemkefa.vpbot.VPBot;
import de.zwemkefa.vpbot.util.DateHelper;
import sx.blah.discord.handle.obj.IMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class ChannelConfig {

    @Expose
    private HashSet<Entry> channels = new HashSet<>();

    @Expose
    private String token = "";

    @Expose
    private int version_major = 0;

    @Expose
    private int version_minor = 0;

    @Expose
    private int version_patch = 0;

    public HashSet<Entry> getChannels() {
        return channels;
    }

    public String getToken() {
        return token;
    }

    public boolean init() {
        if (this.token == null || Objects.equals(this.token, "")) {
            System.err.println("Please enter your discord token in config.json");
            return false;
        }
        int comp = VPBot.compareVersion(this.version_major, this.version_minor, this.version_patch);
        if (comp < 0) {
            System.err.println("VPBot v" + VPBot.getVersion() + " is older than config.json (v" + version_major + "." + version_minor + (version_patch == 0 ? "" : "." + version_patch) + ")");
            return false;
        } else if (comp > 0) {
            System.out.println("Updating config.json to version " + VPBot.getVersion() + " (old version: " + version_major + "." + version_minor + (version_patch == 0 ? "" : "." + version_patch) + ")");
            this.version_major = VPBot.VERSION_MAJOR;
            this.version_minor = VPBot.VERSION_MINOR;
            this.version_patch = VPBot.VERSION_PATCH;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChannelConfig config = (ChannelConfig) o;

        return channels != null ? channels.equals(config.channels) : config.channels == null;
    }

    @Override
    public int hashCode() {
        return channels != null ? channels.hashCode() : 0;
    }

    public static class Entry {
        @Expose
        private long id;

        @Expose
        private String className;

        @Expose
        private long checkTime = 600L;

        @Expose(serialize = false)
        private long lastMessageId = 0; //OLD

        @Expose
        private ArrayList<Long> lastMessages = new ArrayList<>();

        @Expose
        private int lastMessageHash = 0;

        @Expose
        private int lastCheckWeek = DateHelper.getWeekOfYear(DateHelper.getDate(LocalDateTime.now()).toLocalDate()); //Push on first check even if there are no events

        @Expose
        private int lastCheckYear = DateHelper.getDate(LocalDateTime.now()).getYear();

        public Entry() {
        }

        public Entry(long id, String className) {
            this.id = id;
            this.className = className;
        }

        public long getId() {
            return id;
        }

        public String getClassName() {
            return className;
        }

        public long getCheckTime() {
            return checkTime;
        }

        public ArrayList<Long> getLastMessages() {
            if (lastMessages.isEmpty() && lastMessageId != 0)
                lastMessages.add(lastMessageId);
            return lastMessages;
        }

        public void setLastMessages(ArrayList<IMessage> messages) {
            this.lastMessages.clear();
            messages.forEach(m -> this.lastMessages.add(m.getLongID()));
        }

        public int getLastMessageHash() {
            return lastMessageHash;
        }

        public void setLastMessageHash(int lastMessageHash) {
            this.lastMessageHash = lastMessageHash;
        }

        public int getLastCheckWeek() {
            return lastCheckWeek;
        }

        public void setLastCheckWeek(int lastCheckWeek) {
            this.lastCheckWeek = lastCheckWeek;
        }

        public int getLastCheckYear() {
            return lastCheckYear;
        }

        public void setLastCheckYear(int lastCheckYear) {
            this.lastCheckYear = lastCheckYear;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return id == entry.id &&
                    checkTime == entry.checkTime &&
                    lastMessageHash == entry.lastMessageHash &&
                    lastCheckWeek == entry.lastCheckWeek &&
                    lastCheckYear == entry.lastCheckYear &&
                    Objects.equals(className, entry.className) &&
                    Objects.equals(lastMessages, entry.lastMessages);
        }

        @Override
        public int hashCode() {

            return Objects.hash(id, className, checkTime, lastMessages, lastMessageHash, lastCheckWeek, lastCheckYear);
        }
    }
}
