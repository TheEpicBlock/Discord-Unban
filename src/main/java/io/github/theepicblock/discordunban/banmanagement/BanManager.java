package io.github.theepicblock.discordunban.banmanagement;

import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public abstract class BanManager {

    /**
     * Unbans a player
     * @param player player to unban
     */
    public abstract void unban(OfflinePlayer player, UUID staffmember);

    public abstract boolean isBanned(OfflinePlayer player);

    public abstract MessageEmbed getBanInfo(OfflinePlayer player, String dateFormat);
}
