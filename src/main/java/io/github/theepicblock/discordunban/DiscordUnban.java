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
    private BanRequestManager requestManager;
    private MessageProcessor messageProcessor;

    private List<String> enabledChannels;
    private String unbanCommand;
    private String infoCommand;
    private String roleId;
    private Role role;
    private String dateFormat;
    private boolean showInfoAfterUnban;
    private boolean requireConfirmation;


    @Override
    public void onEnable() {
        //load config
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        enabledChannels = config.getStringList("EnabledChannels");
        unbanCommand = config.getString("UnbanCommand") + ' ';
        infoCommand = config.getString("InfoCommand") + ' ';
        roleId = config.getString("Role");
        dateFormat = config.getString("DateFormat");
        showInfoAfterUnban = config.getBoolean("ShowInfoAfterUnban");
        requireConfirmation = config.getBoolean("RequireConfirmation");

        //load processors
        messageProcessor = new MessageProcessor(this, enabledChannels, unbanCommand, infoCommand, roleId, dateFormat,showInfoAfterUnban,requireConfirmation);
        discordEventProcessor = new DiscordEventProcessor(messageProcessor);
        requestManager = new BanRequestManager(this);

        //get correct banmanager depending on enabled plugins
        if (getServer().getPluginManager().getPlugin("DKBans")!=null){  //dkbans is installed
            getLogger().info("Enabled DKBans integration");
            banManager = new DKBansBanManager();
        } else {
            banManager = new VanillaBanManager(this);
        }

        //subscribe to discord events
        DiscordSRV.api.subscribe(discordEventProcessor);
    }

    public BanManager getBanManager() {
        return banManager;
    }



    @Override
    public void onDisable() {
        DiscordSRV.api.unsubscribe(discordEventProcessor);
    }
}
