package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.theepicblock.discordunban.banmanagement.BanManager;
import io.github.theepicblock.discordunban.banmanagement.DKBansBanManager;
import io.github.theepicblock.discordunban.banmanagement.VanillaBanManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DiscordUnban extends JavaPlugin {
    private DiscordEventProcessor discordEventProcessor;
    private BanManager banManager;
    private MessageProcessor messageProcessor;
    private ConfirmManager confirmManager;
    private LangStrings langStrings;

    private boolean debug;

    @Override
    public void onEnable() {
        //load config
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        debug = config.getBoolean("Debug");
        debugLog("Debug logs are enabled");

        //load processors
        if (config.getBoolean("RequireConfirmation"))
            confirmManager = new ConfirmManager(this, config.getString("Role"));
        messageProcessor = new MessageProcessor(this, config, confirmManager);
        discordEventProcessor = new DiscordEventProcessor(messageProcessor, this);
        loadLangstrings();

        //get correct banmanager depending on enabled plugins
        if (isEnabled("DKBans")) {  //dkbans is installed
            banManager = new DKBansBanManager();
        } else {
            banManager = new VanillaBanManager(this);
        }
        debugLog("Banmanager is: " + banManager.getClass().getSimpleName());

        //subscribe to discord events
        DiscordSRV.api.subscribe(discordEventProcessor);
    }
    
    public void reload() {
        FileConfiguration oldConfig = this.getConfig();
        reloadConfig();
        FileConfiguration config = this.getConfig();

        debug = config.getBoolean("Debug");

        debugLog("reloading config and lang.yml");

        if (config.getBoolean("RequireConfirmation") && !oldConfig.getBoolean("RequireConfirmation")) {
            //require confirm was previously not enabled, but now is
            getLogger().info("requireConfirmation changed. May or may not cause errors");
            confirmManager = new ConfirmManager(this, config.getString("Role"));
        }

        this.messageProcessor = new MessageProcessor(this, this.getConfig(), confirmManager);
        this.discordEventProcessor.setMessageProcessor(messageProcessor);
        loadLangstrings();
    }

    private void loadLangstrings() {
        debugLog("loading lang strings");
        File langFile = new File(getDataFolder(),"lang.yml");

        if (!langFile.exists()) {
            this.saveResource("lang.yml",false); //copies the file from resources if it doesn't exist
        }

        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
        langStrings = new LangStrings(langConfig, getLogger());
    }

    private boolean isEnabled (String plugin) {
        return getServer().getPluginManager().getPlugin(plugin) != null;
    }

    public void debugLog(String message) {
        if (debug) {
            getLogger().info("[debug] " + message);
        }
    }

    @Override
    public void onDisable() {
        DiscordSRV.api.unsubscribe(discordEventProcessor);
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public LangStrings getLangStrings() {
        return langStrings;
    }
}
