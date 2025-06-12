package me.ajh123.poly_navigate.map_object;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import me.ajh123.poly_navigate.PolyNavigate;
import me.ajh123.poly_navigate.Utils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static me.ajh123.poly_navigate.PolyNavigate.LOGGER;
import static me.ajh123.poly_navigate.PolyNavigate.MODID;

public class MapDataRegistry {
    public static final String MAP_OBJECT_TEMPLATE_NAME = "map_object_template";
    public static final String MAP_OBJECT_TEMPLATE_PATH = MODID + "/" + MAP_OBJECT_TEMPLATE_NAME;
    private static final Map<Identifier, MapObjectTemplate> MAP_OBJECT_TEMPLATE_REGISTRY = new java.util.HashMap<>();

    public static final String MAP_OBJECT_TAG_NAME = "map_object_tag";
    public static final String MAP_OBJECT_TAG_PATH = MODID + "/" + MAP_OBJECT_TAG_NAME;
    private static final Map<Identifier, TagDefinition> MAP_OBJECT_TAG_REGISTRY = new java.util.HashMap<>();


    public static void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("Loading map object templates on initial startup...");
            loadAllData(server, server.getResourceManager());
        });

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, manager) -> {
            LOGGER.info("Reloading map object templates...");
            loadAllData(server, manager);
        });
    }

    private static void loadAllData(MinecraftServer server, ResourceManager manager) {
        MAP_OBJECT_TAG_REGISTRY.clear();
        MAP_OBJECT_TEMPLATE_REGISTRY.clear();

        var ops = RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager());

        var tagResources = manager.findResources(
                MAP_OBJECT_TAG_PATH,
                id -> id.getPath().endsWith(".json"));

        var templateResources = manager.findResources(
                MAP_OBJECT_TEMPLATE_PATH,
                id -> id.getPath().endsWith(".json"));

        // Load tags
        for (var res : tagResources.entrySet()) {
            try {
                TagDefinition tag = TagDefinition.CODEC.decode(ops, JsonParser.parseReader(res.getValue().getReader())).getOrThrow().getFirst();

                MAP_OBJECT_TAG_REGISTRY.put(Utils.removePathAndExtension(res.getKey()), tag);
            } catch (Throwable e) {
                PolyNavigate.LOGGER.warning("%s is invalid: %s".formatted(res.getKey(), e));
            }
        }

        // Load templates
        for (var res : templateResources.entrySet()) {
            try {
                MapObjectTemplate template = MapObjectTemplate.CODEC.decode(ops, JsonParser.parseReader(res.getValue().getReader())).getOrThrow().getFirst();

                MAP_OBJECT_TEMPLATE_REGISTRY.put(Utils.removePathAndExtension(res.getKey()), template);
            } catch (Throwable e) {
                PolyNavigate.LOGGER.warning("%s is invalid: %s".formatted(res.getKey(), e));
            }
        }
    }

    public static Map<Identifier, TagDefinition> getMapObjectTags() {
        return Collections.unmodifiableMap(MAP_OBJECT_TAG_REGISTRY);
    }

    public static Map<Identifier, MapObjectTemplate> getMapObjectTemplates() {
        return Collections.unmodifiableMap(MAP_OBJECT_TEMPLATE_REGISTRY);
    }

    public static Map<Identifier, MapObjectTemplate> getWayTemplates() {
        return MAP_OBJECT_TEMPLATE_REGISTRY.entrySet().stream()
                .filter(entry -> MapObjectType.WAY.equals(entry.getValue().type()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<Identifier, MapObjectTemplate> getNodeTemplates() {
        return MAP_OBJECT_TEMPLATE_REGISTRY.entrySet().stream()
                .filter(entry -> MapObjectType.NODE.equals(entry.getValue().type()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
