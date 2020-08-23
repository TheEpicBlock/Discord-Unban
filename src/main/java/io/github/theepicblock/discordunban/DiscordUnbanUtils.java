package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public class DiscordUnbanUtils {
    /**
     * Checks if a message is in a list of channels
     *
     * @param msg       msg to check
     * @param channelId channels to check for
     * @return true if the message is in the correct channel
     */
    public static boolean checkChannel(Message msg, List<String> channelId) {
        if (channelId.get(0).equals("*")) return true;
        return channelId.contains(msg.getChannel().getId());
    }

    /**
     * Checks if a channel is in a list of channels
     *
     * @param channel   channel to check
     * @param channelId channels to check for
     * @return true if the message is in the correct channel
     */
    public static boolean checkChannel(TextChannel channel, List<String> channelId) {
        if (channelId.get(0).equals("*")) return true;
        return channelId.contains(channel.getId());
    }

    /**
     * checks if a guildmember has a certain role
     *
     * @param guildMember guildmember to check
     * @param role        role to check for
     */
    public static boolean checkForPerms(Member guildMember, Role role) {
        return guildMember.getRoles().contains(role);
    }

    /**
     * checks if a guildmember has a certain role
     *
     * @param guildMember guildmember to check
     * @param roleId      role to check for
     */
    public static boolean checkForPerms(Member guildMember, String roleId) {
        if (guildMember == null) return false;

        for (Role role : guildMember.getRoles()) {
            if (role.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks if the owner of a message has a certain role
     *
     * @param msg    the msg of someone who we would like to check
     * @param roleId role to check for
     * @return true if the user has the role
     */
    public static boolean checkForPerms(Message msg, String roleId) {
        return checkForPerms(msg.getMember(), roleId);
    }

    /**
     * Strips a string from the beginning. Eg: "!unban test" - "!unban" = "test"
     *
     * @param string string to strip
     * @param strip  what to strip from the string
     * @return stripped string
     */
    public static String stripString(String string, String strip) {
        if (string.length() < strip.length()) {
            return "";
        }
        return string.substring(strip.length());
    }

    public static String[] getArgsFromCommand(String message, String command) {
        String args = stripString(message, command).trim();
        if (args.equals("")) {
            return null;
        }
        return args.split(" ");
    }

    /**
     * @param Message message to parse. Eg "<@!123456>", "123456" or "Notch"
     * @return the offline player matching the string
     */
    public static OfflinePlayer getPlayerFromTargetted(String Message) {
        if (Message.equals("")) {
            return null;
        }

        if (Message.charAt(0) == '<') {//This is a discord mention
            try {
                String id = Message.substring(3, Message.length() - 1); // converts "<@!1234>" to "1234"
                UUID playerId = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(id);
                return Bukkit.getOfflinePlayer(playerId);
            } catch (Exception e) {
                return null;
            }
        }

        if (Character.isDigit(Message.charAt(0))) {
            try {
                UUID playerId = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(Message);
                return Bukkit.getOfflinePlayer(playerId);
            } catch (Exception e) {
                return null;
            }
        }

        //this is probably a playername
        OfflinePlayer player = Bukkit.getOfflinePlayer(Message);
        if (!player.hasPlayedBefore()) { //this is most likely an invalid name
            return null;
        }
        return player;
    }
}
