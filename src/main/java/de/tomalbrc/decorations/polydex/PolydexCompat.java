package de.tomalbrc.decorations.polydex;

import de.tomalbrc.decorations.carpentry.CarpentryRecipe;
import de.tomalbrc.filament.decoration.DecorationItem;
import eu.pb4.polydex.api.v1.recipe.PolydexCategory;
import eu.pb4.polydex.api.v1.recipe.PolydexEntry;
import eu.pb4.polydex.api.v1.recipe.PolydexIngredient;
import eu.pb4.polydex.api.v1.recipe.PolydexPage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PolydexCompat {
    public static final PolydexCategory CATEGORY = PolydexCategory.of(ResourceLocation.fromNamespaceAndPath("tsa", "carpentry"));

    public static void init(MinecraftServer server) {
        List<RecipeHolder<CarpentryRecipe>> recipes = server.getRecipeManager().getAllRecipesFor(CarpentryRecipe.Type.INSTANCE);
        for (RecipeHolder<CarpentryRecipe> recipe : recipes) {
            ItemStack res = recipe.value().getResultItem(server.registryAccess());
            if (res.getItem() instanceof DecorationItem decorationItem) {
                add(decorationItem);

                List<PolydexIngredient<?>> ingredients = new ArrayList<>();
                ingredients.add(PolydexIngredient.of(recipe.value().getBaseIngredient()));
                ingredients.add(PolydexIngredient.of(recipe.value().getIngredient()));
                recipe.value().getTrimIngredient().ifPresent(val -> ingredients.add(PolydexIngredient.of(val)));

                PolydexCompat.INFO_MAP.put(decorationItem.getDecorationData().id(), new PolydexCompat.RecipeInfo(res, ingredients));
            }
        }

        PolydexPage.register(PolydexCompat::createPages);
    }

    public static void add(DecorationItem item) {
        PolydexEntry.registerEntryCreator(item, PolydexCompat::createEntries);
    }

    private static PolydexEntry createEntries(ItemStack stack) {
        if (!(stack.getItem() instanceof DecorationItem decorationItem))
            return PolydexEntry.of(stack);

        return PolydexEntry.of(decorationItem.getDecorationData().id(), stack);
    }

    private static void createPages(MinecraftServer server, Consumer<PolydexPage> pageConsumer) {
        for (var val : INFO_MAP.values()) {
            if (val.itemStack.getItem() instanceof DecorationItem decorationItem) {
                pageConsumer.accept(new CarpentryPage(decorationItem.getDecorationData()));
            }
        }
    }

    public static final Map<ResourceLocation, RecipeInfo> INFO_MAP = new Object2ObjectOpenHashMap<>();

    public record RecipeInfo(ItemStack itemStack, List<PolydexIngredient<?>> ingredients) {}
}
