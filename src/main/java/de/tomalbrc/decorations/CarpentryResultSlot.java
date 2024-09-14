package de.tomalbrc.decorations;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class CarpentryResultSlot extends Slot {
    Consumer<ItemStack> runnable;
    public CarpentryResultSlot(Container container, int i, int j, int k, Consumer<ItemStack> consumeCallback) {
        super(container, i, j, k);
        this.runnable = consumeCallback;
    }

    @Override
    public boolean isFake() {
        return true;
    }

    @Override
    public void onTake(Player player, ItemStack itemStack) {
        super.onTake(player, itemStack);

        if (!itemStack.isEmpty()) {
            this.runnable.accept(itemStack);
        }
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }
}
