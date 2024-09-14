package de.tomalbrc.decorations;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class CarpentryRecipe implements PolymerObject, Recipe<CraftingInput> {
    public static final MapCodec<CarpentryRecipe> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("baseIngredient").forGetter(CarpentryRecipe::getBaseIngredient),
                            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CarpentryRecipe::getIngredient),
                            Ingredient.CODEC.optionalFieldOf("trimIngredient").forGetter(CarpentryRecipe::getTrimIngredient),
                            ItemStack.CODEC.fieldOf("result").forGetter(CarpentryRecipe::getResult))
                    .apply(builder, CarpentryRecipe::new));

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
        if (recipeInput.size() < 2 || (recipeInput.size() < 3 && this.trimIngredient.isPresent()))
            return false;

        Ingredient list[] = {this.baseIngredient, this.ingredient, this.trimIngredient.orElse(Ingredient.EMPTY)};
        boolean matches = true;
        for (int i = 0; i < recipeInput.size() && matches; i++) {
            var item = recipeInput.getItem(i);
            matches = list[i].test(item);
        }

        return matches;
    }

    @Override
    public ItemStack assemble(CraftingInput recipeInput, HolderLookup.Provider provider) {
        return this.getResultItem(provider).copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i >= 3 && j >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CarpentryRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
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
        return result;
    }

    public static class CarpentryRecipeSerializer implements RecipeSerializer<CarpentryRecipe>, PolymerObject {
        public static final CarpentryRecipeSerializer INSTANCE = new CarpentryRecipeSerializer();

        @Override
        public MapCodec<CarpentryRecipe> codec() {
            return CarpentryRecipe.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CarpentryRecipe> streamCodec() {
            return null;
        }
    }

    public static class Type implements RecipeType<CarpentryRecipe>, PolymerObject {
        private Type() {}
        public static final Type INSTANCE = new Type();
    }
}
