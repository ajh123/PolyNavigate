package me.ajh123.poly_navigate.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

public interface Command {
    LiteralArgumentBuilder<ServerCommandSource> getCommandBuilder();
}
