package me.ajh123.poly_navigate.commands;

import me.ajh123.poly_navigate.PolyNavigate;
import me.ajh123.poly_navigate.commands.template.NodeTemplateArgumentType;
import me.ajh123.poly_navigate.commands.template.WayTemplateArgumentType;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;

import java.util.List;

public class CommandRegistry {
    private static final List<Command> commands = List.of(
            new ListTemplateCommand(),
            new ListTagCommand(),
            new TestCommand(),
            new NodeCommand(),
            new WayCommand()
    );

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var rootCommand = CommandManager.literal("poly_navigate");

            for (Command command : commands) {
                rootCommand.then(command.getCommandBuilder());
            }

            dispatcher.register(rootCommand);
        });

        ArgumentTypeRegistry.registerArgumentType(
                Identifier.of(PolyNavigate.MODID, "template_spec_node"),
                NodeTemplateArgumentType.class,
                ConstantArgumentSerializer.of(NodeTemplateArgumentType::new)
        );

        ArgumentTypeRegistry.registerArgumentType(
                Identifier.of(PolyNavigate.MODID, "template_spec_way"),
                WayTemplateArgumentType.class,
                ConstantArgumentSerializer.of(WayTemplateArgumentType::new)
        );
    }
}