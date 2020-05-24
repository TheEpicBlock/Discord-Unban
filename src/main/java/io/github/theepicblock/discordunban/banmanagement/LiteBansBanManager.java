package io.github.theepicblock.discordunban.banmanagement;

import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import litebans.api.Database;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.UUID;

public class LiteBansBanManager implements BanManager {
    private final String IP = "127.0.0.1"; //LiteBans requires an ip with every query

    @Override
    public void unban(OfflinePlayer player, UUID staffmember) {
        //yes, this is what LiteBans suggests you do. And yes, it's kinda stupid
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("unban %s --sender-uuid=%s", player.getName(), staffmember.toString()));
    }

    @Override
    public boolean isBanned(OfflinePlayer player) {
        return Database.get().isPlayerBanned(player.getUniqueId(),IP);
    }

    @Override
    public MessageEmbed getBanInfo(OfflinePlayer player, DateFormat dateFormat, @Nullable String[] args) {
        String uuid = player.getUniqueId().toString();
        String query = "SELECT * FROM {bans} WHERE uuid=?";

        EmbedBuilder embedBuilder = new EmbedBuilder();

        try (PreparedStatement st = Database.get().prepareStatement(query)) {
            st.setString(1, uuid);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String reason = rs.getString("reason");
                    String bannedByUuid = rs.getString("banned_by_uuid");
                    long time = rs.getLong("time");
                    long until = rs.getLong("until");
                    long id = rs.getLong("id");
                    boolean active = rs.getBoolean("active");

                    if (active) {
                        embedBuilder.addField("by", Bukkit.getOfflinePlayer(UUID.fromString(bannedByUuid)).getName(),true);
                        embedBuilder.addField("on",dateFormat.format(time),true);
                        embedBuilder.addField("until",dateFormat.format(until),true);
                        embedBuilder.addField("reason",reason,false);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return embedBuilder.build();
    }
}
