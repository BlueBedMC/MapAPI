package com.bluebed.mapapi.spigot;

import com.bluebed.mapapi.core.AbstractMapAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

@Getter
public class MapAPI extends AbstractMapAPI<MapAPI> {
    protected ItemFrame[][] itemFrames;
    private Location pos;
    private Direction direction;

    public MapAPI pos(Location pos) {
        this.pos = pos;
        return this;
    }

    public MapAPI direction(Direction direction) {
        this.direction = direction;
        return this;
    }

    @Override
    protected MapAPI self() {
        return this;
    }

    public void render(BufferedImage image) {
        render(slice(image));
    }

    @Override
    public MapAPI build(Consumer<MapAPI> consumer)  {
        if (direction == null) direction = Direction.NORTH;
        if (pos == null) return null;
        if (pos.getWorld() == null) return null;

        int size = tileSize.getSize();
        itemFrames = new ItemFrame[
                (int) Math.ceil((double) width / size)
                ][
                (int) Math.ceil((double) height / size)
                ];

        for (int i = 0; i < itemFrames.length; i++) {
            for (int j = 0; j < itemFrames[i].length; j++) {
                // make this better eventually
                ItemFrame frame = switch (direction) {
                    case WEST -> pos.getWorld().spawn(pos.add(0, -j, -i), ItemFrame.class);
                    case EAST -> pos.getWorld().spawn(pos.add(0, -j, i), ItemFrame.class);
                    case SOUTH -> pos.getWorld().spawn(pos.add(i, -j, 0), ItemFrame.class);
                    default -> pos.getWorld().spawn(pos.add(-i, -j, 0), ItemFrame.class);
                };

                frame.setFacingDirection(convertDirection(direction), true);
                frame.setVisible(!invisible);

                itemFrames[i][j] = frame;
            }
        }

        if (consumer != null)
            consumer.accept(this);

        return this;
    }

    public void render(BufferedImage[][] tiles) {
        // if needed in future
        int size = tileSize.getSize();

        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                ItemFrame frame = itemFrames[col][row];
                BufferedImage tile = tiles[row][col];

                MapView map = Bukkit.createMap(frame.getWorld());
                map.getRenderers().forEach(map::removeRenderer);

                map.addRenderer(new MapRenderer() {
                    @Override
                    public void render(@NotNull MapView view, @NotNull MapCanvas canvas, @NotNull Player player) {
                        canvas.drawImage(0, 0, tile);
                    }
                });

                ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) mapItem.getItemMeta();
                if (meta != null) meta.setMapView(map);
                mapItem.setItemMeta(meta);

                frame.setItem(mapItem);
            }
        }
    }

    protected BufferedImage[][] slice(BufferedImage image) {
        int size = tileSize.getSize();

        int cols = (int) Math.ceil(image.getWidth() / (double) size);
        int rows = (int) Math.ceil(image.getHeight() / (double) size);

        BufferedImage[][] tiles = new BufferedImage[rows][cols];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int w = Math.min(size, image.getWidth() - x * size);
                int h = Math.min(size, image.getHeight() - y * size);

                tiles[y][x] = image.getSubimage(x * size, y * size, w, h);
            }
        }

        return tiles;
    }

    @Override
    public void remove() {
        for (ItemFrame[] rows : itemFrames) {
            for (ItemFrame frame : rows) frame.remove();
        }
    }

    protected BlockFace convertDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> BlockFace.NORTH;
            case SOUTH -> BlockFace.SOUTH;
            case EAST -> BlockFace.EAST;
            case WEST -> BlockFace.WEST;
            default -> BlockFace.NORTH;
        };
    }

}
