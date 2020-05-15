package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class MessageProcessor {
    private final DiscordUnban plugin;

    private final List<String> enabledChannels;
    private final String unbanCommand;
    private final String infoCommand;
    private final String roleId;
    private final DateFormat dateFormat;
    private final boolean showInfoAfterUnban;
    private final boolean requireConfirmation;


    public MessageProcessor(DiscordUnban plugin, FileConfiguration config) {
        this.plugin = plugin;
        enabledChannels = config.getStringList("EnabledChannels");
        unbanCommand = config.getString("UnbanCommand") + ' ';
        infoCommand = config.getString("InfoCommand") + ' ';
        roleId = config.getString("Role");
        dateFormat = new SimpleDateFormat(config.getString("DateFormat"));
        showInfoAfterUnban = config.getBoolean("ShowInfoAfterUnban");
        requireConfirmation = config.getBoolean("RequireConfirmation");
    }

    public void process(Message msg) {
        if (DiscordUnbanUtils.checkChannel(msg,enabledChannels)) {
            if (msg.getContentRaw().startsWith(unbanCommand)) {
                processUnban(msg);
            } else if (msg.getContentRaw().startsWith(infoCommand)) {
                processInfo(msg);
            }
        }
    }

    private void processUnban(Message msg) {
        DiscordUtil.deleteMessage(msg);

        if (!DiscordUnbanUtils.checkForPerms(msg,roleId)) {
            sendReply(msg, String.format("%s, you don't have the perms to unban", msg.getAuthor()));
            return;
        }

        String playerStr = DiscordUnbanUtils.stripString(msg.getContentRaw(),unbanCommand);
        OfflinePlayer requestedPlayer = DiscordUnbanUtils.getPlayerFromTargetted(playerStr);

        if (requestedPlayer == null) {
            sendReply(msg, String.format("%s, couldn't find '%s'", msg.getAuthor(), playerStr));
            return;
        }

        if (!plugin.getBanManager().isBanned(requestedPlayer)) {
            sendReply(msg, String.format("'%s' is not banned, %s", requestedPlayer.getName(), msg.getAuthor()));
            return;
        }

        UUID requesterID = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(msg.getAuthor().getId());
        if (requesterID == null) {
            sendReply(msg, String.format("%s, your account is not linked with a mc account", msg.getAuthor()));
            return;
        }
        OfflinePlayer requester = Bukkit.getOfflinePlayer(requesterID);

        //reply
        MessageBuilder messageBuilder = new MessageBuilder();

        if (requireConfirmation) {
            messageBuilder.append(String.format("%s, do you want to unban %s?", msg.getAuthor(), requestedPlayer.getName()));
        } else {
            messageBuilder.append(String.format("'%s' was unbanned by %s", requestedPlayer.getName(), msg.getAuthor()));
        }

        if (showInfoAfterUnban) {
            messageBuilder.setEmbed(plugin.getBanManager().getBanInfo(requestedPlayer,dateFormat));
        }
        sendReply(msg,messageBuilder.build());

        //unban logic
        if (!requireConfirmation) {
            plugin.getBanManager().unban(requestedPlayer,requesterID); //we don't need confirmation, we'll just unban the guy
        }
    }

    private void processInfo(Message msg) {
        DiscordUtil.deleteMessage(msg);

        if (!DiscordUnbanUtils.checkForPerms(msg,roleId)) {
            sendReply(msg, String.format("%s, you don't have the perms to look up info", msg.getAuthor()));
            return;
        }

        String playerStr = DiscordUnbanUtils.stripString(msg.getContentRaw(),infoCommand);
        OfflinePlayer requestedPlayer = DiscordUnbanUtils.getPlayerFromTargetted(playerStr);

        if (requestedPlayer == null) {
            sendReply(msg, String.format("%s, couldn't find '%s'", msg.getAuthor(), playerStr));
            return;
        }

        //reply with the info
        MessageBuilder messageBuilder = new MessageBuilder();

        String message = String.format("%s, info for '%s'",
                msg.getAuthor(),
                requestedPlayer.getName());

        messageBuilder.append(message);
        messageBuilder.setEmbed(plugin.getBanManager().getBanInfo(requestedPlayer,dateFormat));

        sendReply(msg, messageBuilder.build());
    }

    private void sendReply(Message replyMessage, String message) {
        DiscordUtil.sendMessage(replyMessage.getTextChannel(),message);
    }

    private void sendReply(Message replyMessage, Message message) {
        DiscordUtil.queueMessage(replyMessage.getTextChannel(),message);
    }
}
