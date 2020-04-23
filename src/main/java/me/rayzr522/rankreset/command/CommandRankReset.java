package me.rayzr522.rankreset.command;

import me.rayzr522.rankreset.RankReset;
import org.bukkit.command.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandRankReset implements CommandExecutor, TabCompleter {
    private final RankReset plugin;

    public CommandRankReset(RankReset plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (plugin.checkPermission(sender, "use", false) && args.length == 1) {
            return Stream.of("version", "reload", "reset", "help")
                    .filter(arg -> arg.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }

    public void register(PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//        if (!(sender instanceof Player)) {
//            sender.sendMessage(plugin.tr("command.fail.only-players"));
//            return true;
//        }
//
//        Player player = (Player) sender;

        if (!plugin.checkPermission(sender, "use", true)) {
            return true;
        }

        if (args.length < 1) {
            showUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "version":
                sender.sendMessage(plugin.tr("command.rankreset.version", plugin.getName(), plugin.getDescription().getVersion()));
                break;
            case "reload":
                plugin.reload();
                sender.sendMessage(plugin.tr("command.rankreset.reloaded"));
                break;
            case "reset":
                plugin.triggerReset();
                break;
            case "help":
            case "?":
            default:
                showUsage(sender);
        }

        return true;
    }

    private void showUsage(CommandSender sender) {
        sender.sendMessage(plugin.tr("command.rankreset.help.message"));
    }
}