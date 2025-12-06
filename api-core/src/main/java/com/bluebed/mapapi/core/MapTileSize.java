package com.bluebed.mapapi.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MapTileSize {
    SMALL(128),
    MEDIUM(256),
    LARGE(512),
    EXTRA_LARGE(1024),
    SERVER_CRASHER(2048);

    private final int size;
}
