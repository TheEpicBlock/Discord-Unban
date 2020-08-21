package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
    private final boolean headInInfo;


    public MessageProcessor(DiscordUnban plugin, FileConfiguration config, ConfirmManager confirmManager) {
        this.plugin = plugin;
        this.confirmManager = confirmManager;

        //import config
        enabledChannels     = config.getStringList("EnabledChannels");
        unbanCommand        = config.getString("UnbanCommand");
        infoCommand         = config.getString("InfoCommand");
        roleId              = config.getString("Role");
        dateFormat          = new SimpleDateFormat(Objects.requireNonNull(config.getString("DateFormat")));
        showInfoAfterUnban  = config.getBoolean("ShowInfoAfterUnban");
        requireConfirmation = config.getBoolean("RequireConfirmation");
        headInInfo          = config.getBoolean("HeadInInfo");

        //debug info
        if (enabledChannels.size() > 0) {
            enabledChannels.forEach((channel) -> plugin.debugLog("channel '" + channel + "' is enabled"));
        } else {
            plugin.getLogger().warning("no channels enabled! Discord-Unban won't react to anything!");
        }
    }

    /**
     * processes a discord message
     *
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
     *
     * @param msg message to process
     */
    private void processUnban(Message msg) {
        DiscordUtil.deleteMessage(msg);

        if (!DiscordUnbanUtils.checkForPerms(msg, roleId)) { //check perms
            sendReply(msg, format("noPermUnban", msg.getAuthor().getAsMention()));
            return;
        }

        String[] args = DiscordUnbanUtils.getArgsFromCommand(msg.getContentRaw(), unbanCommand);

        if (args == null) {
            sendReply(msg, format("moreArgs", msg.getAuthor().getAsMention()));
            return;
        }

        //get player requested
        String playerStr = args[0]; //the player is the first argument, the rest is ignored
        OfflinePlayer requestedPlayer = DiscordUnbanUtils.getPlayerFromTargetted(playerStr);
        if (requestedPlayer == null) {
            sendReply(msg, format("cantFind", msg.getAuthor().getAsMention(), playerStr));
            return;
        }

        if (!plugin.getBanManager().isBanned(requestedPlayer)) { //you can't unban someone who isn't banned
            sendReply(msg, format("notBanned", msg.getAuthor().getAsMention(), requestedPlayer.getName()));
            return;
        }

        UUID requesterID = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(msg.getAuthor().getId()); //get the person who is trying to unban someone
        if (requesterID == null) {
            sendReply(msg, format("notLinked", msg.getAuthor().getAsMention()));
            return;
        }

        //reply and take action //////////////////////////////////////////////////////
        MessageBuilder messageBuilder = new MessageBuilder();

        if (showInfoAfterUnban) {
            EmbedBuilder infoEmbed = plugin.getBanManager().getBanInfo(requestedPlayer, dateFormat, null);
            messageBuilder.setEmbed(infoEmbed.build());
        }

        if (requireConfirmation) {
            messageBuilder.append(format("unbanAsk", msg.getAuthor().getAsMention(), requestedPlayer.getName()));
            sendReply(msg, messageBuilder.build(), (message) -> confirmManager.addMessageToConfirmQueue(message, requestedPlayer, requesterID));
        } else {
            messageBuilder.append(format("unbanSucces", requestedPlayer.getName(), msg.getAuthor().getAsMention()));
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
     *
     * @param msg message to process
     */
    private void processInfo(Message msg) {
        DiscordUtil.deleteMessage(msg);

        if (!DiscordUnbanUtils.checkForPerms(msg, roleId)) {
            sendReply(msg, format("noPermInfo", msg.getAuthor().getAsMention()));
            return;
        }

        String[] args = DiscordUnbanUtils.getArgsFromCommand(msg.getContentRaw(), infoCommand);

        if (args == null) {
            sendReply(msg, format("moreArgs"));
            return;
        }

        //get the player
        String playerStr = args[0]; //player is the first argument
        OfflinePlayer requestedPlayer = DiscordUnbanUtils.getPlayerFromTargetted(playerStr);

        if (requestedPlayer == null) {
            sendReply(msg, format("cantFind", msg.getAuthor().getAsMention(), playerStr));
            return;
        }

        //get the argument which need to be passed to the getBanInfo command
        String[] passedArgs = null;
        if (args.length > 1) {
            passedArgs = Arrays.copyOfRange(args, 1, args.length); //get all but the first argument
        }

        //reply with the info
        MessageBuilder messageBuilder = new MessageBuilder();

        String message = format("infoSucces", msg.getAuthor().getAsMention(), requestedPlayer.getName());
        messageBuilder.append(message);

        EmbedBuilder infoEmbed = plugin.getBanManager().getBanInfo(requestedPlayer, dateFormat, passedArgs);
        if (headInInfo) infoEmbed.setThumbnail(DiscordSRV.getPlugin().getEmbedAvatarUrl(requestedPlayer.getName(),requestedPlayer.getUniqueId()));
        messageBuilder.setEmbed(infoEmbed.build());


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

    private String format (String key, Object... objects) {
        return plugin.getLangStrings().getFormatted(key, objects);
    }
}
