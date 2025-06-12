package me.ajh123.poly_navigate.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.ajh123.poly_navigate.map_object.MapDataRegistry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ListTagCommand implements Command {
    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommandBuilder() {
        return CommandManager.literal("list_tags")
                .executes(context -> {
                    var source = context.getSource();
                    var tags = MapDataRegistry.getMapObjectTags();

                    if (tags.isEmpty()) {
                        source.sendFeedback(() -> Text.literal("No tags found."), false);
                    } else {
                        source.sendFeedback(() -> Text.literal("Tags:"), false);
                        for (Identifier id : tags.keySet()) {
                            source.sendFeedback(() -> Text.literal("- " + id.toString()), false);
                        }
                    }

                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                });
    }
}
