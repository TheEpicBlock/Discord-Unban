package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import io.github.theepicblock.discordunban.banmanagement.BanManager;
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

    private List<String> enabledChannels;
    private String command;
    private String roleId;
    private Role role;

    @Override
    public void onEnable() {
        //load config
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        enabledChannels = config.getStringList("EnabledChannels");
        command = config.getString("Command") + ' ';
        roleId = config.getString("Role");
        discordEventProcessor = new DiscordEventProcessor(this);

        banManager = new VanillaBanManager(this);

        DiscordSRV.api.subscribe(discordEventProcessor);
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public boolean checkForCommand(Message msg) {
        if (role == null) {
            role = DiscordSRV.getPlugin().getJda().getRoleById(roleId); //the role hasn't been initialized yet
        }

        //series of checks to perform if the message is valid
        if (!enabledChannels.contains(msg.getChannel().getId())) {
            getLogger().info("wrong channel");
            return false; //the message isn't in the right channel. Since it isn't in the enabledChannels list
        }
        if (!msg.getContentRaw().startsWith(command)) {
            getLogger().info("wrong command");
            return false; //the message doesn't start with the command
        }
        if (!msg.getGuild().getMember(msg.getAuthor()).getRoles().contains(role)) {
            getLogger().info("wrong perms");
            return false;
        }

        return true;
    }

    /**
     * Strips the command from the beginning. Eg: "!unban test" -> "test"
     * @param string string to strip
     * @return stripped string
     */
    public String stripCommand(String string){
        if (string.length() < command.length()) {
            return null;
        }
        return string.substring(command.length());
    }

    public OfflinePlayer getPlayerFromTargetted(String Message) {
        System.out.println("MEHMEHMEH: '" + Message + "'");

        if (Message == "") {
            return null;
        }

        switch (Message.charAt(0)) {
            case '<':
                //This is a discord mention
                try {
                    String id = Message.substring(3,Message.length()-1); //  "<@!1234>" -> "1234"
                    UUID playerId = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(id);
                    return  Bukkit.getOfflinePlayer(playerId);
                } catch (Exception e) {
                    return null;
                }
            default:
                //this is probably a playername
                OfflinePlayer player = Bukkit.getOfflinePlayer(Message);
                if (!player.hasPlayedBefore()) { //this is most likely an invalid name
                    return null;
                }
                return player;
        }
    }

    @Override
    public void onDisable() {
        DiscordSRV.api.unsubscribe(discordEventProcessor);
    }
}
