package io.github.theepicblock.discordunban.banmanagement;

import org.bukkit.BanList;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

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
}
