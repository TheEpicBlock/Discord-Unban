package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.*;
import github.scarsz.discordsrv.util.DiscordUtil;
import io.github.theepicblock.discordunban.banmanagement.BanManager;
import io.github.theepicblock.discordunban.banmanagement.DKBansBanManager;
import io.github.theepicblock.discordunban.banmanagement.VanillaBanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class DiscordUnban extends JavaPlugin {
    private DiscordEventProcessor discordEventProcessor;
    private BanManager banManager;
    private MessageProcessor messageProcessor;

    private boolean debug;

    @Override
    public void onEnable() {
        //load config
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        debug = config.getBoolean("Debug");
        debugLog("Debug logs are enabled");

        //load processors
        messageProcessor = new MessageProcessor(this, config);
        discordEventProcessor = new DiscordEventProcessor(messageProcessor);

        //get correct banmanager depending on enabled plugins
        if (getServer().getPluginManager().getPlugin("DKBans") != null) {  //dkbans is installed
            getLogger().info("Enabled DKBans integration");
            banManager = new DKBansBanManager();
        } else {
            banManager = new VanillaBanManager(this);
        }

        //subscribe to discord events
        DiscordSRV.api.subscribe(discordEventProcessor);
    }

    public void debugLog (String message) {
        if (debug) {
            getLogger().info(message + " [Debug]");
        }
    }

    @Override
    public void onDisable() {
        DiscordSRV.api.unsubscribe(discordEventProcessor);
    }

    public BanManager getBanManager() {
        return banManager;
    }
}
