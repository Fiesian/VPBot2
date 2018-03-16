package de.zwemkefa.vpbot.thread;

import de.zwemkefa.vpbot.VPBot;
import de.zwemkefa.vpbot.config.ChannelConfig;
import de.zwemkefa.vpbot.io.UntisIOHelper;
import de.zwemkefa.vpbot.timetable.Timetable;
import de.zwemkefa.vpbot.util.DiscordFormatter;
import de.zwemkefa.vpbot.util.ExceptionHandler;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TimetableWatcherThread extends Thread {
    private ChannelConfig.Entry config;
    private Timetable lastCheck;
    private IMessage lastMessage;
    private IChannel channel;
    private TTWExceptionHandler e;
    private int classId;

    public TimetableWatcherThread(ChannelConfig.Entry config) {
        this.config = config;
        this.channel = VPBot.getInstance().getClient().getChannelByID(config.getId());

        this.e = new TTWExceptionHandler();

        this.classId = VPBot.getInstance().getClassResolver().resolve(this.config.getClassName(), e);

        if (config.getLastMessageId() != 0)
            this.lastMessage = channel.fetchMessage(config.getLastMessageId());

        if (this.classId == 0) {
            return;
        }

        this.start();
    }

    @Override
    public void run() {
        while (true) {
            this.channel.setTypingStatus(true);
            String timetableRaw = UntisIOHelper.getTimetableRaw(this.classId, this.e);
            String newsRaw = UntisIOHelper.getNewsRaw(this.e);
            if (timetableRaw != null && newsRaw != null) {
                Timetable t = Timetable.ofRawJSON(timetableRaw, newsRaw, this.e, this.classId);
                if (t != null && (!t.equals(lastCheck) && this.config.getLastMessageHash() != t.hashCode())) {
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
                            this.e.onMessageSuccess();
                            this.channel.setTypingStatus(false);
                        } catch (Exception ex) {
                            System.err.println("Could not send message: ");
                            ex.printStackTrace();
                            this.e.handleException(ex);
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
                                this.e.onMessageSuccess();
                                this.channel.setTypingStatus(false);
                            } catch (DiscordException ex) {
                                System.err.println("Could not send message: ");
                                ex.printStackTrace();
                                this.e.handleException(ex);
                            }
                        });
                    }
                }
            }
            try {
                Thread.sleep(this.config.getCheckTime() * 1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                Thread.currentThread().interrupt();
            }

        }

    }

    class TTWExceptionHandler implements ExceptionHandler {
        private boolean lastMessageSuccess = true;
        private IMessage errorMessage = null;
        private ArrayList<Exception> exceptions = new ArrayList<>();
        private boolean lastMessageSocket = false;
        private LocalDateTime socketExceptionStart = null;

        public void onMessageSuccess() {
            if (!lastMessageSuccess) {
                lastMessageSuccess = true;
                errorMessage = null;
                exceptions.clear();
                VPBot.getInstance().getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, "VPBot v" + VPBot.getVersion());
            }
            if (lastMessageSocket) {
                lastMessageSocket = false;
                socketExceptionStart = null;
                VPBot.getInstance().getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, "VPBot v" + VPBot.getVersion());
            }
        }

        @Override
        public void handleException(Exception ex) {
            if (lastMessageSuccess) {
                exceptions.add(ex);
                if (ex instanceof SocketException) {
                    if (!lastMessageSocket) {
                        lastMessageSocket = true;
                        socketExceptionStart = LocalDateTime.now();
                    }
                    if (lastMessage == null) {
                        System.out.println("SocketException, but lastMessage == null. Won't do anything.");
                    } else {
                        System.out.println("SocketException: " + ex.getMessage());
                        RequestBuffer.request(() -> {
                            try {
                                lastMessage.edit(DiscordFormatter.formatSocketErrorMessage(socketExceptionStart));
                                channel.setTypingStatus(false);
                            } catch (DiscordException ex4) {
                                System.err.println("Could not edit message: ");
                                this.handleException(ex4);
                            }
                        });
                        VPBot.getInstance().getClient().changePresence(StatusType.IDLE, ActivityType.PLAYING, "VPBot v" + VPBot.getVersion());
                    }
                } else {
                    RequestBuffer.request(() -> {
                        try {
                            this.errorMessage = channel.sendMessage(DiscordFormatter.formatErrorMessage(ex));
                            channel.setTypingStatus(false);
                        } catch (DiscordException ex2) {
                            System.err.println("Could not send error message: ");
                            ex2.printStackTrace();
                            this.exceptions.add(ex2);
                        }
                    });
                    this.exceptions.add(ex);
                    this.lastMessageSuccess = false;
                    VPBot.getInstance().getClient().changePresence(StatusType.DND);
                }
            } else {
                this.exceptions.add(ex);
                if (this.errorMessage == null) {
                    System.out.println("null @ TTWEx#(this.errorMessage == null) Trying to send a new message");
                    RequestBuffer.request(() -> {
                        try {
                            this.errorMessage = channel.sendMessage(DiscordFormatter.formatErrorMessage(exceptions));
                            channel.setTypingStatus(false);
                        } catch (DiscordException ex2) {
                            System.err.println("Could not send error message: ");
                            ex2.printStackTrace();
                            this.exceptions.add(ex2);
                        }
                    });
                } else {
                    RequestBuffer.request(() -> {
                        try {
                            this.errorMessage.edit(DiscordFormatter.formatErrorMessage(exceptions));
                            channel.setTypingStatus(false);
                        } catch (DiscordException ex3) {
                            System.err.println("Could not edit error message: ");
                            ex3.printStackTrace();
                            exceptions.add(ex3);
                        }
                    });
                }
            }
        }
    }
}
