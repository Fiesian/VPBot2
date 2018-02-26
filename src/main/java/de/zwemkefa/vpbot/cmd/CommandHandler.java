package de.zwemkefa.vpbot.cmd;

import de.zwemkefa.vpbot.VPBot;
import de.zwemkefa.vpbot.io.UntisIOHelper;
import de.zwemkefa.vpbot.timetable.Timetable;
import de.zwemkefa.vpbot.util.DiscordFormatter;
import de.zwemkefa.vpbot.util.ExceptionHandler;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;

public class CommandHandler {
    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        if (e.getChannel().isPrivate()) {
            try {
                String[] cmd = e.getMessage().getContent().split(" ");
                switch (cmd[0]) {
                    case "ping":
                        EmbedBuilder b = new EmbedBuilder();
                        b.withTitle("Pong!");
                        b.withFooterText("°_°");
                        b.withColor(Color.ORANGE);
                        VPBot.getInstance().sendMessage(e.getChannel(), b.build());
                        break;

                    case "vp":
                        ExceptionHandler exceptionHandler = ex -> VPBot.getInstance().sendMessage(e.getChannel(), DiscordFormatter.formatErrorMessage(ex));
                        if (cmd.length < 2) {
                            throw new IllegalArgumentException("Keine Klasse angegeben. \n\"vp <klasse>\"\n\"klassen\" zeigt die Klassen an.");
                        }
                        int classID = VPBot.getInstance().getClassResolver().resolve(cmd[1], exceptionHandler);
                        Timetable t = Timetable.ofRawJSON(UntisIOHelper.getTimetableRaw(classID, exceptionHandler), UntisIOHelper.getNewsRaw(exceptionHandler), exceptionHandler, classID);
                        VPBot.getInstance().sendMessage(e.getChannel(), DiscordFormatter.formatTimetableMessage(t, cmd[1], false));
                        break;

                    case "klassen":
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.withTitle("Klassen");
                        builder.withColor(Color.CYAN);
                        builder.withFooterText("-> vp <klasse>");
                        StringBuilder stringBuilder = new StringBuilder();
                        VPBot.getInstance().getClassResolver().getCachedClasses().stream().sorted().forEachOrdered(s -> stringBuilder.append(s).append(", "));
                        stringBuilder.setLength(stringBuilder.length() - 2);
                        builder.withDescription(stringBuilder.toString());
                        VPBot.getInstance().sendMessage(e.getChannel(), builder.build());
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                VPBot.getInstance().sendMessage(e.getChannel(), DiscordFormatter.formatErrorMessage(ex));
            }
        }
    }
}
