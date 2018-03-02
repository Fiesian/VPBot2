package de.zwemkefa.vpbot;

import com.google.gson.Gson;
import de.zwemkefa.vpbot.cmd.CommandHandler;
import de.zwemkefa.vpbot.config.ChannelConfig;
import de.zwemkefa.vpbot.io.UntisClassResolver;
import de.zwemkefa.vpbot.io.UntisIOHelper;
import de.zwemkefa.vpbot.thread.TimetableWatcherThread;
import de.zwemkefa.vpbot.timetable.PeriodResolver;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VPBot {

    private static VPBot instance;

    private IDiscordClient client;

    private UntisClassResolver classResolver;

    private Gson gson;

    private ChannelConfig channelConfig;

    private PeriodResolver periodResolver;

    private static final Path CONFIG_PATH = Paths.get("config.json");

    public static final int VERSION_MAJOR = 2;
    public static final int VERSION_MINOR = 2;
    public static final int VERSION_PATCH = 0;

    public VPBot() {
        VPBot.instance = this;
        System.out.println("Starting VPBot v" + VPBot.getVersion());
        this.gson = new Gson();

        if (!Files.exists(CONFIG_PATH)) {
            this.channelConfig = new ChannelConfig();
            this.saveConfig();
        } else {
            try {
                byte[] config = Files.readAllBytes(CONFIG_PATH);
                this.channelConfig = this.gson.fromJson(new String(config, Charset.defaultCharset()), ChannelConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
                this.channelConfig = new ChannelConfig();
            }
        }

        if (!this.channelConfig.init()) {
            System.err.println("Shutting down.");
            return;
        }
        this.client = new ClientBuilder()
                .withToken(this.channelConfig.getToken())
                .build();

        client.getDispatcher().registerListener(new CommandHandler());
        client.login();

        while (!client.isLoggedIn()) {
            Thread.yield();
        }

        this.classResolver = new UntisClassResolver();
        this.periodResolver = new PeriodResolver(UntisIOHelper.getPeriodsRaw(Exception::printStackTrace));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.changePlayingText("VPBot v" + VPBot.getVersion());
        this.channelConfig.getChannels().forEach(e -> new TimetableWatcherThread(e));
    }

    public IDiscordClient getClient() {
        return client;
    }

    public static VPBot getInstance() {
        return instance;
    }

    public PeriodResolver getPeriodResolver() {
        return periodResolver;
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

    public void saveConfig() {
        try {
            Files.write(CONFIG_PATH, this.gson.toJson(this.channelConfig).getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UntisClassResolver getClassResolver() {
        return classResolver;
    }

    public Gson getGson() {
        return gson;
    }

    public static String getVersion() {
        return VERSION_MAJOR + "." + VERSION_MINOR + (VERSION_PATCH == 0 ? "" : "." + VERSION_PATCH);
    }

    public static int compareVersion(int major, int minor, int patch) {
        return Integer.compare(VPBot.VERSION_MAJOR, major) == 0 ? (Integer.compare(VPBot.VERSION_MINOR, minor) == 0 ? Integer.compare(VPBot.VERSION_PATCH, patch) : Integer.compare(VPBot.VERSION_MAJOR, minor)) : Integer.compare(VPBot.VERSION_MAJOR, major);
    }
}
