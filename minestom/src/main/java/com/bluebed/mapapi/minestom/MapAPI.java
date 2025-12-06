package com.bluebed.mapapi.minestom;

import com.bluebed.mapapi.core.AbstractMapAPI;
import lombok.Getter;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.map.framebuffers.LargeGraphics2DFramebuffer;
import net.minestom.server.network.packet.server.play.MapDataPacket;
import net.minestom.server.utils.Direction;

import javax.management.InstanceNotFoundException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

@Getter
public class MapAPI extends AbstractMapAPI<MapAPI> {
    protected Entity[][] itemFrames;
    private Instance instance;
    private Pos pos;
    private Direction direction;

    public MapAPI instance(Instance instance) {
        this.instance = instance;
        return this;
    }

    public MapAPI pos(Pos pos) {
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
    public MapAPI build(Consumer<MapAPI> consumer) throws InstanceNotFoundException {
        if (instance == null) throw new InstanceNotFoundException();
        if (direction == null) direction = Direction.NORTH;
        if (pos == null) return null;

        int size = tileSize.getSize();
        itemFrames = new Entity[
                (int) Math.ceil((double) width / size)
                ][
                (int) Math.ceil((double) height / size)
                ];

        for (int i = 0; i < itemFrames.length; i++) {
            for (int j = 0; j < itemFrames[i].length; j++) {
                Entity frame = new Entity(EntityType.ITEM_FRAME);
                // make this better eventually
                switch (direction) {
                    case WEST:
                        frame.setInstance(
                                instance,
                                pos.add(0, -j, -i)
                        );
                        break;
                    case EAST:
                        frame.setInstance(
                                instance,
                                pos.add(0, -j, i)
                        );
                        break;
                    case SOUTH:
                        frame.setInstance(
                                instance,
                                pos.add(i, -j, 0)
                        );
                        break;
                    case NORTH:
                    default:
                        frame.setInstance(
                                instance,
                                pos.add(-i, -j, 0)
                        );
                }

                ItemFrameMeta meta = (ItemFrameMeta) frame.getEntityMeta();
                meta.setDirection(direction);
                frame.setInvisible(invisible);

                frame.spawn();

                itemFrames[i][j] = frame;
            }
        }

        if (consumer != null)
            consumer.accept(this);

        return this;
    }

    public void render(BufferedImage[][] tiles) {
        int mapId = mapIdHolder.getMapId();
        int size = tileSize.getSize();

        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                Entity itemFrame = itemFrames[col][row];
                BufferedImage tile = tiles[row][col];

                LargeGraphics2DFramebuffer fb = new LargeGraphics2DFramebuffer(size, size);
                Graphics2D g = fb.getRenderer();

                g.setBackground(Color.BLACK);
                g.clearRect(0, 0, size, size);
                g.drawImage(tile, 0, 0, null);

                MapDataPacket packet = fb.preparePacket(mapId, 0, 0);

                ItemFrameMeta meta = (ItemFrameMeta) itemFrame.getEntityMeta();
                meta.setItem(ItemStack.of(Material.FILLED_MAP)
                        .with(DataComponents.MAP_ID, mapId));

                for (Player player : itemFrame.getViewers()) {
                    player.sendPacket(packet);
                }

                mapId++;
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
        for (Entity[] rows : itemFrames) {
            for (Entity frame : rows)  frame.remove();
        }
    }
}
