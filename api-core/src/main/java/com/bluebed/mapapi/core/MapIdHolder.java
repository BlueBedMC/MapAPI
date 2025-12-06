package com.bluebed.mapapi.core;

import lombok.Getter;

@Getter
public class MapIdHolder {
    private static int globalMapId = 5;
    private final int mapId;

    public MapIdHolder() {
        mapId = globalMapId * 100;
        globalMapId++;
    }
}
