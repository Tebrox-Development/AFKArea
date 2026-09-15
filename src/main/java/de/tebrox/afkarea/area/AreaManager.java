package de.tebrox.afkarea.area;

import de.tebrox.afkarea.region.CuboidRegionProvider;
import de.tebrox.afkarea.region.RegionProvider;
import de.tebrox.vertexCore.database.Database;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

public final class AreaManager {
    private final JavaPlugin plugin;
    private final Database<AreaData> database;

    private final Map<String, AreaData> areas = new HashMap<>();
    private final List<RuntimeArea> runtimeAreas = new ArrayList<>();

    private boolean loaded;
    private Runnable runtimeChangeListener = () -> {};

    public AreaManager(JavaPlugin plugin, Database<AreaData> database) {
        this.plugin = plugin;
        this.database = database;
    }

    public void loadAsync(){
        loaded = false;

        database.loadObjectsAsyncMain(
                this::replaceCache,
                error -> {
                    loaded = false;
                    plugin.getLogger().severe("Failed to load AFK areas: " + error.getMessage());
                    error.printStackTrace();
                });
    }

    private void replaceCache(Collection<AreaData>loadedAreas) {
        areas.clear();

        for(AreaData area : loadedAreas) {
            String id = area.getUniqueId();

            if(id == null || id.isBlank()) {
                plugin.getLogger().warning("Skipping AFK area with missing unique ID");
                continue;
            }

            areas.put(id, area);
        }

        rebuildRuntimeAreas();
        loaded = true;
        runtimeChangeListener.run();

        if(areas.size() > 1 || areas.isEmpty()) {
            plugin.getLogger().info("Loaded " + areas.size() + " AFK areas");
        }else{
            plugin.getLogger().info("Loaded " + areas.size() + " AFK area");
        }
    }

    public AreaData getArea(String id) {
        return areas.get(id);
    }

    public boolean hasArea(String id) {
        return areas.containsKey(id);
    }

    public Collection<AreaData> getAreas() {
        return List.copyOf(areas.values());
    }

    public void clear() {
        areas.clear();
        runtimeAreas.clear();
        loaded = false;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void saveArea(AreaData area, Runnable onSuccess, Consumer<Throwable> onError) {
        if(!loaded) {
            onError.accept(new IllegalStateException("Area data is still loading"));
            return;
        }

        String id = area.getUniqueId();
        if(id == null || id.isBlank()) {
            onError.accept(new IllegalArgumentException("Area unique ID must not be blank"));
            return;
        }

        database.saveObjectAsyncMain(area, () -> {
            areas.put(id, area);
            rebuildRuntimeAreas();

            onSuccess.run();
            runtimeChangeListener.run();
        }, onError);
    }

    public void deleteArea(String id, Runnable onSuccess, Consumer<Throwable> onError) {
        if(!loaded) {
            onError.accept(new IllegalStateException("Area is still loading"));
            return;
        }

        database.supplyAsyncMain(database.deleteObjectAsync(id), (ignored, error) -> {
            if(error != null) {
                onError.accept(error);
                return;
            }

            areas.remove(id);
            rebuildRuntimeAreas();

            onSuccess.run();
            runtimeChangeListener.run();
        });
    }

    private record RuntimeArea(AreaData area, RegionProvider region) {}

    private void rebuildRuntimeAreas() {
        runtimeAreas.clear();

        for(AreaData area : areas.values()) {
            if(!area.isEnabled()) continue;

            RegionProvider provider = createRegionProvider(area);

            if(provider == null) continue;

            runtimeAreas.add(new RuntimeArea(area, provider));
        }

        runtimeAreas.sort(Comparator.comparingInt((RuntimeArea runtime) -> runtime.area().getPriority()).reversed().thenComparing(runtime -> runtime.area().getUniqueId()));
    }

    public void setRuntimeChangeListener(Runnable runtimeChangeListener) {
        this.runtimeChangeListener = runtimeChangeListener == null ? () -> {} : runtimeChangeListener;
    }

    private RegionProvider createRegionProvider(AreaData area) {
        String regionType = area.getRegionType();

        if(regionType == null || regionType.isBlank()) {
            plugin.getLogger().warning("AFK area '" + area.getUniqueId() + "' has no region type");
            return null;
        }

        if(regionType.equalsIgnoreCase("cuboid")) {
            if(area.getCuboidRegion() == null) {
                plugin.getLogger().warning("AFK area '" + area.getUniqueId() + "' uses cuboid region type but has no cuboid data");
                return null;
            }

            return new CuboidRegionProvider(area.getCuboidRegion());
        }

        plugin.getLogger().warning("AFK area '" + area.getUniqueId() + "' uses unsupported region type '" + regionType + "'");
        return null;
    }

    public Optional<AreaData> findArea(Location location) {
        if(!loaded || location == null) return Optional.empty();

        for(RuntimeArea runtime : runtimeAreas) {
            if(runtime.region().contains(location)) return Optional.of(runtime.area());
        }

        return Optional.empty();
    }
}
