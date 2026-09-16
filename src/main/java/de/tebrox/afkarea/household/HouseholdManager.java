package de.tebrox.afkarea.household;

import de.tebrox.vertexCore.database.Database;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.C;

import java.util.*;
import java.util.function.Consumer;

public final class HouseholdManager {
    private final JavaPlugin plugin;
    private final Database<HouseholdData> database;

    private final Map<String, HouseholdData> households = new HashMap<>();
    private final Map<UUID, String> householdByMember = new HashMap<>();
    private final Set<UUID> pendingMembers = new HashSet<>();
    private boolean loaded;


    public HouseholdManager(JavaPlugin plugin, Database<HouseholdData> database) {
        this.plugin = plugin;
        this.database = database;
    }

    public void loadAsync() {
        loaded = false;

        database.loadObjectsAsyncMain(
                this::replaceCache,
                error -> {
                    plugin.getLogger().severe("Failed to load household data: " + error.getMessage());
                    error.printStackTrace();
                });
    }

    private void replaceCache(Collection<HouseholdData> loadedHouseholds) {
        households.clear();
        householdByMember.clear();

        List<HouseholdData> sorted = new ArrayList<>(loadedHouseholds);
        sorted.sort(Comparator.comparing(HouseholdData::getUniqueId, Comparator.nullsLast(String::compareTo)));

        for(HouseholdData raw : sorted) {
            String id = raw.getUniqueId();

            if(id == null || id.isBlank()) {
                plugin.getLogger().warning("Skipping household with missing unique ID");
                continue;
            }

            Set<UUID> members = parseMembers(raw);

            if(members.size() < 2) {
                plugin.getLogger().warning("Skipping household '" + id + "' because it has fewer than two valid members");
                continue;
            }

            boolean conflict = members.stream().anyMatch(householdByMember::containsKey);
            if(conflict) {
                plugin.getLogger().warning("Skipping household '" + id + "' because at least one player already belongs to another household");
            }

            HouseholdData household = new HouseholdData(id, toStrings(members));
            households.put(id, household);

            for(UUID member : members) {
                householdByMember.put(member, id);
            }
            loaded = true;

            plugin.getLogger().info("Loaded " + households.size() + " household" + (households.size() == 1 ? "" : "s"));
        }
    }

    public void link(UUID first, UUID second, Consumer<LinkResult> onResult, Consumer<Throwable> onError) {
        if(!loaded) {
            onResult.accept(LinkResult.DATA_LOADING);
            return;
        }

        if(first.equals(second)) {
            onResult.accept(LinkResult.SAME_PLAYER);
            return;
        }

        String firstHousehold = householdByMember.get(first);
        String secondHousehold = householdByMember.get(second);

        if(firstHousehold != null && secondHousehold != null) {
            if(firstHousehold.equals(secondHousehold)) {
                onResult.accept(LinkResult.ALREADY_SAME_HOUSEHOLD);
            }else{
                onResult.accept(LinkResult.DIFFERENT_HOUSEHOLDS);
            }

            return;
        }

        HouseholdData updated;
        Set<UUID> affected = new HashSet<>();

        if(firstHousehold == null && secondHousehold == null) {
            affected.add(first);
            affected.add(second);

            updated = new HouseholdData(UUID.randomUUID().toString(), toStrings(affected));
        }else{
            String householdId = firstHousehold != null ? firstHousehold : secondHousehold;
            UUID newMember = firstHousehold == null ? first : second;
            HouseholdData current = households.get(householdId);

            if(current == null){
                onResult.accept(LinkResult.DATA_LOADING);
                return;
            }

            affected.addAll(parseMembers(current));
            affected.add(newMember);

            updated = current.copy();
            updated.setMembers(toStrings(affected));
        }

        if(!reserve(affected)) {
            onResult.accept(LinkResult.BUSY);
            return;
        }

        database.saveObjectAsyncMain(updated,
                () -> {
                    release(affected);
                    cacheHousehold(updated);

                    onResult.accept(LinkResult.LINKED);
                },
                error -> {
                    release(affected);
                    onError.accept(error);
                });
    }

