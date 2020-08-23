package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.events.message.react.MessageReactionAddEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages pending unbans. Listens to reactions
 */
public class ConfirmManager {
    public static final String CANCEL = "U+274c";
    public static final String ACCEPT = "U+2705";
    private final String roleID;
    private final DiscordUnban plugin;
    private final ConcurrentHashMap<Long,UnbanRequest> unbanRequests = new ConcurrentHashMap<>();

    public ConfirmManager(DiscordUnban plugin, String roleid) {
        this.plugin = plugin;
        roleID = roleid;
        initReactionListener();
    }

    public void processReaction(@Nonnull MessageReactionAddEvent event) {
        plugin.debugLog("Received reaction: " + event.getReaction().getReactionEmote().getAsCodepoints());
        if (event.getUser() == null || event.getUser().isBot())
            return; //prevent the bot's own reactions from interfering.

        UnbanRequest request = unbanRequests.get(event.getMessageIdLong());
        if (request != null) { //check if this reaction is on a pending unban message
            OfflinePlayer player = request.requestedPlayer;
            if (!DiscordUnbanUtils.checkForPerms(event.getMember(), roleID)) {
                //they don't have the perms to confirm an unban.
                event.getReaction().removeReaction(event.getUser()).queue();
                return;
            }

            //see what emoji they've reacted with
            switch (event.getReaction().getReactionEmote().getAsCodepoints()) {
                case ACCEPT:
                    plugin.getBanManager().unban(player, DiscordSRV.getPlugin().getAccountLinkManager().getUuid(event.getUser().getId()));
                    unbanRequests.remove(event.getMessageIdLong());
                    cleanUpMessage(event, format("unbanSucces", player.getName(), event.getUser().getAsMention()));
                    break;
                case CANCEL:
                    unbanRequests.remove(event.getMessageIdLong());
                    cleanUpMessage(event, format("unbanCancel", player.getName(), event.getUser().getAsMention()));
                    break;
                default:
                    event.getReaction().removeReaction(event.getUser()).queue();
            }

            //Check if there are lingering unban requests
            if (!unbanRequests.isEmpty()) {
                long oneDayAgo = System.currentTimeMillis() - (1000 * 60 * 60 * 24);
                unbanRequests.forEach((id, requestA) -> {
                    if (requestA.time < oneDayAgo) {
                        TextChannel channel = DiscordSRV.getPlugin().getJda().getTextChannelById(request.channelId);
                        if (channel != null) {
                            cleanUpMessage(channel, id, plugin.getLangStrings().getFormatted("unbanCancel", request.requestedPlayer.getName(), "inactivity"));
                        }
                        unbanRequests.remove(id);
                    }
                });
            }
        }
    }

    /**
     * Cancels all ban requests due to the server reloading
     */
    public void cancelAll() {
        unbanRequests.forEach((messageId, request) -> {
            TextChannel channel = DiscordSRV.getPlugin().getJda().getTextChannelById(request.channelId);
            if (channel != null) {
                Message msg = channel.editMessageById(messageId, plugin.getLangStrings().getFormatted("unbanCancel", request.requestedPlayer.getName(), "the server stopping")).complete();

                msg.clearReactions().queue();
                msg.suppressEmbeds(true).queue();
            }
        });
        unbanRequests.clear();
    }

    public void addMessageToQueue(Message message, OfflinePlayer requestedPlayer) {
        plugin.debugLog("added message '" + message.getId() + "to the confirm queue");

        //Check if there are other requests open for the same player
        if (!unbanRequests.isEmpty()) {
            unbanRequests.forEach((id, request) -> {
                if (request.requestedPlayer == requestedPlayer) {
                    TextChannel channel = DiscordSRV.getPlugin().getJda().getTextChannelById(request.channelId);
                    if (channel != null) {
                        cleanUpMessage(channel, id, plugin.getLangStrings().getFormatted("unbanCancel", request.requestedPlayer.getName(), "another request being created"));
                    }
                    unbanRequests.remove(id);
                }
            });
        }

        UnbanRequest unbanRequest = new UnbanRequest(requestedPlayer, message.getChannel().getIdLong());
        unbanRequests.put(message.getIdLong(), unbanRequest);

        message.addReaction(ACCEPT).queue((useless) -> message.addReaction(CANCEL).queue()); //adds the 2 reactions to the message, making sure the ACCEPT reaction comes first
    }

    /**
     * Removes the embed and all reactions on a message. Then replaces the text with {@code newMessage}
     */
    private void cleanUpMessage(MessageReactionAddEvent event, String newMessage) {
        cleanUpMessage(event.getTextChannel(), event.getMessageIdLong(), newMessage);
    }

    /**
     * Removes the embed and all reactions on a message. Then replaces the text with {@code newMessage}
     */
    private void cleanUpMessage(TextChannel channel, Long id, String newMessage) {
        channel.editMessageById(id, newMessage).queue((msg) -> {
            msg.clearReactions().queue();
            msg.suppressEmbeds(true).queue();
        });
    }

    private void initReactionListener() {
        new BukkitRunnable() {
            @Override
            public void run() {
                JDA jda = DiscordSRV.getPlugin().getJda();
                if (jda != null) {
                    if (jda.getStatus() == JDA.Status.CONNECTED) {
                        plugin.getLogger().info("jda is connected, added reaction listener");
                        jda.addEventListener(new DiscordEventProcessor.JDAReactionListener(ConfirmManager.this));
                    } else {
                        plugin.getLogger().warning("jda doesn't seem to be connected. Can't enable reaction listener");
                        plugin.getLogger().warning("jda status: " + jda.getStatus().toString());
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 40, 2L);
    }

    public static class UnbanRequest {
        public final OfflinePlayer requestedPlayer;
        public final Long channelId;
        public final Long time;

        public UnbanRequest(OfflinePlayer requestedPlayer, Long channelId) {
            this.requestedPlayer = requestedPlayer;
            this.channelId = channelId;
            this.time = System.currentTimeMillis();
        }
    }

    private String format (String key, Object... objects) {
        return plugin.getLangStrings().getFormatted(key, objects);
    }
}
