package io.github.theepicblock.discordunban.banmanagement;

import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.OfflinePlayer;

import java.text.DateFormat;
import java.util.UUID;

public abstract class BanManager {

    /**
     * Unbans a player
     * @param player player to unban
     */
    public abstract void unban(OfflinePlayer player, UUID staffmember);

    /**
     * checks if a player is banned
     * @param player player to check
     * @return true if the player is banned
     */
    public abstract boolean isBanned(OfflinePlayer player);

    /**
     * gets information about a players bans as an embed
     * @param player player to get info from
     * @param dateFormat format to use for any dates
     */
    public abstract MessageEmbed getBanInfo(OfflinePlayer player, DateFormat dateFormat);
}
