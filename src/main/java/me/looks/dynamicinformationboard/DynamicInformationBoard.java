package me.looks.dynamicinformationboard;

import me.looks.dynamicinformationboard.commands.Command;
import me.looks.dynamicinformationboard.manager.Loader;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class DynamicInformationBoard extends JavaPlugin implements Listener {
    private Loader loader;

    @Override
    public void onEnable() {

        loader = new Loader(this);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> loader.load());

        PluginCommand pluginCommand = Bukkit.getPluginCommand("dynamicinformationboard");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(new Command(this));
        }
    }

    @Override
    public void onDisable() {
        if (loader != null) {
            loader.unload();
        }
    }

    public Loader getLoader() {
        return loader;
    }
}
