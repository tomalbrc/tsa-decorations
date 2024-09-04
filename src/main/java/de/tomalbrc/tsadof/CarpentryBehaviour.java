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
        var gui = new SimpleGui(MenuType.GENERIC_9x3, player, false) {

        };
        gui.open();

        SimpleContainer container = getSimpleContainer(player);
        Slot baseSlot = new Slot(container, 0, 0, 0);
        Slot slot =     new Slot(container, 1, 0, 0);
        Slot trimSlot = new Slot(container, 2, 0, 0);

        gui.setSlotRedirect(1, baseSlot);
        gui.setSlotRedirect(2, slot);
        gui.setSlotRedirect(3, trimSlot);

        gui.setSlotRedirect(4, new Slot(container, 3, 0, 0) {
            @Override
            public boolean isFake() {
                return true;
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                super.onTake(player, itemStack);

                if (!itemStack.isEmpty()) {
                    baseSlot.getItem().shrink(1);
                    slot.getItem().shrink(1);
                    trimSlot.getItem().shrink(1);
                }
            }

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }
        });

        gui.setSlot(0, Items.BLUE_STAINED_GLASS_PANE.getDefaultInstance());
        for (int i = 5; i < 9 * 3; i++) {
            gui.setSlot(i, Items.BLUE_STAINED_GLASS_PANE.getDefaultInstance());
        }

        return InteractionResult.CONSUME;
    }

    @NotNull
    private static SimpleContainer getSimpleContainer(ServerPlayer player) {
        SimpleContainer simpleContainer = new SimpleContainer(27);
        simpleContainer.addListener(container -> {
            var input = CraftingInput.of(1, 3, List.of(container.getItem(0), container.getItem(1), container.getItem(2)));
            List<RecipeHolder<CarpentryRecipe>> recipes = player.level().getRecipeManager().getRecipesFor(CarpentryRecipe.Type.INSTANCE, input, player.level());
            boolean matches = false;
            if (recipes != null && !recipes.isEmpty()) {
                for (RecipeHolder<CarpentryRecipe> recipe : recipes) {
                    var res = recipe.value().getResult();
                    if (res != null && !res.isEmpty()) {
                        if (!ItemStack.matches(container.getItem(3), res)) {
                            container.setItem(3, res.copy());
                        }
                        matches = true;
                    }
                }
            }

            if (!container.getItem(3).isEmpty() && !matches) {
                container.setItem(3, ItemStack.EMPTY);
            }
        });
        return simpleContainer;
    }

    @Override
    public CarpentryBenchConfig getConfig() {
        return this.config;
    }

    public class CarpentryBenchConfig {
    }
}
