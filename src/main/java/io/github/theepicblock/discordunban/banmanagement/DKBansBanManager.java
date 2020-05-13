package io.github.theepicblock.discordunban.banmanagement;

import ch.dkrieger.bansystem.lib.BanSystem;
import ch.dkrieger.bansystem.lib.player.NetworkPlayer;
import ch.dkrieger.bansystem.lib.player.history.BanType;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.OfflinePlayer;

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

    public MessageEmbed getBanInfo(OfflinePlayer player, String dateFormat) {
        return null;
    }
}
