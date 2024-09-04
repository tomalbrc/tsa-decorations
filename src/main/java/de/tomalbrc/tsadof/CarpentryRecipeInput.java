package de.tomalbrc.tsadof;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record CarpentryRecipeInput(ItemStack base, ItemStack glue, ItemStack addition) implements RecipeInput {
        public ItemStack getItem(int i) {
            ItemStack itemStack = switch (i) {
                case 0 -> this.base;
                case 1 -> this.glue;
                case 2 -> this.addition;
                default -> throw new IllegalArgumentException("Invalid carpentry_recipe slot " + i);
            };
            return itemStack;
        }

        public int size() {
            return 3;
        }
}
