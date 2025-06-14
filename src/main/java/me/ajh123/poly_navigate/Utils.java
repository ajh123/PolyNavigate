package me.ajh123.poly_navigate;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class Utils {
    public static Identifier removePathAndExtension(Identifier original) {
        String path = original.getPath();
        // Extract the file name (after the last '/') and remove the file extension
        String newPath = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
        return Identifier.of(original.getNamespace(), newPath);
    }

    public static Identifier getWorldID(ServerCommandSource source) throws CommandSyntaxException {
        // Get the world ID from the server command source
        return source.getPlayerOrThrow().getWorld().getRegistryKey().getValue();
    }
}
