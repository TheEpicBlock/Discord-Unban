package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.message.react.MessageReactionAddEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

/**
 * Listens for discord events
 */
public class DiscordEventProcessor {
    private MessageProcessor messageProcessor;

    public DiscordEventProcessor(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void discordMessageReceived(DiscordGuildMessageReceivedEvent event) {
        messageProcessor.process(event.getMessage());
    }

    public static class JDAReactionListener extends ListenerAdapter {
        ConfirmManager confirmManager;

        public JDAReactionListener(ConfirmManager confirmManager) {
            this.confirmManager = confirmManager;
        }

        @Override
        public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
            confirmManager.processReaction(event);
        }
    }
}
