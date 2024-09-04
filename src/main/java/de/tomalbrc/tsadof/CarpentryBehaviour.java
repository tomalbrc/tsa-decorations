package de.tomalbrc.tsadof;

import de.tomalbrc.filament.api.behaviour.decoration.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.virtual.FakeScreenHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CarpentryBehaviour implements DecorationBehaviour<CarpentryBehaviour.CarpentryBenchConfig> {
    private final CarpentryBenchConfig config;

    public CarpentryBehaviour(CarpentryBenchConfig config) {
        this.config = config;
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        CarpentryGui gui = new CarpentryGui(MenuType.GENERIC_9x6, player, false);
        gui.open();

        return InteractionResult.CONSUME;
    }

    @Override
    public CarpentryBenchConfig getConfig() {
        return this.config;
    }

    public class CarpentryBenchConfig {
    }
}
