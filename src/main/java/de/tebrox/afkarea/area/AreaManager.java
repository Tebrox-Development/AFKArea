package de.tebrox.afkarea.area;

import de.tebrox.afkarea.integration.WorldGuardIntegration;
import de.tebrox.afkarea.region.CuboidRegionProvider;
import de.tebrox.afkarea.region.RegionProvider;
import de.tebrox.afkarea.region.WorldGuardRegionProvider;
import de.tebrox.afkarea.region.data.WorldGuardRegionData;
import de.tebrox.afkarea.reward.RewardConfigValidator;
import de.tebrox.vertexCore.database.Database;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

public final class AreaManager {
    private final JavaPlugin plugin;
    private final Database<AreaData> database;
    private final WorldGuardIntegration worldGuardIntegration;

    private final Map<String, AreaData> areas = new HashMap<>();
    private final List<RuntimeArea> runtimeAreas = new ArrayList<>();

    private boolean loaded;
    privat boolean loading;
    private long loadGeneration;
    private Runnable runtimeChangeListener = () -> {};


    public AreaManager(JavaPlugin plugin, Database<AreaData> database, WorldGuardIntegration worldGuardIntegration) {
        this.plugin = plugin;
        this.database = database;
        this.worldGuardIntegration = worldGuardIntegration;
    }

    public void loadAsync(){
        boolean preserveCurrentCache = loaded;
        long generation = ++loadGeneration;

        loading = true;

        database.loadObjectsAsyncMain(
                loadedAreas -> {
                    if(generation != loadGeneration) return;
                    loading = false;

                    try {
                        replaceCache(loadedAreas);
                    }catch(RuntimeException exception) {
                        loaded = preserveCurrentCache;

                        plugin.getLogger().severe(preserveCurrentCache ? "Failed to reload AFK areas. The previous runtime cache remains active: " + exception.getMessage() : "Failed to loda AFK areas: " + exception.get);
                        exception.printStackTrace();
                    }
                },
                error -> {
                    if(generation != loadGeneration) return;

                    loading = false;
                    loaded = preserveCurrentCache;

                    plugin.getLogger().severe(preserveCurrentCache ? "Failed to reload AFK areas. The previous runtime cache remains active: " + error.getMessage() : "Failed to loda AFK areas: " + exception.get);
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
        loadGeneration++;
        loading = false;

        areas.clear();
        runtimeAreas.clear();

        loaded = false;
    }

    public boolean isLoaded() {
        return loaded;
    }

    private IllegalStateException dataUnavailableException() {
        if(loading && loaded) {
            return new IllegalStateException("Area data is currently reloading");
        }
        return new IllegalStateException("Area data is still loading");
    }

    public void saveArea(AreaData area, Runnable onSuccess, Consumer<Throwable> onError) {
        if(!loaded) {
            onError.accept(dataUnavailableException());
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
            onError.accept(dataUnavailableException());
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

            for(String warning : RewardConfigValidator.validate(area.getRewards())) {
                plugin.getLogger().warning("AFK area '" + area.getUniqueId() + "' reward configuration: " + warning);
            }

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

        if(regionType.equalsIgnoreCase("worldguard")) {
            if(!worldGuardIntegration.isAvailable()) {
                plugin.getLogger().warning("AFK area '" + area.getUniqueId() + "' uses WorldGuard but WorldGuard is unavailable");
                return null;
            }

            WorldGuardRegionData region = area.getWorldGuardRegion();

            if(region == null) {
                plugin.getLogger().warning("AFK area '" + area.getUniqueId() + "' uses WorldGuard but has no WorldGuard region data");
                return null;
            }

            if(region.getWorld() == null || region.getWorld().isBlank() || region.getRegionId() == null || region.getRegionId().isBlank()) {
                plugin.getLogger().warning("AFK area '" + area.getUniqueId() + "' has invalid WorldGuard region data");
                return null;
            }

            WorldGuardRegionProvider provider = new WorldGuardRegionProvider(worldGuardIntegration, region.getWorld(), region.getRegionId(), region.isIncludeChildren());
            if(!provider.exists()) {
                plugin.getLogger().warning("Skipping AFK area '" + area.getUniqueId() + "': WorldGuard region '" + region.getRegionId() + "' does not exist in world '" + region.getWorld() + "'");
                return null;
            }

            return provider;
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

    public boolean worldGuardRegionExists(String worldName, String regionId) {
        if(!worldGuardIntegration.isAvailable()) return false;
        return new WorldGuardRegionProvider(worldGuardIntegration, worldName, regionId).exists();
    }
}
