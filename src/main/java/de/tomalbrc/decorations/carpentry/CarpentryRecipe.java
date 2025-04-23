package de.tomalbrc.decorations.carpentry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class CarpentryRecipe implements PolymerObject, Recipe<CraftingInput> {
    public static final MapCodec<CarpentryRecipe> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Ingredient.CODEC.fieldOf("baseIngredient").forGetter(CarpentryRecipe::getBaseIngredient),
                            Ingredient.CODEC.fieldOf("ingredient").forGetter(CarpentryRecipe::getIngredient),
                            Ingredient.CODEC.optionalFieldOf("trimIngredient").forGetter(CarpentryRecipe::getTrimIngredient),
                            ItemStack.CODEC.fieldOf("result").forGetter(CarpentryRecipe::getResult))
                    .apply(builder, CarpentryRecipe::new));


    private static final RecipeBookCategory category = new RecipeBookCategory();
    private final Ingredient baseIngredient;
    private final Ingredient ingredient;
    private final Optional<Ingredient> trimIngredient;
    private final ItemStack result;

    public CarpentryRecipe(Ingredient baseIngredient, Ingredient ingredient, Optional<Ingredient> trimIngredient, ItemStack result) {
        this.baseIngredient = baseIngredient;
        this.ingredient = ingredient;
        this.trimIngredient = trimIngredient;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingInput recipeInput, Level level) {
        List<Ingredient> expected = new ObjectArrayList<>();
        expected.add(this.ingredient);
        expected.add(this.baseIngredient);
        this.trimIngredient.ifPresent(expected::add);

        List<ItemStack> inputs = new ObjectArrayList<>();
        for (int i = 0; i < recipeInput.size(); i++) {
            ItemStack stack = recipeInput.getItem(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        if (inputs.size() != expected.size()) {
            return false;
        }

        for (Ingredient ingredient : expected) {
            boolean matched = false;
            Iterator<ItemStack> iter = inputs.iterator();
            while (iter.hasNext()) {
                ItemStack stack = iter.next();
                if (ingredient.test(stack)) {
                    iter.remove(); // Prevent reuse
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }

        return true;
    }

    @Override
    @NotNull
    public ItemStack assemble(CraftingInput recipeInput, HolderLookup.Provider provider) {
        return this.getResult().copy();
    }

    @Override
    @NotNull
    public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
        return CarpentryRecipeSerializer.INSTANCE;
    }

    @Override
    @NotNull
    public RecipeType<? extends Recipe<CraftingInput>> getType() {
        return Type.INSTANCE;
    }

    @Override
    @NotNull
    public PlacementInfo placementInfo() {
        var list = ObjectArrayList.of(ingredient, baseIngredient);
        this.trimIngredient.ifPresent(list::add);
        return PlacementInfo.create(list);
    }

    @Override
    @NotNull
    public RecipeBookCategory recipeBookCategory() {
        return category;
    }

    public Ingredient getBaseIngredient() {
        return baseIngredient;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public Optional<Ingredient> getTrimIngredient() {
        return trimIngredient;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public static class CarpentryRecipeSerializer implements RecipeSerializer<CarpentryRecipe>, PolymerObject {
        public static final CarpentryRecipeSerializer INSTANCE = new CarpentryRecipeSerializer();

        @Override
        @NotNull
        public MapCodec<CarpentryRecipe> codec() {
            return CarpentryRecipe.CODEC;
        }

        @Override
        @NotNull
        public StreamCodec<RegistryFriendlyByteBuf, CarpentryRecipe> streamCodec() {
            return null;
        }
    }

    public static class Type implements RecipeType<CarpentryRecipe>, PolymerObject {
        private Type() {}
        public static final Type INSTANCE = new Type();
    }
}
