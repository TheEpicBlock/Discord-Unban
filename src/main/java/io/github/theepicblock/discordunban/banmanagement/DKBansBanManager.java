package io.github.theepicblock.discordunban.banmanagement;

import ch.dkrieger.bansystem.lib.BanSystem;
import ch.dkrieger.bansystem.lib.player.NetworkPlayer;
import ch.dkrieger.bansystem.lib.player.history.BanType;
import ch.dkrieger.bansystem.lib.player.history.History;
import ch.dkrieger.bansystem.lib.player.history.entry.Ban;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class DKBansBanManager extends BanManager{

    public void unban(OfflinePlayer player, UUID staffmember) {
        NetworkPlayer networkPlayer = BanSystem.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        networkPlayer.unban(BanType.NETWORK,"Unbanned via discord", staffmember);
    }

    public boolean isBanned(OfflinePlayer player) {
        NetworkPlayer networkPlayer = BanSystem.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        return networkPlayer.isBanned();
    }

    public MessageEmbed getBanInfo(OfflinePlayer player, DateFormat dateFormat) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        NetworkPlayer networkPlayer = BanSystem.getInstance().getPlayerManager().getPlayer(player.getUniqueId());

        History history = networkPlayer.getHistory();

        //get current ban info
        if (!history.isBanned()) {
            embedBuilder.addField("not currently banned", "", false);
        } else {

            Ban lastBan = history.getLastBan();
            embedBuilder.addField("by", ChatColor.stripColor(lastBan.getStaffName()),true);
            embedBuilder.addField("on",dateFormat.format(lastBan.getTimeStamp()),true);
            embedBuilder.addField("ends",dateFormat.format(lastBan.getTimeOut()), true);
            embedBuilder.addField("reason",lastBan.getReason(), true);
            if (lastBan.getMessage() != "") {
                embedBuilder.addField("Message",lastBan.getMessage(), true);
            }
        }

        //get some history
        embedBuilder.addField("warnings", String.valueOf(history.getWarnCount()), true);
        embedBuilder.addField("bans", String.valueOf(history.getBanCount()), true);
        embedBuilder.addField("points", String.valueOf(history.getPoints()), true);

        return embedBuilder.build();
    }
}
