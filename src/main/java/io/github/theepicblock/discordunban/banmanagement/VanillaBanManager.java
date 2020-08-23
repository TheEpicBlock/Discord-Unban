package io.github.theepicblock.discordunban.banmanagement;

import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class VanillaBanManager implements BanManager {
    private final JavaPlugin plugin;

    public VanillaBanManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void unban(OfflinePlayer player, UUID staffmember) {
        Bukkit.getScheduler().runTask(plugin, () -> { //this is a bukkit api thing. So I should run it on the main thread to be safe
            if (player.getName() != null) {
                plugin.getServer().getBanList(BanList.Type.NAME).pardon(player.getName());
            }
        });
    }

    public boolean isBanned(OfflinePlayer player) {
        return player.isBanned();
    }

    public EmbedBuilder getBanInfo(OfflinePlayer player, DateFormat dateFormat, @Nullable String[] args) {
        if (player.getName() == null) return new EmbedBuilder().appendDescription("Error, can't find player name");
        //get ban info and check if it's not null
        BanEntry banEntry = plugin.getServer().getBanList(BanList.Type.NAME).getBanEntry(player.getName());
        if (banEntry == null) return new EmbedBuilder().appendDescription("Error, can't find ban information");

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (!this.isBanned(player)) { //this player isn't banned, so we can't get any info
            embedBuilder.appendDescription(player.getName() + " is not currently banned");
            return embedBuilder;
        }

        //get expiration date
        Date expirationDate = banEntry.getExpiration();
        String expiration;
        if (expirationDate == null) {
            expiration = "never";
        } else {
            expiration = "on " + dateFormat.format(banEntry.getExpiration());
        }

        //add fields to embed
        embedBuilder.addField("by", banEntry.getSource(), true);
        embedBuilder.addField("on", dateFormat.format(banEntry.getCreated()), true);
        embedBuilder.addField("ends", expiration, true);
        embedBuilder.addField("reason", banEntry.getReason(), false);

        return embedBuilder;
    }
}
