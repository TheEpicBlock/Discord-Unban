package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.events.message.react.MessageReactionAddEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages pending unbans. Listens to reactions
 */
public class ConfirmManager {
    public static final String CANCEL = "U+274c";
    public static final String ACCEPT = "U+2705";
    private final String roleID;
    private DiscordUnban plugin;
    private ConcurrentHashMap<Long, UnbanAttempt> unbanAttempts = new ConcurrentHashMap<>();

    public ConfirmManager(DiscordUnban plugin, String roleid) {
        this.plugin = plugin;
        roleID = roleid;
        initReactionListener();
    }

    public void processReaction(@Nonnull MessageReactionAddEvent event) {
        plugin.debugLog("Received reaction: " + event.getReaction().getReactionEmote().getAsCodepoints());
        if (event.getUser() == null || event.getUser().isBot())
            return; //prevent the bot's own reactions from interfering.

        UnbanAttempt unbanAttempt = unbanAttempts.get(event.getMessageIdLong());
        if (unbanAttempt != null) { //check if this reaction is on a pending unban message
            if (!DiscordUnbanUtils.checkForPerms(event.getMember(), roleID)) {
                //they don't have the perms to confirm an unban.
                event.getReaction().removeReaction(event.getUser()).queue();
                return;
            }

            //see what emoji they've reacted with
            switch (event.getReaction().getReactionEmote().getAsCodepoints()) {
                case ACCEPT:
                    plugin.getBanManager().unban(unbanAttempt.requestedPlayer, unbanAttempt.requesterId);
                    unbanAttempts.remove(event.getMessageIdLong());
                    cleanUpMessage(event, format("unbanSucces", unbanAttempt.requestedPlayer.getName(), event.getUser().getAsMention()));
                    break;
                case CANCEL:
                    unbanAttempts.remove(event.getMessageIdLong());
                    cleanUpMessage(event, format("unbanCancel", unbanAttempt.requestedPlayer.getName(), event.getUser().getAsMention()));
                    break;
                default:
                    event.getReaction().removeReaction(event.getUser()).queue();
            }
        }
    }

    public void addMessageToConfirmQueue(Message message, OfflinePlayer requestedPlayer, UUID requesterId) {
        plugin.debugLog("added message '" + message.getId() + "to the confirm queue");
        UnbanAttempt unbanAttempt = new UnbanAttempt(requestedPlayer, requesterId);
        unbanAttempts.put(message.getIdLong(), unbanAttempt);

        message.addReaction(ACCEPT).queue((useless) -> message.addReaction(CANCEL).queue()); //adds the 2 reactions to the message, making sure the ACCEPT reaction comes first
    }

    private void cleanUpMessage(MessageReactionAddEvent event, String newMessage) {
        event.getTextChannel().editMessageById(event.getMessageIdLong(), newMessage).queue((msg) -> {
            msg.clearReactions().queue();
            //msg.suppressEmbeds(true); not included in the main DiscordSRV yet
        });
    }

    private void initReactionListener() {
        new BukkitRunnable() {
            @Override
            public void run() {
                JDA jda = DiscordSRV.getPlugin().getJda();
                if (jda != null) {
                    if (jda.getStatus() == JDA.Status.CONNECTED) {
                        plugin.getLogger().info("jda is connected, enabling reaction listener");
                        jda.addEventListener(new DiscordEventProcessor.JDAReactionListener(ConfirmManager.this));
                    } else {
                        plugin.getLogger().warning("jda doesn't seem to be connected. Error whilst enabling reaction listener");
                        plugin.getLogger().warning("jda status: " + jda.getStatus().toString());
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 40, 2L);
    }

    public static class UnbanAttempt {
        public OfflinePlayer requestedPlayer;
        public UUID requesterId;

        public UnbanAttempt(OfflinePlayer requestedPlayer, UUID requesterId) {
            this.requestedPlayer = requestedPlayer;
            this.requesterId = requesterId;
        }
    }

    private String format (String key, Object... objects) {
        return plugin.getLangStrings().getFormatted(key, objects);
    }
}
