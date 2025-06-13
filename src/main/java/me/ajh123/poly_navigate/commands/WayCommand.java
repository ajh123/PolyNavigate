package me.ajh123.poly_navigate.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.ajh123.poly_navigate.Utils;
import me.ajh123.poly_navigate.commands.template.TemplateSpec;
import me.ajh123.poly_navigate.commands.template.TemplateSpecArgumentType;
import me.ajh123.poly_navigate.commands.template.WayTemplateArgumentType;
import me.ajh123.poly_navigate.map_object.Graph;
import me.ajh123.poly_navigate.map_object.GraphManager;
import me.ajh123.poly_navigate.map_object.MapObjectType;
import me.ajh123.poly_navigate.map_object.Way;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class WayCommand implements Command {
    private static final Map<ServerPlayerEntity, Way> wayTracker = new HashMap<>();

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommandBuilder() {
        // this is the branch under /poly_navigate
        return CommandManager.literal("way")
                .then(applyTemplate())
                .then(beginWay())
                .then(selectWay());
    }

    private static LiteralArgumentBuilder<ServerCommandSource> applyTemplate() {
        return CommandManager.literal("apply")
                .then(CommandManager.argument("spec", WayTemplateArgumentType.spec())
                        .suggests(TemplateSpecArgumentType.suggestTemplatesAndTags(MapObjectType.WAY))
                        .executes(ctx -> {
                            TemplateSpec spec = ctx.getArgument("spec", TemplateSpec.class);
                            ServerCommandSource source = ctx.getSource();

                            // Retrieve the currently tracked way
                            Way currentWay = wayTracker.get(source.getPlayerOrThrow());
                            if (currentWay == null) {
                                source.sendError(Text.literal("No active way to apply the template to."));
                                return 0;
                            }

                            // Create a new Way with updated tags
                            Way updatedWay = new Way(
                                    currentWay.id(),
                                    spec.tagValues()
                            );

                            // Update the way tracker
                            wayTracker.put(source.getPlayerOrThrow(), updatedWay);

                            // Send feedback to the user
                            source.sendFeedback(() ->
                                            Text.literal("Applied template " + spec.templateId()
                                                    + " with tags " + spec.tagValues()),
                                    false
                            );

                            return 1;
                        })
                );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> beginWay() {
        return CommandManager.literal("begin")
                .executes(ctx -> {
                    int wayCount = GraphManager.getWayCount(Utils.getWorldID(ctx.getSource()));

                    // Create a new Way with a unique ID
                    Way way = new Way(wayCount + 1);

                    // Track the new way
                    wayTracker.put(ctx.getSource().getPlayerOrThrow(), way);

                    ctx.getSource().sendFeedback(() ->
                                    Text.literal("Started a new way with ID " + way.id()),
                            false
                    );

                    return 1;
                });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> selectWay() {
        return CommandManager.literal("select")
                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                .executes(ctx -> {
                    int id = IntegerArgumentType.getInteger(ctx, "id");
                    Graph graph = GraphManager.getGraph(Utils.getWorldID(ctx.getSource()));

                    Way way = graph.getWay(id);

                    if (way == null) {
                        ctx.getSource().sendError(Text.literal("No way found with ID " + id));
                        return 0;
                    }

                    // Track the new way
                    wayTracker.put(ctx.getSource().getPlayerOrThrow(), way);

                    ctx.getSource().sendFeedback(() ->
                                    Text.literal("Selected way with ID " + way.id()),
                            false
                    );

                    return 1;
                }));
    }
}