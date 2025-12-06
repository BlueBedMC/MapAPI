package com.bluebed.mapapi.core;

import lombok.AccessLevel;
import lombok.Getter;

import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public abstract class AbstractMapAPI<T extends AbstractMapAPI<T>> {
    protected final MapIdHolder mapIdHolder = new MapIdHolder();

    @Getter(AccessLevel.PRIVATE)
    protected MapTileSize tileSize = MapTileSize.SMALL;
    protected int width = MapTileSize.SMALL.getSize(), height = MapTileSize.SMALL.getSize();
    protected boolean invisible = false;

    public T tileSize(MapTileSize size) {
        this.tileSize = size;
        return self();
    }

    public T mapSize(int width, int height) {
        this.width = width;
        this.height = height;
        return self();
    }

    public T invisible(boolean invisible) {
        this.invisible = invisible;
        return self();
    }

    public T build() throws InstanceNotFoundException {
        return build(null);
    }

    protected abstract T self();

    public void render(BufferedImage image) {
        render(slice(image));
    }

    public abstract T build(Consumer<T> consumer) throws InstanceNotFoundException;
    public abstract void render(BufferedImage[][] tiles);
    protected abstract BufferedImage[][] slice(BufferedImage image);
    public abstract void remove();
}
