package de.tomalbrc.decorations.carpentry;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CarpentryBehaviour implements DecorationBehaviour<CarpentryBehaviour.CarpentryBenchConfig> {
    private final CarpentryBenchConfig config;

    private final Long2ObjectArrayMap<List<CarpentryGui>> map = new Long2ObjectArrayMap<>();

    public CarpentryBehaviour(CarpentryBenchConfig config) {
        this.config = config;
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        CarpentryGui gui = new CarpentryGui(MenuType.GENERIC_9x6, player, false);
        final var key = decorationBlockEntity.getBlockPos().asLong();
        gui.open(()->{
            map.get(key).remove(gui);
            if (map.get(key).isEmpty())
                map.remove(key);
        });

        if (map.containsKey(key)) {
            map.get(key).add(gui);
        } else {
            map.put(key, ObjectArrayList.of(gui));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void destroy(DecorationBlockEntity decorationBlockEntity, boolean dropItem) {
        var key = decorationBlockEntity.getBlockPos().asLong();
        if (map.containsKey(key)) {
            for (CarpentryGui carpentryGui : map.get(key)) {
                 carpentryGui.close();
            }
        }
    }

    @Override
    @NotNull
    public CarpentryBenchConfig getConfig() {
        return this.config;
    }

    public static class CarpentryBenchConfig {
    }
}
