package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class BanRequestManager {
    private DiscordUnban plugin;


    public BanRequestManager(DiscordUnban plugin) {
        this.plugin = plugin;
    }

    public void parseRequestFromMessage(Message message) {
//        message.delete();
//
//        String playerStr = plugin.stripString(message.getContentRaw(),plugin.getUnbanCommand());
//        OfflinePlayer player = plugin.getPlayerFromTargetted(playerStr);
//        if (player == null) {
//            sendMessage(message, String.format("%s, couldn't find '%s'", message.getAuthor(), playerStr));
//            return;
//        }
//
//        if (plugin.getBanManager().isBanned(player)) {
//            sendMessage(message, String.format("'%s' is already banned, %s", player.getName(), message.getAuthor()));
//            return;
//        }
//
//        UUID requesterID = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(message.getAuthor().getId());
//        OfflinePlayer requester = Bukkit.getOfflinePlayer(requesterID);
//        MessageBuilder messageBuilder = new MessageBuilder();
//
//        if (plugin.getRequireConfirmation()) {
//            messageBuilder.append(String.format("%s wants to unban %s", message.getAuthor(), player.getName()));
//        } else {
//            messageBuilder.append(String.format("%s was unbanned by %s(ign: %s)", player.getName(), message.getAuthor(), requester.getName()));
//            plugin.getBanManager().unban(player, requesterID);
//        }

    }

    public void sendMessage(Message channel, String message) {
        DiscordUtil.sendMessage(channel.getTextChannel(), message);
    }

    public static class unbanRequest {
        public OfflinePlayer toBeUnbanned;
        public String confirmationMessageID;
    }
}
