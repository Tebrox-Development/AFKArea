package de.tebrox.afkarea.area;

import de.tebrox.vertexCore.database.Database;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class AreaManager {
    private final JavaPlugin plugin;
    private final Database<AreaData> database;

    private final Map<String, AreaData> areas = new HashMap<>();
    private boolean loaded;

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

        loaded = true;

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
            onSuccess.run();
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
            onSuccess.run();
        });
    }
}
