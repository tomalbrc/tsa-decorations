package de.tomalbrc.decorations;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class CarpentryInputSlot extends Slot {
    Runnable runnable;
    public CarpentryInputSlot(Container container, int i, int j, int k, Runnable consumeCallback) {
        super(container, i, j, k);
        this.runnable = consumeCallback;
    }

    @Override
    public void setChanged() {
        this.runnable.run();
        super.setChanged();
    }
}
