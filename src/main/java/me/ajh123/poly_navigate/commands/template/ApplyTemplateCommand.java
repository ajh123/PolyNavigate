package me.ajh123.poly_navigate.commands.template;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.ajh123.poly_navigate.commands.Command;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ApplyTemplateCommand implements Command {
    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommandBuilder() {
        // this is the branch under /poly_navigate
        return CommandManager.literal("apply")
                .then(CommandManager.argument("spec", TemplateSpecArgumentType.spec())
                        .suggests(TemplateSpecArgumentType.suggestTemplatesAndTags())
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