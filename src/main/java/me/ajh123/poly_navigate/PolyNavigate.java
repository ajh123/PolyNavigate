package me.ajh123.poly_navigate;

import me.ajh123.poly_navigate.commands.CommandRegistry;
import me.ajh123.poly_navigate.map_object.MapDataRegistry;
import net.fabricmc.api.ModInitializer;
import java.util.logging.Logger;


public class PolyNavigate implements ModInitializer {
    public static final Logger LOGGER = Logger.getLogger("PolyNavigate");
    public static final String MODID = "poly_navigate";

    @Override
    public void onInitialize() {
        LOGGER.info("PolyNavigate is initializing...");

        // Initialize the template registry
        MapDataRegistry.onInitialize();

        CommandRegistry.registerCommands();

        // Log successful initialization
        LOGGER.info("PolyNavigate initialized successfully.");
    }
}
