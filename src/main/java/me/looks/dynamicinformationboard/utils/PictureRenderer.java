package me.looks.dynamicinformationboard.utils;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class PictureRenderer extends MapRenderer {
    private final Image image;
    private boolean rendered;

    public PictureRenderer(Image image) {
        this.image = image;
    }

    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        if (rendered) {
            return;
        }
        rendered = true;
        MapCursorCollection mapCursor = canvas.getCursors();
        for (int i = 0; i < mapCursor.size(); i++) {
            mapCursor.removeCursor(mapCursor.getCursor(i));
        }
        try {
            canvas.drawImage(0, 0, image);
            player.sendMap(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
