package me.ajh123.poly_navigate.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TestCommand implements Command {
    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommandBuilder() {
        return CommandManager.literal("second_command")
                .executes(context -> {
                    var source = context.getSource();
                    source.sendFeedback(() -> Text.literal("Second command executed!"), false);
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                });
    }
}
