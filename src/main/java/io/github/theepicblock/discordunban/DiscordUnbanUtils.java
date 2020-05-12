package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public class DiscordUnbanUtils {
    /**
     * Checks if a message is in a list of channels
     * @param msg msg to check
     * @param channelId channels to check for
     * @return true if the message is in the correct channel
     */
    public static boolean checkChannel(Message msg, List<String> channelId) {
        return channelId.contains(msg.getChannel().getId());
    }
    /**
     * checks if a guildmember has a certain role
     * @param guildMember guildmember to check
     * @param role role to check for
     */
    public static boolean checkForPerms(Member guildMember, Role role) {
        return guildMember.getRoles().contains(role);
    }

    /**
     * checks if a guildmember has a certain role
     * @param guildMember guildmember to check
     * @param roleId role to check for
     */
    public static boolean checkForPerms(Member guildMember, String roleId) {
        for (Role role : guildMember.getRoles()) {
            if (role.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks if the owner of a message has a certain role
     * @param msg the msg of someone who we would like to check
     * @param role role to check for
     * @return
     */
    public static boolean checkForPerms(Message msg, Role role) {
        return checkForPerms(msg.getGuild().getMemberById(msg.getAuthor().getIdLong()),role);
    }

    /**
     * checks if the owner of a message has a certain role
     * @param msg the msg of someone who we would like to check
     * @param roleId role to check for
     * @return
     */
    public static boolean checkForPerms(Message msg, String roleId) {
        return checkForPerms(msg.getGuild().getMemberById(msg.getAuthor().getIdLong()),roleId);
    }

    /**
     * Strips a string from the beginning. Eg: "!unban test" - "!unban" = "test"
     * @param string string to strip
     * @param strip what to strip from the string
     * @return stripped string
     */
    public static String stripString(String string, String strip){
        if (string.length() < strip.length()) {
            return "";
        }
        return string.substring(strip.length());
    }

    /**
     * @param Message message to parse. Eg "<@!123456>" or "Notch"
     * @return the offline player matching the string
     */
    public static OfflinePlayer getPlayerFromTargetted(String Message) {
        if (Message == "") {
            return null;
        }

        switch (Message.charAt(0)) {
            case '<':
                //This is a discord mention
                try {
                    String id = Message.substring(3,Message.length()-1); // converts "<@!1234>" to "1234"
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
}
