package com.bluebed.mapapi.core;

import lombok.AccessLevel;
import lombok.Getter;

import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public abstract class AbstractMapAPI {
    protected final MapIdHolder mapIdHolder = new MapIdHolder();

    @Getter(AccessLevel.PRIVATE)
    protected MapTileSize tileSize = MapTileSize.SMALL;
    protected int width = MapTileSize.SMALL.getSize(),
            height = MapTileSize.SMALL.getSize();
    protected boolean invisible = false;

    public AbstractMapAPI tileSize(MapTileSize size) {
        this.tileSize = size;
        return this;
    }

    public AbstractMapAPI mapSize(int width, int height) {
        this.height = height;
        this.width = width;
        return this;
    }

    public AbstractMapAPI invisible(boolean invisible) {
        this.invisible = invisible;
        return this;
    }

    public AbstractMapAPI build() throws InstanceNotFoundException {
        return build(null);
    }

    public void render(BufferedImage image) {
        render(slice(image));
    }

    protected abstract AbstractMapAPI build(Consumer<AbstractMapAPI> consumer) throws InstanceNotFoundException;
    public abstract void render(BufferedImage[][] tiles);
    protected abstract BufferedImage[][] slice(BufferedImage image);
    public abstract void remove();
}
