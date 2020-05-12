package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class MessageProcessor {
    private DiscordUnban plugin;

    private List<String> enabledChannels;
    private String unbanCommand;
    private String infoCommand;
    private String roleId;
    private String dateFormat;
    private boolean showInfoAfterUnban;
    private boolean requireConfirmation;


    public MessageProcessor(DiscordUnban plugin, List<String> enabledChannels, String unbanCommand, String infoCommand,
                            String roleId, String dateFormat, boolean showInfoAfterUnban, boolean requireConfirmation) {
        this.plugin = plugin;
        this.enabledChannels = enabledChannels;
        this.unbanCommand = unbanCommand;
        this.infoCommand = infoCommand;
        this.roleId = roleId;
        this.dateFormat = dateFormat;
        this.showInfoAfterUnban = showInfoAfterUnban;
        this.requireConfirmation = requireConfirmation;
    }

    public void process(Message msg) {
        if (DiscordUnbanUtils.checkChannel(msg,enabledChannels)) {
            if (msg.getContentRaw().startsWith(unbanCommand)) {

            } else if (msg.getContentRaw().startsWith(infoCommand)) {
                processInfo(msg);
            }
        }
    }

    private void processUnban(Message msg) {
        DiscordUtil.deleteMessage(msg);

        if (!DiscordUnbanUtils.checkForPerms(msg,roleId)) {
            sendReply(msg, String.format("%s, you don't have the perms unban", msg.getAuthor()));
            return;
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

        if (!plugin.getBanManager().isBanned(requestedPlayer)) {
            sendReply(msg, String.format("'%s' is not banned, %s", requestedPlayer.getName(), msg.getAuthor()));
            return;
        }

        //reply with the info
        MessageBuilder messageBuilder = new MessageBuilder();

        String message = String.format("%s, info for %s",
                msg.getAuthor(),
                requestedPlayer.getName());

        messageBuilder.append(message);
        messageBuilder.setEmbed(plugin.getBanManager().getBanReason(requestedPlayer,dateFormat));

        sendReply(msg, messageBuilder.build());
    }

    private void sendReply(Message replyMessage, String message) {
        DiscordUtil.sendMessage(replyMessage.getTextChannel(),message);
    }

    private void sendReply(Message replyMessage, Message message) {
        DiscordUtil.queueMessage(replyMessage.getTextChannel(),message);
    }
}
