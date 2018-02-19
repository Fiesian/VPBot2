package de.zwemkefa.vpbot;

import de.zwemkefa.vpbot.cmd.CommandHandler;
import de.zwemkefa.vpbot.io.UntisClassResolver;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class VPBot {

    private static VPBot instance;

    private IDiscordClient client;

    private UntisClassResolver classResolver;

    public VPBot() {

        this.client = new ClientBuilder()
                .withToken("Mzk3NDUzMjYxODM1MDc1NTg2.DWgahw.mEIyoZXS6w_EXBLMKgJCFH8UyE4")
                .build();

        client.getDispatcher().registerListener(new CommandHandler());
        client.login();

        while (!client.isLoggedIn()) {
            Thread.yield();
        }

        //ExceptionHandler defaultExceptionHandler = (e) -> this.sendMessage(client.getUserByID(226978525121478656l).getOrCreatePMChannel(), DiscordFormatter.formatErrorMessage(e));

        this.classResolver = new UntisClassResolver();


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

    public UntisClassResolver getClassResolver() {
        return classResolver;
    }
}
