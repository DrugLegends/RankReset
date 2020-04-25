package me.rayzr522.rankreset.listeners;

import me.rayzr522.rankreset.RankReset;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
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
                .map(String::toLowerCase)
                .filter(ranksToPreserve::contains)
                .collect(Collectors.toList());

        List<String> finalRanks = new ArrayList<>();

        finalRanks.addAll(preservedRanks);
        finalRanks.addAll(plugin.getDefaultRanks());

        resetPlayerInContext(player).accept(null);

        Bukkit.getWorlds().stream()
                .map(World::getName)
                .forEach(resetPlayerInContext(player));

        plugin.debug(String.format("Giving ranks to player '%s': %s", player.getName(), String.join(", ", finalRanks)));
        finalRanks.forEach(rank -> permissions.playerAddGroup(null, player, rank));
    }

    private Consumer<String> resetPlayerInContext(Player player) {
        return context -> {
            Arrays.stream(plugin.getPermissions().getPlayerGroups(context, player)).forEach(
                    group -> {
                        plugin.debug(String.format("Resetting rank '%s' for player '%s' in context: %s", group, player.getName(), context));
                        plugin.getPermissions().playerRemoveGroup(context, player, group);
                    }
            );

            plugin.getPermissionsToRemove().forEach(
                    permission -> {
                        plugin.debug(String.format("Resetting permission '%s' for player '%s' in context: %s", permission, player.getName(), context));
                        plugin.getPermissions().playerRemove(context, player, permission);
                    }
            );
        };
    }
}
