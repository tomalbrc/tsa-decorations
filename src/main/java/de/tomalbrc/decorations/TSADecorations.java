package de.tomalbrc.decorations;

import com.mojang.logging.LogUtils;
import de.tomalbrc.decorations.carpentry.CarpentryBehaviour;
import de.tomalbrc.decorations.carpentry.CarpentryRecipe;
import de.tomalbrc.decorations.polydex.PolydexCompat;
import de.tomalbrc.filament.api.FilamentLoader;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class TSADecorations implements ModInitializer {
    public static Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "tsa-decorations";
    public static final String COMMON_ID = "tsa";

    @Override
    public void onInitialize() {
        try {
            BehaviourRegistry.registerBehaviour(ResourceLocation.fromNamespaceAndPath(COMMON_ID, "carpentry"), CarpentryBehaviour.class);

            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ResourceLocation.fromNamespaceAndPath(COMMON_ID, "carpentry_recipe"), CarpentryRecipe.CarpentryRecipeSerializer.INSTANCE);
            Registry.register(BuiltInRegistries.RECIPE_TYPE, ResourceLocation.fromNamespaceAndPath(COMMON_ID, "carpentry_recipe"), CarpentryRecipe.Type.INSTANCE);

            FilamentLoader.loadModels(MOD_ID, COMMON_ID);
            FilamentLoader.loadItems(MOD_ID);
            FilamentLoader.loadDecorations(MOD_ID);

            PolymerResourcePackUtils.addModAssets(MOD_ID);
            PolymerResourcePackUtils.markAsRequired();

            if (FabricLoader.getInstance().isModLoaded("polydex")) {
                ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
                    PolydexCompat.init(server);
                });
            }

        } catch (Exception e) {
            LOGGER.error("Could not load some files!");
            e.printStackTrace();
        }
    }
}
