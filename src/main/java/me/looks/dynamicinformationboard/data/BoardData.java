package me.looks.dynamicinformationboard.data;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.clip.placeholderapi.PlaceholderAPI;
import me.looks.dynamicinformationboard.DynamicInformationBoard;
import me.looks.dynamicinformationboard.utils.BoardFace;
import me.looks.dynamicinformationboard.utils.PictureRenderer;
import me.looks.dynamicinformationboard.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BoardData {
    private final String key;
    private final Location location;
    private final int widthDimension;
    private final int heightDimension;
    private final boolean frameInvisibly;
    private final double distanceView;
    private final int boardUpdateDelay;
    private int boardUpdateTime;

    private final List<ItemFrameData> itemFrames = new ArrayList<>();
    private final List<Player> viewers = new ArrayList<>();

    public BoardData(String key, Location location, int widthDimension, int heightDimension,
                     boolean frameInvisibly, double distanceView, int boardUpdateDelay) {
        this.key = key;
        this.location = location;
        this.widthDimension = widthDimension;
        this.heightDimension = heightDimension;
        this.frameInvisibly = frameInvisibly;
        this.distanceView = distanceView;
        this.boardUpdateDelay = boardUpdateDelay;
        this.boardUpdateTime = boardUpdateDelay;
    }
    public void load(BoardFace boardFace) {

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        for (int h = 0; h < heightDimension; h++) {
            for (int w = 0; w < widthDimension; w++) {

                int entityID = (int) (Math.random() * Integer.MAX_VALUE);

                PacketContainer packetSpawn = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
                packetSpawn.getIntegers().write(0, entityID);
                packetSpawn.getEntityTypeModifier().write(0, EntityType.ITEM_FRAME);
                packetSpawn.getDoubles()
                        .write(0, location.getX() + (w * boardFace.getX()))
                        .write(1, location.getY() + (h * boardFace.getY()))
                        .write(2, location.getZ() + (w * boardFace.getZ()));
                if (ServerVersion.beforeOrEqual(17)) {
                    packetSpawn.getIntegers().write(5, (int) (boardFace.getRotation() * 256.0F / 360.0F));
                } else {
                    packetSpawn.getBytes().write(1, (byte) (boardFace.getRotation() * 256.0F / 360.0F));
                }

                ItemFrameData itemFrameData = new ItemFrameData(entityID, packetSpawn);
                itemFrames.add(itemFrameData);
            }
        }
    }
    public void update(DynamicInformationBoard plugin) {

        ConfigurationSection sectionBoard = plugin.getConfig().getConfigurationSection("boards." + key);
        if (sectionBoard == null) {
            plugin.getLogger().severe("Боард " + key + " не найден в конфиге!");
            return;
        }
        BufferedImage bufferedImage = getImage(plugin, sectionBoard.getString("image", ""));

        ConfigurationSection sectionStrings = sectionBoard.getConfigurationSection("strings");

        if (sectionStrings != null) {
            for (String key : sectionStrings.getKeys(false)) {

                Graphics2D graphics = bufferedImage.createGraphics();
                File fileFont = new File(plugin.getDataFolder(), sectionStrings.getString(key + ".font", ""));
                int fontSize = sectionStrings.getInt(key + ".size", 50);
                String text = PlaceholderAPI.setPlaceholders(null,
                        sectionStrings.getString(key + ".text", ""));
                int offsetX = sectionStrings.getInt(key + ".offset-x", 0);
                int offsetY = sectionStrings.getInt(key + ".offset-y", 0);
                Color color = getColor(sectionStrings.getString(key + ".color", ""), Color.BLACK);

                try {
                    Font font;
                    if (fileFont.exists() && fileFont.getPath().endsWith(".ttf")) {
                        font = Font.createFont(Font.TRUETYPE_FONT, fileFont).deriveFont(Font.PLAIN, fontSize);
                    } else {
                        font = new Font("Arial", Font.PLAIN, fontSize);
                    }
                    graphics.setFont(font);
                    graphics.setColor(color);
                    graphics.drawString(text, offsetX, offsetY);

                    graphics.dispose();
                } catch (IOException | FontFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        Image[][] splitImages = splitImage(bufferedImage, widthDimension, heightDimension,
                getColor(sectionBoard.getString("background-image", ""), null));

        World world = location.getWorld();
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        int count = 0;
        for (int h = 0; h < heightDimension; h++) {
            for (int w = 0; w < widthDimension; w++) {

                Image image = splitImages[w][h];
                ItemFrameData itemFrameData = itemFrames.get(count++);

                MapView mapView = Bukkit.createMap(world);
                mapView.addRenderer(new PictureRenderer(image));

                ItemStack map = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) map.getItemMeta();
                meta.setMapView(mapView);
                map.setItemMeta(meta);

                PacketContainer packetMeta = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);

                WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObjectItem = new WrappedDataWatcher.WrappedDataWatcherObject(
                        ServerVersion.afterOrEqual(17) ? 8 : 7,
                        WrappedDataWatcher.Registry.getItemStackSerializer(false));
                WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObjectRotation = new WrappedDataWatcher.WrappedDataWatcherObject(
                        ServerVersion.afterOrEqual(17) ? 9 : 8,
                        WrappedDataWatcher.Registry.get(Integer.class));

                packetMeta.getIntegers().write(0, itemFrameData.getEntityID());
                WrappedDataWatcher watcher = new WrappedDataWatcher();
                watcher.setObject(dataWatcherObjectItem, map);
                watcher.setObject(dataWatcherObjectRotation, 100);
                if (frameInvisibly) {
                    watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0,
                            WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);
                }

                if (ServerVersion.afterOrEqual(ServerVersion.v1_19_R3)) {
                    packetMeta.getDataValueCollectionModifier().write(0, toDataValueList(watcher));
                } else {
                    packetMeta.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
                }

                itemFrameData.update(packetMeta, mapView);
            }
        }
        sendBoardToPlayer(viewers, false);
    }

    private Color getColor(String str, Color defaultValue) {
        String[] args = str.split(";");
        if (args.length == 3) {
            try {
                return new Color(
                        Integer.parseInt(args[0]),
                        Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
    public @NotNull BufferedImage getImage(DynamicInformationBoard plugin, String pathImage) {
        File imageFile = new File(plugin.getDataFolder(), pathImage);
        BufferedImage image;
        if (imageFile.exists()) {
            try {
                image = ImageIO.read(imageFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            image = new BufferedImage(128 * widthDimension, 128 * heightDimension, BufferedImage.TYPE_INT_ARGB);
        }
        return image;
    }
    public static @NotNull Image[][] splitImage(BufferedImage image, int width, int height, Color finalBackground) {
        Image[][] images = new Image[width][height];

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int frameWidth = 128 * width;
        int frameHeight = 128 * height;
        double imageAspectRatio = (double) imageWidth / imageHeight;
        double frameAspectRatio = (double) frameWidth / frameHeight;
        int containedWidth;
        int containedHeight;
        if (imageAspectRatio >= frameAspectRatio) {
            containedWidth = frameWidth;
            containedHeight = (int) Math.floor(containedWidth / imageAspectRatio);
        } else {
            containedHeight = frameHeight;
            containedWidth = (int) Math.floor(containedHeight * imageAspectRatio);
        }

        Image resizedImage = image.getScaledInstance(containedWidth, containedHeight, Image.SCALE_DEFAULT);
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                BufferedImage newImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

                int heightModifier = 0;
                int widthModifier = 0;
                if (frameHeight > containedHeight) {
                    heightModifier = (frameHeight - containedHeight) / 2;
                }
                if (frameWidth > containedWidth) {
                    widthModifier = (frameWidth - containedWidth) / 2;
                }
                int sourceX = 128 * w - widthModifier ;
                int sourceY = 128 * h - heightModifier;

                Graphics2D g2d = newImage.createGraphics();
                if (finalBackground != null) {
                    g2d.setPaint(finalBackground);
                    g2d.fillRect(0, 0, 128, 128);
                }
                g2d.drawImage(resizedImage, 0, 0, 128, 128, sourceX, sourceY, sourceX + 128, sourceY + 128, null);
                g2d.dispose();

                images[w][height - h - 1] = newImage;
            }
        }
        return images;
    }
    private List<WrappedDataValue> toDataValueList(WrappedDataWatcher wrappedDataWatcher) {
        List<WrappedWatchableObject> watchableObjectList = wrappedDataWatcher.getWatchableObjects();
        List<WrappedDataValue> wrappedDataValues = new ArrayList<>(watchableObjectList.size());
        for (WrappedWatchableObject wrappedWatchableObject : wrappedDataWatcher.getWatchableObjects()) {
            WrappedDataWatcher.WrappedDataWatcherObject wrappedDataWatcherObject = wrappedWatchableObject.getWatcherObject();
            wrappedDataValues.add(new WrappedDataValue(wrappedDataWatcherObject.getIndex(), wrappedDataWatcherObject.getSerializer(), wrappedWatchableObject.getRawValue()));
        }
        return wrappedDataValues;
    }
    public void sendBoardToPlayer(List<Player> players, boolean load) {
        if (players.isEmpty()) return;

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        for (Player player : players) {
            for (ItemFrameData itemFrameData : itemFrames) {
                if (load) {
                    protocolManager.sendServerPacket(player, itemFrameData.getPacketSpawn());
                }

                if (itemFrameData.getMapView() != null && itemFrameData.getPacketMeta() != null) {
                    player.sendMap(itemFrameData.getMapView());
                    protocolManager.sendServerPacket(player, itemFrameData.getPacketMeta());
                }
            }
        }
        this.viewers.addAll(players);
    }
    public void unloadBoardToPlayer(List<Player> players) {
        if (players.isEmpty()) return;

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        PacketContainer packetDestroy = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        if (ServerVersion.after(16)) {
            packetDestroy.getIntLists().write(0, itemFrames.stream()
                    .map(ItemFrameData::getEntityID).collect(Collectors.toList()));
        } else {
            packetDestroy.getIntegerArrays().write(0, itemFrames.stream()
                    .mapToInt(ItemFrameData::getEntityID).toArray());
        }

        for (Player player : players) {
            protocolManager.sendServerPacket(player, packetDestroy);
        }
        this.viewers.removeAll(players);
    }

    public List<Player> getViewers() {
        return viewers;
    }

    public double getDistanceView() {
        return distanceView;
    }

    public int getBoardUpdateTime() {
        return boardUpdateTime;
    }

    public void setBoardUpdateTime(int boardUpdateTime) {
        this.boardUpdateTime = boardUpdateTime;
    }

    public int getBoardUpdateDelay() {
        return boardUpdateDelay;
    }

    public Location getLocation() {
        return location;
    }
}
