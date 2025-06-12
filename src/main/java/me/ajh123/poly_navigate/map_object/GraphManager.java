package me.ajh123.poly_navigate.map_object;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class GraphManager {
    private static final Map<Identifier, Graph> graphRegistry = new HashMap<>();

    public static void onInitialize() {
        ServerLifecycleEvents.AFTER_SAVE.register(((server, flush, force) -> {
            Iterable<ServerWorld> worlds = server.getWorlds();

            for (ServerWorld world : worlds) {
                Identifier dimensionId = world.getRegistryKey().getValue();
                if (!graphRegistry.containsKey(dimensionId)) {
                    graphRegistry.put(dimensionId, new Graph(dimensionId));
                }
            }
        }));
    }

    public static Graph getGraph(Identifier dimension) {
        return graphRegistry.get(dimension);
    }
}
