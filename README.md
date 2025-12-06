# MapAPI

A way to easily create and render map art for **Minestom** and **Spigot (WIP)**

## Credit
me - making it

## Pull Requests

**I am not the best developer in the world** (except when someone from google says that i am).
This means that this is most likely **NOT** the most optimised code in the world.
Please feel free to create improvements to the API, any notable improvements will be shown in the credits tab :)

## Installation

1. Clone the repo to your computer
2. Build & Publish to Local Maven
3. Use in your plugin

## Minestom usage

**Installation**
```gradle
implementation("com.bluebed.mapapi:api-core:<version>")
implementation("com.bluebed.mapapi:minestom:<version>")
```

**Usage**

*Regular MapAPI*
```java
new MapAPI()
    .pos(pos)
    .instance(instance)
    .direction(Direction.SOUTH)
    .mapSize(width, height)
    .tileSize(MapTileSize.SMALL)
    .build(mapApi -> {
        mapApi.render(getBufferedImage());
    });
```

*LWJGL MapAPI*
```java
LWJGLMapAPI lwjglMap = LWJGLMapAPI.builder()
    .pos(pos)
    .tileSize(MapTileSize.SMALL)
    .instance(instance)
    .direction(direction)
    .mapSize(width, height)
    .repeat(1000 / 60, ChronoUnit.MILLIS)
    .tick(new WindowRunnable() {
        @Override
        public void run(long window) {
            // lwjgl code here
        }
    }).build();
```

## Spigot Usage

**Installation**
```gradle
implementation("com.bluebed.mapapi:api-core:<version>")
implementation("com.bluebed.mapapi:spigot:<version>")
```

**Usage**

**WARNING**: Spigot is currently **extremely buggy and laggy** and should NOT be used unless forked and optimised heavily.
*Regular MapAPI*
```java
// call this to cancel the listener for hanging maps to break
new MapAPISpigot().init(this);

new MapAPI()
    .pos(new Location(Bukkit.getWorld("world"), 0, 50, 0))
    .direction(Direction.SOUTH)
    .mapSize(width, height)
    .tileSize(MapTileSize.SMALL)
    .build(mapApi -> {
        mapApi.renderAndRelease(getBufferedImage());
    });
```
