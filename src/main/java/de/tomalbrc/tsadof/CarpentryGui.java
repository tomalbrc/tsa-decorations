package de.tomalbrc.tsadof;

import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class CarpentryGui extends SimpleGui {

    private final SimpleContainer container;
    private final Slot baseSlot;
    private final Slot slot;
    private final Slot trimSlot;

    private final CarpentryResultSlot resultSlot;

    private int scrollIndex = 0;
    private static final int WIDTH = 4;
    private static final int HEIGHT = 6;

    public CarpentryGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots) {
        super(type, player, manipulatePlayerSlots);

        this.setTitle(Component.literal("CarpentryGui"));

        this.container = getSimpleContainer(player);

        for (int i = 0; i < this.getSize(); i++) {
            this.setSlot(i, Items.BLUE_STAINED_GLASS_PANE.getDefaultInstance());
        }

        this.baseSlot = new Slot(this.container, 0, 0, 0);
        this.slot =     new Slot(this.container, 1, 0, 0);
        this.trimSlot = new Slot(this.container, 2, 0, 0);
        this.resultSlot = new CarpentryResultSlot(container, 3, 0, 0, this.consumeCallback());

        BiFunction<Integer, Integer, Integer> f = (x, y) -> x + y*9;

        this.setSlotRedirect(f.apply(0,1), baseSlot);
        this.setSlotRedirect(f.apply(1,1), slot);
        this.setSlotRedirect(f.apply(2,1), trimSlot);
        this.setSlotRedirect(f.apply(1,4), resultSlot);

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 4; x++) {
                this.setSlot(f.apply(4+x,y), Items.BROWN_STAINED_GLASS_PANE.getDefaultInstance());
            }
            this.setSlot(f.apply(8, y), Items.BLACK_STAINED_GLASS_PANE.getDefaultInstance());
        }

        this.setSlot(f.apply(8,0), Items.SUNFLOWER.getDefaultInstance(), (x,y,clickType) -> {
            TSADOF.LOGGER.warn("up");
            scrollIndex = Math.max(0, --scrollIndex);
        });
        this.setSlot(f.apply(8,5), Items.SUNFLOWER.getDefaultInstance(), (x,y,clickType) -> {
            TSADOF.LOGGER.warn("down");
            var items = this.availableItems();
            if (items.size() > WIDTH*HEIGHT) {

            }
        });
    }

    private List<ItemStack> availableItems() {
        List<ItemStack> list = new ObjectArrayList<>();

        CraftingInput input = CraftingInput.of(1, 3, List.of(container.getItem(0), container.getItem(1), container.getItem(2)));
        List<RecipeHolder<CarpentryRecipe>> recipes = this.player.level().getRecipeManager().getRecipesFor(CarpentryRecipe.Type.INSTANCE, input, player.level());
        boolean matches = false;
        if (recipes != null && !recipes.isEmpty()) {
            for (RecipeHolder<CarpentryRecipe> recipe : recipes) {
                ItemStack res = recipe.value().getResult();
                if (res != null && !res.isEmpty()) {
                    list.add(res);
                }
            }
        }
        return list;
    }

    private void updateItemList(List<ItemStack> items) {
        int offset = scrollIndex*9;
        int start = 4;
        for (int column = 0; column < HEIGHT; column++) {
            for (int row = 0; row < WIDTH; row++) {
                int index = row+column*9;
                ItemStack item = items.get(index);
                this.setSlot(start+index, item.copy());
            }
        }

    }


    @NotNull
    private SimpleContainer getSimpleContainer(ServerPlayer player) {
        SimpleContainer simpleContainer = new SimpleContainer(4);
        simpleContainer.addListener(container -> {
            var input = CraftingInput.of(1, 3, List.of(container.getItem(0), container.getItem(1), container.getItem(2)));
            List<RecipeHolder<CarpentryRecipe>> recipes = player.level().getRecipeManager().getRecipesFor(CarpentryRecipe.Type.INSTANCE, input, player.level());
            boolean matches = false;
            if (recipes != null && !recipes.isEmpty()) {
                for (RecipeHolder<CarpentryRecipe> recipe : recipes) {
                    var res = recipe.value().getResult();
                    if (res != null && !res.isEmpty()) {
                        if (!ItemStack.matches(container.getItem(this.resultSlot.getContainerSlot()), res)) {
                            container.setItem(this.resultSlot.getContainerSlot(), res.copy());
                        }
                        matches = true;
                    }
                }
            }

            if (!container.getItem(this.resultSlot.getContainerSlot()).isEmpty() && !matches) {
                container.setItem(this.resultSlot.getContainerSlot(), ItemStack.EMPTY);
            }
        });
        return simpleContainer;
    }

    @Override
    public ItemStack quickMove(int index) {
        if (index == this.resultSlot.index) {
            this.consumeCallback().run();
        }

        return super.quickMove(index);
    }

    public Runnable consumeCallback() {
        return () -> {
            this.baseSlot.getItem().shrink(1);
            this.slot.getItem().shrink(1);
            this.trimSlot.getItem().shrink(1);
        };
    }
}
