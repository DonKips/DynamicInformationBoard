package me.looks.dynamicinformationboard.manager;

import me.looks.dynamicinformationboard.DynamicInformationBoard;
import me.looks.dynamicinformationboard.data.BoardData;
import me.looks.dynamicinformationboard.utils.BoardFace;
import me.looks.dynamicinformationboard.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Loader {
    private final DynamicInformationBoard plugin;
    private final List<BoardData> boards = new ArrayList<>();
    private final HashMap<String, String> messages = new HashMap<>();

    public Loader(DynamicInformationBoard plugin) {
        this.plugin = plugin;
    }


    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        ConfigurationSection sectionBoards = plugin.getConfig().getConfigurationSection("boards");

        if (sectionBoards != null) {
            for (String key : sectionBoards.getKeys(false)) {
                ConfigurationSection sectionBoard = sectionBoards.getConfigurationSection(key);
                Objects.requireNonNull(sectionBoard);

                Location location = getLocation(sectionBoard);
                if (location == null) continue;

                int widthDimension = sectionBoard.getInt("width-dimension");
                int heightDimension = sectionBoard.getInt("height-dimension");
                BoardFace boardFace = getBoardFace(sectionBoard.getString("board-face", ""));
                boolean frameInvisibly = sectionBoard.getBoolean("frame-invisibly", false);
                double distanceView = sectionBoard.getDouble("distance-view", 20);
                int boardUpdateDelay = Math.max(0, sectionBoard.getInt("update-delay"));

                BoardData boardData = new BoardData(key, location, widthDimension, heightDimension, frameInvisibly,
                        distanceView, boardUpdateDelay);
                boardData.load(boardFace);
                boardData.update(plugin);

                boards.add(boardData);
            }
        }

        ConfigurationSection sectionMessages = plugin.getConfig().getConfigurationSection("messages");

        if (sectionMessages != null) {
            for (String key : sectionMessages.getKeys(false)) {
                messages.put(key, ColorUtil.color(sectionMessages.getString(key)));
            }
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {

            for (BoardData boardData : boards) {

                boardData.setBoardUpdateTime(boardData.getBoardUpdateTime() - 1);
                if (boardData.getBoardUpdateTime() <= 0) {
                    boardData.setBoardUpdateTime(boardData.getBoardUpdateDelay());
                    boardData.update(plugin);
                }

                Location location = boardData.getLocation();
                List<Player> viewers = new ArrayList<>(Bukkit.getOnlinePlayers()).stream().filter(player -> {
                    Location locationPlayer = player.getLocation();
                    return location.getWorld().equals(locationPlayer.getWorld())
                            && location.distance(locationPlayer) <= boardData.getDistanceView();
                }).collect(Collectors.toList());

                List<Player> unload = boardData.getViewers().stream().filter(player -> !viewers.contains(player)).collect(Collectors.toList());
                List<Player> load = viewers.stream().filter(player -> !boardData.getViewers().contains(player)).collect(Collectors.toList());

                boardData.unloadBoardToPlayer(unload);
                boardData.sendBoardToPlayer(load, true);
            }

        }, 20, 20);
    }
    public @Nullable Location getLocation(ConfigurationSection sectionBoard) {
        World world = Bukkit.getWorld(sectionBoard.getString("location.world", ""));
        if (world == null) {
            plugin.getLogger().severe("Мир локации у боарда " + sectionBoard.getName() + " указан неверно!");
            return null;
        }
        return new Location(world,
                sectionBoard.getDouble("location.x"),
                sectionBoard.getDouble("location.y"),
                sectionBoard.getDouble("location.z"));
    }
    public @NotNull BoardFace getBoardFace(String strValue) {
        try {
            return BoardFace.valueOf(strValue.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return BoardFace.WEST;
        }
    }

    public void unload() {
        Bukkit.getScheduler().cancelTasks(plugin);
        if (!Bukkit.isStopping()) {
            for (BoardData boardData : boards) {
                boardData.unloadBoardToPlayer(boardData.getViewers());
            }
        }
        boards.clear();
        messages.clear();
    }

    public HashMap<String, String> getMessages() {
        return messages;
    }
}
