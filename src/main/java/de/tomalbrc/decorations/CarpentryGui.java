package de.tomalbrc.decorations;

import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
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

    private static final int WIDTH = 4;
    private static final int HEIGHT = 6;

    private String buildGuiTitle(int column, int row, float percentage) {
        column = (WIDTH-1) - column;

        String colA = "(".repeat(column);
        String colB = ")".repeat(column);
        String rowStr = row < 6 && row >= 0 ? Character.toString(0xF700 | row) : "___>";
        String scrollStr = percentage == -1 ? "\uF000" : Character.toString(0xF800 | (int)((0x1C)*percentage));

        // cursed.
        return String.format("<color:#ffffff><font:tsadecorations:ui><U<xx----%s%s%s____--%s<<xxxxxxxxx</font></color><lang:tsa.carpentry.menu.title>", colA, rowStr, colB, scrollStr);
    }

    private void updateTitle(int column, int row, List<CarpentryRecipe> recipes) {
        int rows = (int) Math.ceil(recipes.size()/WIDTH);
        int hidden = rows - (HEIGHT-1);
        float scrollProgress = Math.min(1.0f, Math.max(0.0f, this.scrollIndex / (float) hidden));
        String str = this.buildGuiTitle(column, row, hidden <= 0 ? -1 : scrollProgress);
        this.setTitle(TextParserUtils.formatText(str));
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

        this.setSlotRedirect(f.apply(0,1), this.baseSlot);
        this.setSlotRedirect(f.apply(1,1), this.slot);
        this.setSlotRedirect(f.apply(2,1), this.trimSlot);
        this.setSlotRedirect(f.apply(1,4), this.resultSlot);

        this.setSlot(f.apply(8,0), ItemStack.EMPTY, (x,y,clickType) -> {
            var newIndex = Math.max(0, this.scrollIndex-1);
            if (this.scrollIndex != newIndex) {
                this.scrollIndex = newIndex;
                this.selY++;
                updateItemList(availableRecipes());
            }
        });
        this.setSlot(f.apply(8,5), ItemStack.EMPTY, (x,y,clickType) -> {
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
        List<RecipeHolder<CarpentryRecipe>> recipes = this.player.level().getRecipeManager().getRecipesFor(CarpentryRecipe.Type.INSTANCE, input, this.player.level());

        // TODO: check for known recipes

        for (RecipeHolder<CarpentryRecipe> recipe : recipes) {
            ItemStack res = recipe.value().getResult();
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
                    ItemStack item = recipe.getResult();
                    final int finalColumn = column;
                    final int finalRow = row;
                    this.setSlot(start+index2, item.copy(), (x, y, clickType) -> {
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
    public void onClose() {
        this.player.getInventory().placeItemBackInInventory(this.player.containerMenu.getCarried());
        this.player.containerMenu.setCarried(ItemStack.EMPTY);

        for (int i = 0; i < this.container.getContainerSize()-1; i++) {
            if (!this.container.getItem(i).isEmpty())
                this.player.getInventory().placeItemBackInInventory(this.container.removeItemNoUpdate(i));
        }
        this.container.removeAllItems();

        if (this.closeCB != null) {
            this.closeCB.run();
        }
    }

    private Runnable closeCB = null;
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

        this.updateTitle(this.selX, this.selY, l);
    }

    private void updateResultSlot(List<CarpentryRecipe> available) {
        if (this.selectedRecipe == null)
            return;

        if (available.contains(this.selectedRecipe)) {
            this.resultSlot.set(this.selectedRecipe.getResult().copy());
        }
        updateItemList(available);
    }
}
