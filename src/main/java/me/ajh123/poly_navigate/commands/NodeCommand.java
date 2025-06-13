package me.ajh123.poly_navigate.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.ajh123.poly_navigate.Utils;
import me.ajh123.poly_navigate.commands.template.NodeTemplateArgumentType;
import me.ajh123.poly_navigate.commands.template.TemplateSpec;
import me.ajh123.poly_navigate.commands.template.TemplateSpecArgumentType;
import me.ajh123.poly_navigate.map_object.*;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class NodeCommand implements Command {
    private static final Map<ServerPlayerEntity, Node> nodeTracker = new HashMap<>();

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommandBuilder() {
        // this is the branch under /poly_navigate
        return CommandManager.literal("node")
                .then(applyTemplate())
                .then(beginNode()
                .then(selectNode()));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> applyTemplate() {
        return CommandManager.literal("apply")
                .then(CommandManager.argument("spec", NodeTemplateArgumentType.spec())
                        .suggests(TemplateSpecArgumentType.suggestTemplatesAndTags(MapObjectType.NODE))
                        .executes(ctx -> {
                            TemplateSpec spec = ctx.getArgument("spec", TemplateSpec.class);
                            ServerCommandSource source = ctx.getSource();

                            // Retrieve the currently tracked node
                            Node currentNode = nodeTracker.get(source.getPlayerOrThrow());
                            if (currentNode == null) {
                                source.sendError(Text.literal("No active node to apply the template to."));
                                return 0;
                            }

                            // Create a new Node with updated tags
                            Node updatedNode = new Node(
                                    currentNode.id(),
                                    currentNode.pos(),
                                    spec.tagValues()
                            );

                            // Update the node tracker
                            nodeTracker.put(source.getPlayerOrThrow(), updatedNode);

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

    private static LiteralArgumentBuilder<ServerCommandSource> beginNode() {
        return CommandManager.literal("begin")
                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                    .executes(ctx -> {
                        int nodeCount = GraphManager.getNodeCount(Utils.getWorldID(ctx.getSource()));

                        BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "pos");
                        Node node = new Node(nodeCount + 1, pos);

                        nodeTracker.put(ctx.getSource().getPlayerOrThrow(), node);

                        ctx.getSource().sendFeedback(() ->
                                        Text.literal("Started a new node at position " + pos +
                                                " with ID " + node.id()),
                                false
                        );

                        return 1;
                    })
                );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> selectNode() {
        return CommandManager.literal("select")
                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            int id = IntegerArgumentType.getInteger(ctx, "id");
                            Graph graph = GraphManager.getGraph(Utils.getWorldID(ctx.getSource()));

                            Node node = graph.getNode(id);

                            if (node == null) {
                                ctx.getSource().sendError(Text.literal("No node found with ID " + id));
                                return 0;
                            }

                            // Track the new node
                            nodeTracker.put(ctx.getSource().getPlayerOrThrow(), node);

                            ctx.getSource().sendFeedback(() ->
                                            Text.literal("Selected node with ID " + node.id()),
                                    false
                            );

                            return 1;
                        }));
    }
}