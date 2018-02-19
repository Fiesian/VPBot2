package de.zwemkefa.vpbot.config;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (id != entry.id) return false;
            return className != null ? className.equals(entry.className) : entry.className == null;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (className != null ? className.hashCode() : 0);
            return result;
        }
    }
}
