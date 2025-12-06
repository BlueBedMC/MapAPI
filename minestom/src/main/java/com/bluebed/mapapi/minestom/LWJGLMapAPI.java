package com.bluebed.mapapi.minestom;

import com.bluebed.mapapi.core.MapIdHolder;
import com.bluebed.mapapi.core.MapTileSize;
import com.bluebed.mapapi.minestom.lwjgl.LargeGLFWFramebuffer;
import com.bluebed.mapapi.minestom.lwjgl.MapColorRenderer;
import com.bluebed.mapapi.minestom.lwjgl.WindowRunnable;
import lombok.AccessLevel;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
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
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.Direction;

import javax.management.InstanceNotFoundException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.function.Consumer;

@Getter
public class LWJGLMapAPI {
    private LargeGLFWFramebuffer framebuffer;
    private final MapIdHolder mapIdHolder = new MapIdHolder();

    @Getter(AccessLevel.PRIVATE)
    private Entity[][] itemFrames;
    private MapTileSize tileSize = MapTileSize.SMALL;
    private Instance instance;
    private Pos pos;
    private Direction direction;
    private int width = 1,
            height = 1;
    private long loopTime;
    private TemporalUnit loopUnit = ChronoUnit.MILLIS;
    private WindowRunnable tick;

    public static LWJGLMapAPI builder() {
        return new LWJGLMapAPI();
    }

    public LWJGLMapAPI tileSize(MapTileSize size) {
        this.tileSize = size;
        return this;
    }

    public LWJGLMapAPI mapSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public LWJGLMapAPI instance(Instance instance) {
        this.instance = instance;
        return this;
    }

    public LWJGLMapAPI pos(Pos pos) {
        this.pos = pos;
        return this;
    }

    public LWJGLMapAPI direction(Direction direction) {
        this.direction = direction;
        return this;
    }

    public LWJGLMapAPI repeat(long time, ChronoUnit unit) {
        this.loopTime = time;
        this.loopUnit = unit;
        return this;
    }

    public LWJGLMapAPI tick(WindowRunnable tick) {
        this.tick = tick;
        return this;
    }

    public LWJGLMapAPI build() throws InstanceNotFoundException {
        return build(null);
    }

    public LWJGLMapAPI build(Consumer<LWJGLMapAPI> consumer) throws InstanceNotFoundException {
        if (instance == null) throw new InstanceNotFoundException();
        if (direction == null) direction = Direction.NORTH;
        if (pos == null) return null;

        framebuffer = new LargeGLFWFramebuffer(width, height);
        int rows = (int) Math.ceil((double) height / 128);
        int cols = (int) Math.ceil((double) width / 128);
        itemFrames = new Entity[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Entity frame = new Entity(EntityType.ITEM_FRAME);

                int invRow = (rows - 1) - row;
                Pos framePos = switch (direction) {
                    case SOUTH -> pos.add(col, invRow, 0);
                    case WEST  -> pos.add(0, invRow, col);
                    case EAST  -> pos.add(0, invRow, (cols - 1) - col);
                    case UP    -> pos.add(col, 0, row);
                    case DOWN    -> pos.add(col, 0, invRow);
                    default    -> pos.add(-col, invRow, 0);
                };

                frame.setInstance(instance, framePos);

                ItemFrameMeta meta = (ItemFrameMeta) frame.getEntityMeta();
                meta.setDirection(direction);

                frame.spawn();
                itemFrames[row][col] = frame;
            }
        }

        SchedulerManager scheduler = MinecraftServer.getSchedulerManager();
        scheduler.buildTask(() -> {
            int mapId = mapIdHolder.getMapId();
            BufferedImage img = framebuffer.asBufferedImage();
            BufferedImage[][] tiles = slice(img);

            for (int row = 0; row < itemFrames.length; row++) {
                for (int col = 0; col < itemFrames[row].length; col++) {
                    Entity itemFrame = itemFrames[row][col];
                    BufferedImage tile = tiles[row][col];

                    LargeGraphics2DFramebuffer fb = new LargeGraphics2DFramebuffer(width, height);
                    Graphics2D g = fb.getRenderer();

                    g.setBackground(Color.BLACK);
                    g.clearRect(0, 0, width, height);
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
        }).repeat(loopTime, loopUnit).schedule();

        framebuffer.changeRenderingThreadToCurrent();
        MapColorRenderer renderer = new MapColorRenderer(framebuffer, getTick());
        framebuffer.unbindContextFromThread();
        framebuffer.setupRenderLoop(loopTime, loopUnit, renderer);

        if (consumer != null) consumer.accept(this);
        return this;
    }

    private BufferedImage[][] slice(BufferedImage image) {
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

    public void remove() {
        for (Entity[] rows : itemFrames) {
            for (Entity frame : rows)  frame.remove();
        }
        framebuffer.cleanup();
    }
}
