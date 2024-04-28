package me.looks.dynamicinformationboard.data;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.map.MapView;

public class ItemFrameData {
    private final int entityID;
    private final PacketContainer packetSpawn;
    private PacketContainer packetMeta;
    private MapView mapView;

    public ItemFrameData(int entityID, PacketContainer packetSpawn) {
        this.entityID = entityID;
        this.packetSpawn = packetSpawn;
    }
    public void update(PacketContainer packetMeta, MapView mapView) {
        this.mapView = mapView;
        this.packetMeta = packetMeta;
    }

    public MapView getMapView() {
        return mapView;
    }

    public PacketContainer getPacketMeta() {
        return packetMeta;
    }

    public PacketContainer getPacketSpawn() {
        return packetSpawn;
    }

    public int getEntityID() {
        return entityID;
    }
}
