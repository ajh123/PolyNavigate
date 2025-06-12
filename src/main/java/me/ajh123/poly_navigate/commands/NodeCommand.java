package me.ajh123.poly_navigate.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.ajh123.poly_navigate.commands.template.NodeTemplateArgumentType;
import me.ajh123.poly_navigate.commands.template.TemplateSpec;
import me.ajh123.poly_navigate.commands.template.TemplateSpecArgumentType;
import me.ajh123.poly_navigate.map_object.MapObjectType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class NodeCommand implements Command {
    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommandBuilder() {
        // this is the branch under /poly_navigate
        return CommandManager.literal("node")
                .then(applyTemplate());
    }

    private static LiteralArgumentBuilder<ServerCommandSource> applyTemplate() {
        return CommandManager.literal("apply")
                .then(CommandManager.argument("spec", NodeTemplateArgumentType.spec())
                        .suggests(TemplateSpecArgumentType.suggestTemplatesAndTags(MapObjectType.NODE))
                        .executes(ctx -> {
                            TemplateSpec spec = ctx.getArgument("spec", TemplateSpec.class);
                            ctx.getSource().sendFeedback(() ->
                                    Text.literal("Applying " + spec.templateId()
                                            + " with " + spec.tagValues()),
                                    false
                            );
                            return 1;
                        })
                );
    }
}