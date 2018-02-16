package de.zwemkefa.vpbot;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.time.LocalDateTime;

public class VPBot {

    private static VPBot instance;

    private IDiscordClient client;

    public VPBot(){
        this.client = new ClientBuilder()
                .withToken("!!!Mzk3NDUzMjYxODM1MDc1NTg2.DWgahw.mEIyoZXS6w_EXBLMKgJCFH8UyE4")
                .build();
        client.login();
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        IChannel c = client.getUserByID(0l).getOrCreatePMChannel();

        this.sendMessage(c, "Bin gerade am Testen wie der VPBot bald aussehen soll. Zum Antworten bitte direkt an @Fabian#7090 schreiben");

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        this.sendMessage(c, new EmbedBuilder()
                .withColor(Color.GREEN)
                .withTitle("Vertretungsplan 10c")
                .withDescription("Der Vertretungsplan ist leer.")
                .withTimestamp(LocalDateTime.now())
                .build());

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        this.sendMessage(c, new EmbedBuilder()
                .withColor(Color.YELLOW)
                .withTitle("Vertretungsplan 10c")
                .appendField("Montag", "Geschichte wird von 5:45 bis 19:45 vertreten.", false)
                .withTimestamp(LocalDateTime.now())
                .build());

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        this.sendMessage(c, new EmbedBuilder()
                .withColor(Color.RED)
                .withTitle("Vertretungsplan 10c")
                .withDescription("Ein Fehler ist aufgetreten.")
                .withFooterText("TestException | TestDescription")
                .withTimestamp(LocalDateTime.now())
                .build());
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