    public void unlink(UUID playerId, Consumer<UnlinkResult> onResult, Consumer<Throwable> onError) {
        if (!loaded) {
            onResult.accept(UnlinkResult.DATA_LOADING);
            return;
        }

        String householdId = householdByMember.get(playerId);

        if (householdId == null) {
            onResult.accept(UnlinkResult.NOT_LINKED);
            return;
        }

        HouseholdData current = households.get(householdId);

        if (current == null) {
            onResult.accept(UnlinkResult.NOT_LINKED);
            return;
        }

        Set<UUID> affected = parseMembers(current);
        if (!reserve(affected)) {onResult.accept(UnlinkResult.BUSY);
            return;
        }

        Set<UUID> remaining = new HashSet<>(affected);
        remaining.remove(playerId);

        if (remaining.size() < 2) {
            database.supplyAsyncMain(
                    database.deleteObjectAsync(
                            householdId
                    ),
                    (ignored, error) -> {
                        release(affected);

                        if (error != null) {
                            onError.accept(error);
                            return;
                        }
                        removeHousehold(householdId);

                        onResult.accept(UnlinkResult.UNLINKED);
                    }
            );

            return;
        }

        HouseholdData updated = current.copy();
        updated.setMembers(toStrings(remaining));

        database.saveObjectAsyncMain(
                updated,
                () -> {
                    release(affected);
                    cacheHousehold(updated);

                    onResult.accept(UnlinkResult.UNLINKED);
                },
                error -> {
                    release(affected);
                    onError.accept(error);
                }
        );
    }

    public Set<UUID> membersOf(UUID playerId) {
        String householdId = householdByMember.get(playerId);

        if (householdId == null) return Set.of();
        HouseholdData household = households.get(householdId);

        if (household == null) return Set.of();

        return Set.copyOf(parseMembers(household));
    }

    public String householdIdentity(UUID playerId) {
        return householdByMember.getOrDefault(playerId, playerId.toString());
    }

    public Collection<HouseholdData> getHouseholds() {
        return List.copyOf(households.values());
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void clear() {
        households.clear();
        householdByMember.clear();
        pendingMembers.clear();
        loaded = false;
    }

    private boolean reserve(Set<UUID> members) {
        for (UUID member : members) {
            if (pendingMembers.contains(member)) {
                return false;
            }
        }

        pendingMembers.addAll(members);
        return true;
    }

    private void release(Set<UUID> members) {
        pendingMembers.removeAll(members);
    }

    private void cacheHousehold(HouseholdData household) {
        String id = household.getUniqueId();
        HouseholdData previous = households.put(id, household);

        if (previous != null) {
            for (UUID member : parseMembers(previous)) {
                householdByMember.remove(member, id);
            }
        }

        for (UUID member : parseMembers(household)) {
            householdByMember.put(member, id);
        }
    }

    private void removeHousehold(String householdId) {
        HouseholdData removed = households.remove(householdId);

        if (removed == null) return;
        for (UUID member : parseMembers(removed)) {
            householdByMember.remove(member, householdId);
        }
    }

    private Set<UUID> parseMembers(HouseholdData household) {
        Set<UUID> members = new LinkedHashSet<>();

        if (household.getMembers() == null) {
            return members;
        }

        for (String raw : household.getMembers()) {
            if (raw == null || raw.isBlank()) continue;

            try {
                members.add(UUID.fromString(raw));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Ignoring invalid household member UUID '" + raw + "'");
            }
        }

        return members;
    }

    private List<String> toStrings(Collection<UUID> members) {
        List<String> result = new ArrayList<>();

        for (UUID member : members) {
            result.add(member.toString());
        }
        result.sort(String::compareTo);

        return result;
    }


    public enum LinkResult {
        LINKED,
        SAME_PLAYER,
        ALREADY_SAME_HOUSEHOLD,
        DIFFERENT_HOUSEHOLDS,
        DATA_LOADING,
        BUSY
    }

    public enum UnlinkResult {
        UNLINKED,
        NOT_LINKED,
        DATA_LOADING,
        BUSY
    }
}
