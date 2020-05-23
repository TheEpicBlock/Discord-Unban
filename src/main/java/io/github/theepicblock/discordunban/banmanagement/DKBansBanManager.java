package io.github.theepicblock.discordunban.banmanagement;

import ch.dkrieger.bansystem.lib.BanSystem;
import ch.dkrieger.bansystem.lib.player.NetworkPlayer;
import ch.dkrieger.bansystem.lib.player.history.BanType;
import ch.dkrieger.bansystem.lib.player.history.History;
import ch.dkrieger.bansystem.lib.player.history.entry.Ban;
import ch.dkrieger.bansystem.lib.player.history.entry.HistoryEntry;
import com.google.common.collect.Lists;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.util.List;
import java.util.UUID;

public class DKBansBanManager extends BanManager {

    public void unban(OfflinePlayer player, UUID staffmember) {
        NetworkPlayer networkPlayer = BanSystem.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        networkPlayer.unban(BanType.NETWORK, "Unbanned via discord", staffmember);
    }

    public boolean isBanned(OfflinePlayer player) {
        NetworkPlayer networkPlayer = BanSystem.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        return networkPlayer.isBanned();
    }

    public MessageEmbed getBanInfo(OfflinePlayer player, DateFormat dateFormat, @Nullable String[] args) {
        if (args == null) return noArgs(player, dateFormat);
        try {
            int page = Integer.parseInt(args[0]);
            return getEmbedForPage(player, dateFormat, page);
        } catch (NumberFormatException e) { //this isn't a valid number, ignore it
            return noArgs(player, dateFormat);
        }
    }

    private MessageEmbed noArgs(OfflinePlayer player, DateFormat dateFormat) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        NetworkPlayer networkPlayer = BanSystem.getInstance().getPlayerManager().getPlayer(player.getUniqueId());

        History history = networkPlayer.getHistory();

        //get current ban info
        if (!history.isBanned()) {
            embedBuilder.setAuthor("Current ban");
            embedBuilder.setDescription("*not currently banned*");
        } else {
            embedBuilder.setDescription("**Current ban**");

            Ban lastBan = history.getLastBan();
            embedBuilder.addField("by", getStaffName(lastBan), true);
            embedBuilder.addField("on", dateFormat.format(lastBan.getTimeStamp()), true);
            if (lastBan.getTimeOut() == -1) {
                embedBuilder.addField("ends", "never", true);
            } else {
                embedBuilder.addField("ends", dateFormat.format(lastBan.getTimeOut()), true);
            }
            embedBuilder.addField("reason", lastBan.getReason(), true);
            if (!lastBan.getMessage().isEmpty()) { //check if the message actually contains something
                embedBuilder.addField("Message", lastBan.getMessage(), true);
            }
        }

        embedBuilder.addField("", "**History**", false);//get some history
        embedBuilder.addField("warnings", String.valueOf(history.getWarnCount()), true);
        embedBuilder.addField("bans", String.valueOf(history.getBanCount()), true);
        return embedBuilder.build();
    }

    private MessageEmbed getEmbedForPage(OfflinePlayer player, DateFormat dateFormat, int page) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        int entriesPerPage = 10;

        NetworkPlayer networkPlayer = BanSystem.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        History history = networkPlayer.getHistory();

        String entries = getBanHistoryList(history, (page - 1) * entriesPerPage, page * entriesPerPage, dateFormat);
        embedBuilder.addField("Page " + page, entries, false);
        return embedBuilder.build();
    }

    /**
     * gets a part of history as a string. Formatted as "(action) - by (staff) on (date), (reason)\n"
     *
     * @param history    history to get the info from
     * @param start      on which entry to start (inclusive)
     * @param end        on which entry to end (exclusive)
     * @param dateFormat format to use
     */
    private String getBanHistoryList(History history, int start, int end, DateFormat dateFormat) {
        List<HistoryEntry> entries = Lists.reverse(history.getEntries()); //reverse because the list is in reverse chronological order
        int end_ = Math.min(end, entries.size());
        StringBuilder output = new StringBuilder();
        if (start > end_) {
            return "";
        }

        entries = entries.subList(start, end_);
        for (HistoryEntry entry : entries) {
            String m = String.format("%s - by %s on %s, %s\n",
                    entry.getTypeName(),
                    getStaffName(entry),
                    dateFormat.format(entry.getTimeStamp()),
                    entry.getReason());
            output.append(m);
        }
        return output.toString();
    }

    private String getStaffName(HistoryEntry entry) {
        return ChatColor.stripColor(entry.getStaffName());
    }
}
