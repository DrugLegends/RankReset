package me.rayzr522.rankreset.listeners;

import me.rayzr522.rankreset.RankReset;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerListener implements Listener {
    private final RankReset plugin;

    public PlayerListener(RankReset plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (player == null || !plugin.shouldReset(player)) {
            return;
        }

        plugin.getLogger().info(String.format("Resetting ranks for: %s (uuid: %s)", player.getName(), player.getUniqueId()));

        Permission permissions = plugin.getPermissions();
        List<String> ranksToPreserve = plugin.getRanksToPreserve();

        String[] existingGroups = permissions.getPlayerGroups(null, player);

        List<String> preservedRanks = Arrays.stream(existingGroups)
                .filter(ranksToPreserve::contains)
                .collect(Collectors.toList());

        List<String> finalRanks = new ArrayList<>();

        finalRanks.addAll(preservedRanks);
        finalRanks.addAll(plugin.getDefaultRanks());

        resetPlayerInContext(player, null);
        Bukkit.getWorlds().forEach(world -> resetPlayerInContext(player, world.getName()));

        finalRanks.forEach(rank -> permissions.playerAddGroup(null, player, rank));
    }

    private void resetPlayerInContext(Player player, String context) {
        Arrays.stream(plugin.getPermissions().getPlayerGroups(context, player)).forEach(
                group -> plugin.getPermissions().playerRemoveGroup(context, player, group)
        );
    }
}
