package io.github.theepicblock.discordunban.banmanagement;

import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class VanillaBanManager extends BanManager{
    JavaPlugin plugin;

    public VanillaBanManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void unban(OfflinePlayer player, UUID staffmember) {
        Bukkit.getScheduler().runTask(plugin,() -> { //this is a bukkit api thing. So I should run it on the main thread to be safe
            plugin.getServer().getBanList(BanList.Type.NAME).pardon(player.getName());
        });
    }

    public boolean isBanned(OfflinePlayer player) {
        return player.isBanned();
    }

    public MessageEmbed getBanInfo(OfflinePlayer player, DateFormat dateFormat) {
        BanEntry banEntry = plugin.getServer().getBanList(BanList.Type.NAME).getBanEntry(player.getName());

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (!this.isBanned(player)) { //this player isn't banned, so we can't get any info
            embedBuilder.appendDescription(player.getName() + " is not currently banned");
            return embedBuilder.build();
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
        embedBuilder.addField("by",banEntry.getSource(),true);
        embedBuilder.addField("on",dateFormat.format(banEntry.getCreated()),true);
        embedBuilder.addField("ends",expiration, true);
        embedBuilder.addField("reason",banEntry.getReason(), false);

        return embedBuilder.build();
    }
}
