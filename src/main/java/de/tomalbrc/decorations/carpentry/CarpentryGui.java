package de.tomalbrc.decorations.carpentry;

import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Prediction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public class CarpentryGui extends SimpleGui {
    private final SimpleContainer container;
    private final CarpentryInputSlot baseSlot;
    private final CarpentryInputSlot slot;
    private final CarpentryInputSlot trimSlot;

    private final CarpentryResultSlot resultSlot;

    private int scrollIndex = 0;
    private int selX = 0;
    private int selY = -1;

    @Nullable
    private CarpentryRecipe selectedRecipe;

    private Runnable closeCB = null;

    private static final int WIDTH = 4;
    private static final int HEIGHT = 6;

    private Component buildGuiTitleComponent(int column, int row, float percentage) {
        // flip column
        column = (WIDTH - 1) - column;

        // shift cursor around
        String colAraw = "(".repeat(column);
        String colDraw = ")".repeat(column);

        // row representation
        String rowRaw = (row >= 0 && row < 6)
                ? Character.toString((char)(0xF700 | row))
                : "___>";

        // scroll indicator
        String scrollRaw = percentage == -1
                ? "\uF000"
                : Character.toString((char)(0xF800 | (int)((0x1C) * percentage)));

        MutableComponent colA = Component.literal(colAraw);
        MutableComponent rowComp = Component.literal(rowRaw);
        MutableComponent colB = Component.literal(colDraw);
        MutableComponent scrollComp = Component.literal(scrollRaw);

        Style baseStyle = Style.EMPTY
                .withColor(0xFF_FF_FF)
                .withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("tsadecorations", "ui")));

        MutableComponent prefix = Component.literal("<U<xx----").setStyle(baseStyle);
        MutableComponent middle = Component.literal("____--").setStyle(baseStyle);
        MutableComponent suffix = Component.literal("<<xxxxxxxxx").setStyle(baseStyle);

        return Component.empty()
                .append(prefix)
                .append(colA.setStyle(baseStyle))
                .append(rowComp.setStyle(baseStyle))
                .append(colB.setStyle(baseStyle))
                .append(middle)
                .append(scrollComp.setStyle(baseStyle))
                .append(suffix);
    }

    private void updateTitle(int column, int row, List<CarpentryRecipe> recipes) {
        int rows = (int) Math.ceil(recipes.size()/WIDTH);
        int hidden = rows - (HEIGHT-1);
        float scrollProgress = Math.min(1.0f, Math.max(0.0f, this.scrollIndex / (float) hidden));

        this.setTitle(this.buildGuiTitleComponent(column, row, hidden <= 0 ? -1 : scrollProgress));
    }

    public CarpentryGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots) {
        super(type, player, manipulatePlayerSlots);

        this.updateTitle(this.selX, this.selY, List.of());

        this.container = getSimpleContainer();

        this.baseSlot = new CarpentryInputSlot(this.container, 0, 0, 0, this::updateEx);
        this.slot =     new CarpentryInputSlot(this.container, 1, 0, 0, this::updateEx);
        this.trimSlot = new CarpentryInputSlot(this.container, 2, 0, 0, this::updateEx);
        this.resultSlot = new CarpentryResultSlot(this.container, 3, 0, 0, this::consumeCallback);

        BiFunction<Integer, Integer, Integer> f = (x, y) -> x + y*9;

        this.setSlot(f.apply(0,1), this.baseSlot);
        this.setSlot(f.apply(1,1), this.slot);
        this.setSlot(f.apply(2,1), this.trimSlot);
        this.setSlot(f.apply(1,4), this.resultSlot);

        this.setSlot(f.apply(8,0), ItemStack.EMPTY, () -> {
            var newIndex = Math.max(0, this.scrollIndex-1);
            if (this.scrollIndex != newIndex) {
                this.scrollIndex = newIndex;
                this.selY++;
                updateItemList(availableRecipes());
            }
        });
        this.setSlot(f.apply(8,5), ItemStack.EMPTY, () -> {
            var items = this.availableRecipes();
            if (items.size()-(this.scrollIndex*WIDTH) > WIDTH*HEIGHT) {
                this.scrollIndex++;
                this.selY--;
                updateItemList(items);
            }
        });
    }

    private List<CarpentryRecipe> availableRecipes() {
        List<CarpentryRecipe> list = new ObjectArrayList<>();

        CraftingInput input = CraftingInput.of(3, 1, List.of(this.container.getItem(0), this.container.getItem(1), this.container.getItem(2)));
        List<RecipeHolder<CarpentryRecipe>> recipes = this.player.level().getServer().getRecipeManager().getRecipes().stream().filter(x -> x.value() instanceof CarpentryRecipe carpentryRecipe && carpentryRecipe.matches(input, player.level())).map(x -> (RecipeHolder<CarpentryRecipe>)x).toList();

        // TODO: check for known recipes

        for (RecipeHolder<CarpentryRecipe> recipe : recipes) {
            ItemStack res = recipe.value().getResult().create();
            if (res != null && !res.isEmpty()) {
                list.add(recipe.value());
            }
        }
        return list;
    }

    private void updateItemList(List<CarpentryRecipe> recipes) {
        if (this.selectedRecipe != null && !recipes.contains(this.selectedRecipe)) {
            this.resultSlot.set(ItemStack.EMPTY);
            this.selX = 0;
            this.selY = -1;
            this.scrollIndex = 0;
        }

        // reset
        this.updateTitle(this.selX, this.selY, recipes);

        final int start = 4;
        final int offset = this.scrollIndex*WIDTH;
        for (int column = 0; column < HEIGHT; column++) {
            for (int row = 0; row < WIDTH; row++) {
                int index = row+column*WIDTH + offset;
                int index2 = row+column*9;
                if (index < recipes.size()) {
                    CarpentryRecipe recipe = recipes.get(index);
                    ItemStack item = recipe.getResult().create();
                    final int finalColumn = column;
                    final int finalRow = row;
                    this.setSlot(start+index2, item.copy(), () -> {
                        this.selX = finalRow;
                        this.selY = finalColumn;

                        this.updateTitle(finalRow, finalColumn, recipes);

                        this.selectedRecipe = recipe;
                        this.resultSlot.set(item.copy());
                    });
                } else {
                    this.setSlot(start+index2, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void onRemoved() {
        this.player.getInventory().placeItemBackInInventory(this.player.containerMenu.getCarried(), Prediction.SERVER_ONLY);
        this.player.containerMenu.setCarried(ItemStack.EMPTY);

        for (int i = 0; i < this.container.getContainerSize()-1; i++) {
            if (!this.container.getItem(i).isEmpty())
                this.player.getInventory().placeItemBackInInventory(this.container.removeItemNoUpdate(i), Prediction.SERVER_ONLY);
        }
        this.container.removeAllItems();

        if (this.closeCB != null) {
            this.closeCB.run();
            this.closeCB = null;
        }
    }

    public boolean open(Runnable runnable) {
        closeCB = runnable;
        return this.open();
    }

    @NotNull
    private SimpleContainer getSimpleContainer() {
        return new SimpleContainer(4);
    }

    @Override
    public ItemStack quickMove(int index) {
        var res = super.quickMove(index);
        if (index == this.resultSlot.index) {
            this.consumeCallback(res);
        }
        return res;
    }

    public void consumeCallback(ItemStack itemStack) {
        this.player.awardStat(Stats.ITEM_CRAFTED.get(itemStack.getItem()), itemStack.getCount());

        this.baseSlot.getItem().shrink(1);
        this.slot.getItem().shrink(1);
        this.trimSlot.getItem().shrink(1);

        var l = this.availableRecipes();
        this.updateResultSlot(l);
        this.updateItemList(l);
    }

    private void updateEx() {
        var l = this.availableRecipes();
        this.updateResultSlot(l);
        this.updateItemList(l);

        this.updateTitle(this.selX, this.selY, l);
    }

    private void updateResultSlot(List<CarpentryRecipe> available) {
        if (this.selectedRecipe == null)
            return;

        if (available.contains(this.selectedRecipe)) {
            this.resultSlot.set(this.selectedRecipe.getResult().create());
        }
        updateItemList(available);
    }
}
