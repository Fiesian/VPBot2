package de.zwemkefa.vpbot.thread;

import de.zwemkefa.vpbot.VPBot;
import de.zwemkefa.vpbot.config.ChannelConfig;
import de.zwemkefa.vpbot.io.UntisIOHelper;
import de.zwemkefa.vpbot.timetable.Timetable;
import de.zwemkefa.vpbot.util.DiscordFormatter;
import de.zwemkefa.vpbot.util.ExceptionHandler;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class TimetableWatcherThread extends Thread {
    private ChannelConfig.Entry config;
    private Timetable lastCheck;
    private IMessage lastMessage;
    private IChannel channel;
    private ExceptionHandler e;
    private int classId;

    public TimetableWatcherThread(ChannelConfig.Entry config) {
        this.config = config;
        this.channel = VPBot.getInstance().getClient().getChannelByID(config.getId());

        this.e = (ex) -> VPBot.getInstance().sendMessage(this.channel, DiscordFormatter.formatErrorMessage(ex));

        this.classId = VPBot.getInstance().getClassResolver().resolve(this.config.getClassName(), e);

        if (config.getLastMessageId() != 0)
            this.lastMessage = channel.getMessageByID(config.getLastMessageId());

        if (this.classId == 0) {
            return;
        }

        this.start();
    }

    @Override
    public void run() {
        while (true) {
            Timetable t = Timetable.ofRawJSON(UntisIOHelper.getTimetableRaw(this.classId, this.e), UntisIOHelper.getNewsRaw(this.e), this.e, classId);
            if (t != null && (!t.equals(lastCheck) || (this.lastCheck == null && this.config.getLastMessageHash() != t.hashCode()))) {
                this.lastCheck = t;
                RequestBuffer.request(() -> {
                    try {
                        if (this.lastMessage != null) {
                            this.lastMessage.delete();
                        }
                        this.lastMessage = channel.sendMessage(DiscordFormatter.formatTimetableMessage(t, this.config.getClassName(), true));
                        this.config.setLastMessageId(this.lastMessage.getLongID());
                        this.config.setLastMessageHash(t.hashCode());
                        VPBot.getInstance().saveConfig();
                    } catch (DiscordException e) {
                        System.err.println("Could not send message: ");
                        e.printStackTrace();
                    }
                });
            } else {
                if (this.lastMessage == null) {
                    System.err.println("null @ TTW#(this.lastMessage == null). Won't edit lastMessage");
                } else if (t == null) {
                    System.err.println("null @ TTW#(t == null). Won't edit lastMessage");
                } else {
                    RequestBuffer.request(() -> {
                        try {
                            this.lastMessage.edit(DiscordFormatter.formatTimetableMessage(t, this.config.getClassName(), true));
                        } catch (DiscordException e) {
                            System.err.println("Could not send message: ");
                            e.printStackTrace();
                        }
                    });
                }
            }
            try {
                Thread.sleep(this.config.getCheckTime() * 1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                this.e.handleException(ex);
            }
        }
    }
}
