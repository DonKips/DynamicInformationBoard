package me.looks.dynamicinformationboard.commands;

import me.looks.dynamicinformationboard.DynamicInformationBoard;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Command implements CommandExecutor {
    private final DynamicInformationBoard plugin;

    public Command(DynamicInformationBoard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.isOp() && !sender.hasPermission("dynamicinformationboard.command.usage")) {
            String message = plugin.getLoader().getMessages().get("no-permission");
            if (message != null && !message.isEmpty()) {
                sender.sendMessage(message);
            }
            return false;
        }


        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {

                if (!sender.isOp() && !sender.hasPermission("dynamicinformationboard.command.reload")) {
                    String message = plugin.getLoader().getMessages().get("no-permission");
                    if (message != null && !message.isEmpty()) {
                        sender.sendMessage(message);
                    }
                    return false;
                }

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

                    long startMs = System.currentTimeMillis();

                    plugin.getLoader().unload();
                    plugin.getLoader().load();

                    long endMs = System.currentTimeMillis();

                    String message = plugin.getLoader().getMessages().get("command-reload-confirm");
                    if (message != null && !message.isEmpty()) {
                        sender.sendMessage(message.replace("%ms", String.valueOf(endMs - startMs)));
                    }
                });

                return true;
            }
        }

        String messageHelpHeader = plugin.getLoader().getMessages().get("command-help-header");
        if (messageHelpHeader != null && !messageHelpHeader.isEmpty()) {
            sender.sendMessage(messageHelpHeader);
        }

        if (sender.isOp() || sender.hasPermission("dynamicinformationboard.command.reload")) {
            String message = plugin.getLoader().getMessages().get("command-reload-help");
            if (message != null && !message.isEmpty()) {
                sender.sendMessage(message);
            }
        }

        return false;
    }
}
