package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.theepicblock.discordunban.banmanagement.BanManager;
import io.github.theepicblock.discordunban.banmanagement.DKBansBanManager;
import io.github.theepicblock.discordunban.banmanagement.LiteBansBanManager;
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
        onLoad();
        //subscribe to discord events
        DiscordSRV.api.subscribe(discordEventProcessor);
    }

    public void onLoad() {
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
        } else if (isEnabled("LiteBans")) {
            banManager = new LiteBansBanManager();
        } else {
            banManager = new VanillaBanManager(this);
        }
        debugLog("Banmanager is: " + banManager.getClass().getSimpleName());
    }
    
    public void reload() {
        debugLog("reloading config and lang.yml");
        reloadConfig();
        onLoad();
    }

    private void loadLangstrings() {
        debugLog("loading lang strings");
        File langFile = new File(getDataFolder(),"lang.yml");

        if (!langFile.exists()) {
            this.saveResource("lang.yml",false); //copies the file from resources if it doesn't exist
        }

        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
        langStrings = new LangStrings(langConfig);
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
