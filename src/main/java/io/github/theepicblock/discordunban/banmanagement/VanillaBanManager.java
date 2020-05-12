package io.github.theepicblock.discordunban.banmanagement;

import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class VanillaBanManager extends BanManager{
    JavaPlugin plugin;

    public VanillaBanManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void unban(OfflinePlayer player, UUID staffmember) {
        plugin.getServer().getBanList(BanList.Type.NAME).pardon(player.getName());
    }

    public boolean isBanned(OfflinePlayer player) {
        return player.isBanned();
    }

    public MessageEmbed getBanReason(OfflinePlayer player, String dateFormat) {
        BanEntry banEntry = plugin.getServer().getBanList(BanList.Type.NAME).getBanEntry(player.getName());
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);

        //get expiration date
        Date expirationDate = banEntry.getExpiration();
        String expiration;
        if (expirationDate == null) {
            expiration = "never";
        } else {
            expiration = "on " + format.format(banEntry.getExpiration());
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.addField("by",banEntry.getSource(),true);
        embedBuilder.addField("on",format.format(banEntry.getCreated()),true);
        embedBuilder.addField("ends",expiration, true);
        embedBuilder.addField("reason",banEntry.getReason(), false);

        return embedBuilder.build();
    }
}
