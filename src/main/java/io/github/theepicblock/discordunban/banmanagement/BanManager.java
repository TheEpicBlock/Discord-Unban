package io.github.theepicblock.discordunban.banmanagement;

import org.bukkit.OfflinePlayer;

public abstract class BanManager {

    /**
     * Unbans a player
     * @param player player to unban
     */
    public abstract void unban(OfflinePlayer player);

    public abstract boolean isBanned(OfflinePlayer player);
}
