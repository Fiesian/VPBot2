package de.zwemkefa.vpbot;

import de.zwemkefa.vpbot.io.UntisIOHelper;
import de.zwemkefa.vpbot.timetable.Timetable;
import de.zwemkefa.vpbot.util.DiscordFormatter;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.util.Optional;

public class VPBot {

    private static VPBot instance;

    private IDiscordClient client;

    private UntisIOHelper ioHelper;

    public VPBot() {

        this.client = new ClientBuilder()
                .withToken("Mzk3NDUzMjYxODM1MDc1NTg2.DWgahw.mEIyoZXS6w_EXBLMKgJCFH8UyE4")
                .build();
        client.login();

        this.ioHelper = new UntisIOHelper();

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Timetable t = Timetable.ofRawJSON(ioHelper.getTimetableRaw(123, Optional.of(Exception::printStackTrace)), Optional.of(Exception::printStackTrace));
        this.sendMessage(client.getChannelByID(260177015888412673l), DiscordFormatter.formatTimetableMessage(t, "10c"));
    }

    public static VPBot getInstance() {
        return instance;
    }

    public static void main(String args[]) {
        instance = new VPBot();
    }

    public void sendMessage(IChannel channel, String message) {
        RequestBuffer.request(() -> {
            try {
                channel.sendMessage(message);
            } catch (DiscordException e) {
                System.err.println("Could not send message: ");
                e.printStackTrace();
            }
        });
    }

    public void sendMessage(IChannel channel, EmbedObject message) {
        RequestBuffer.request(() -> {
            try {
                channel.sendMessage(message);
            } catch (DiscordException e) {
                System.err.println("Could not send message: ");
                e.printStackTrace();
            }
        });
    }
}
