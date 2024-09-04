package de.tomalbrc.tsadof;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CarpentryResultSlot extends Slot {
    Runnable runnable;
    public CarpentryResultSlot(Container container, int i, int j, int k, Runnable consumeCallback) {
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
            this.runnable.run();
        }
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }
}
