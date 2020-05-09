package io.github.theepicblock.discordunban;

import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.OfflinePlayer;

public class DiscordEventProcessor {
    DiscordUnban plugin;

    public DiscordEventProcessor(DiscordUnban plugin) {
        this.plugin = plugin;
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void discordMessageReceived(DiscordGuildMessageReceivedEvent event) {
        if (plugin.checkForCommand(event.getMessage())) {

            //try to get the player from the message
            String playerStr = plugin.stripCommand(event.getMessage().getContentRaw());
            OfflinePlayer player = plugin.getPlayerFromTargetted(playerStr);
            if (player != null) {
                if (plugin.getBanManager().isBanned(player)) { //check if actually banned
                    plugin.getBanManager().unban(player);
                    DiscordUtil.sendMessage(event.getChannel(),String.format("%s has been successfully unbanned by %s", player.getName(), event.getAuthor()));
                } else { //the player wasn't banned
                    DiscordUtil.sendMessage(event.getChannel(),String.format("%s, %s is not banned", event.getAuthor(), player.getName()));
                }
            } else { //the player is null
                DiscordUtil.sendMessage(event.getChannel(),String.format("%s, couldn't find %s", event.getAuthor(), playerStr));
            }
        }
    }
}
