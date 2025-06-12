package me.ajh123.poly_navigate.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.ajh123.poly_navigate.map_object.MapDataRegistry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ListTemplateCommand implements Command {
    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommandBuilder() {
        return CommandManager.literal("list_templates")
                .executes(context -> {
                    var source = context.getSource();
                    var templates = MapDataRegistry.getMapObjectTemplates();

                    if (templates.isEmpty()) {
                        source.sendFeedback(() -> Text.literal("No templates found."), false);
                    } else {
                        source.sendFeedback(() -> Text.literal("Templates:"), false);
                        for (Identifier id : templates.keySet()) {
                            source.sendFeedback(() -> Text.literal("- " + id.toString()), false);
                        }
                    }

                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                });
    }
}
