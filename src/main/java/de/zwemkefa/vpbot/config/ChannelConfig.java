package de.zwemkefa.vpbot.config;

import de.zwemkefa.vpbot.VPBot;

import java.util.HashSet;

public class ChannelConfig {

    private HashSet<Entry> channels = new HashSet<>();

    private String token = "";

    public HashSet<Entry> getChannels() {
        return channels;
    }

    public String getToken() {
        return token;
    }

    private int version_major = 0;
    private int version_minor = 0;
    private int version_patch = 0;

    public boolean init() {
        if (this.token == null || this.token == "") {
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
            VPBot.getInstance().saveConfig();
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
        private long id;
        private String className;
        private long checkTime = 600l;
        private long lastMessageId = 0;
        private int lastMessageHash = 0;

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

        public long getLastMessageId() {
            return lastMessageId;
        }

        public void setLastMessageId(long lastMessageId) {
            this.lastMessageId = lastMessageId;
        }

        public int getLastMessageHash() {
            return lastMessageHash;
        }

        public void setLastMessageHash(int lastMessageHash) {
            this.lastMessageHash = lastMessageHash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (id != entry.id) return false;
            if (checkTime != entry.checkTime) return false;
            return className != null ? className.equals(entry.className) : entry.className == null;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (className != null ? className.hashCode() : 0);
            result = 31 * result + (int) (checkTime ^ (checkTime >>> 32));
            return result;
        }
    }
}
