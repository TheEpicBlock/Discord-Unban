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
import java.util.function.Consumer;

/**
 * manages incoming messages and what happens with them
 */
public class MessageProcessor {
    private final DiscordUnban plugin;

    private final List<String> enabledChannels;
    private final String unbanCommand;
    private final String infoCommand;
    private final String roleId;
    private final DateFormat dateFormat;
    private final boolean showInfoAfterUnban;
    private final boolean requireConfirmation;
    private final ConfirmManager confirmManager;


    public MessageProcessor(DiscordUnban plugin, FileConfiguration config, ConfirmManager confirmManager) {
        this.plugin = plugin;
        this.confirmManager = confirmManager;

        //import config
        enabledChannels = config.getStringList("EnabledChannels");
        unbanCommand = config.getString("UnbanCommand") + ' ';
        infoCommand = config.getString("InfoCommand") + ' ';
        roleId = config.getString("Role");
        dateFormat = new SimpleDateFormat(config.getString("DateFormat"));
        showInfoAfterUnban = config.getBoolean("ShowInfoAfterUnban");
        requireConfirmation = config.getBoolean("RequireConfirmation");

        //debug info
        if (enabledChannels.size() > 0) {
            enabledChannels.forEach((channel) -> plugin.debugLog("'" + channel + "' is enabled"));
        } else {
            plugin.getLogger().warning("no channels enabled");
        }
    }

    /**
     * processes a discord message
     * @param msg message to process
     */
    public void process(Message msg) {
        if (DiscordUnbanUtils.checkChannel(msg, enabledChannels)) {
            if (msg.getContentRaw().startsWith(unbanCommand)) {
                processUnban(msg);
            } else if (msg.getContentRaw().startsWith(infoCommand)) {
                processInfo(msg);
            }
        } else {
            plugin.debugLog("a message was sent in a channel that isn't enabled: " + msg.getChannel().getId());
        }
    }

    /**
     * processes an unban command
     * @param msg message to process
     */
    private void processUnban(Message msg) {
        DiscordUtil.deleteMessage(msg);

        if (!DiscordUnbanUtils.checkForPerms(msg, roleId)) { //check perms
            sendReply(msg, String.format("%s, you don't have the perms to unban", msg.getAuthor()));
            return;
        }

        //get player requested
        String playerStr = DiscordUnbanUtils.stripString(msg.getContentRaw(), unbanCommand);
        OfflinePlayer requestedPlayer = DiscordUnbanUtils.getPlayerFromTargetted(playerStr);
        if (requestedPlayer == null) {
            sendReply(msg, String.format("%s, couldn't find '%s'", msg.getAuthor(), playerStr));
            return;
        }

        if (!plugin.getBanManager().isBanned(requestedPlayer)) { //you can't unban someone who isn't banned
            sendReply(msg, String.format("'%s' is not banned, %s", requestedPlayer.getName(), msg.getAuthor()));
            return;
        }

        UUID requesterID = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(msg.getAuthor().getId()); //get the person who is trying to unban someone
        if (requesterID == null) {
            sendReply(msg, String.format("%s, your account is not linked with a mc account", msg.getAuthor()));
            return;
        }

        //reply and take action //////////////////////////////////////////////////////
        MessageBuilder messageBuilder = new MessageBuilder();

        if (showInfoAfterUnban) {
            messageBuilder.setEmbed(plugin.getBanManager().getBanInfo(requestedPlayer, dateFormat));
        }

        if (requireConfirmation) {
            messageBuilder.append(String.format("%s, do you want to unban %s?", msg.getAuthor(), requestedPlayer.getName()));
            sendReply(msg, messageBuilder.build(), (message) -> confirmManager.addMessageToConfirmQueue(message, requestedPlayer, requesterID));
        } else {
            messageBuilder.append(String.format("'%s' was unbanned by %s", requestedPlayer.getName(), msg.getAuthor()));
            plugin.getBanManager().unban(requestedPlayer, requesterID); //directly unban the person
            sendReply(msg, messageBuilder.build());
        }

        //unban logic
        if (requireConfirmation) {
            plugin.debugLog(msg.getId());
        } else {
            plugin.getBanManager().unban(requestedPlayer, requesterID); //we don't need confirmation, we'll just unban the guy
        }
    }

    /**
     * processes an info command
     * @param msg message to process
     */
    private void processInfo(Message msg) {
        DiscordUtil.deleteMessage(msg);

        if (!DiscordUnbanUtils.checkForPerms(msg, roleId)) {
            sendReply(msg, String.format("%s, you don't have the perms to look up info", msg.getAuthor()));
            return;
        }

        String playerStr = DiscordUnbanUtils.stripString(msg.getContentRaw(), infoCommand);
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
        messageBuilder.setEmbed(plugin.getBanManager().getBanInfo(requestedPlayer, dateFormat));

        sendReply(msg, messageBuilder.build());
    }

    private void sendReply(Message replyMessage, String message) {
        DiscordUtil.sendMessage(replyMessage.getTextChannel(), message);
    }

    private void sendReply(Message replyMessage, Message message) {
        DiscordUtil.queueMessage(replyMessage.getTextChannel(), message);
    }

    private void sendReply(Message replyMessage, Message message, Consumer<Message> consumer) {
        DiscordUtil.queueMessage(replyMessage.getTextChannel(), message, consumer);
    }
}
