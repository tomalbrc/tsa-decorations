package de.tomalbrc.tsadof;

import com.mojang.logging.LogUtils;
import de.tomalbrc.filament.api.FilamentLoader;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import de.tomalbrc.filament.api.registry.BlockTypeRegistry;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class TSADOF implements ModInitializer {
    public static Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        try {
            BehaviourRegistry.init();
            BlockTypeRegistry.init();

            BehaviourRegistry.registerBehaviour(ResourceLocation.fromNamespaceAndPath("filament", "carpentry"), CarpentryBehaviour.class);

            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ResourceLocation.fromNamespaceAndPath("tsa", "carpentry_recipe"), CarpentryRecipe.CarpentryRecipeSerializer.INSTANCE);
            Registry.register(BuiltInRegistries.RECIPE_TYPE, ResourceLocation.fromNamespaceAndPath("tsa", "carpentry_recipe"), CarpentryRecipe.Type.INSTANCE);

            FilamentLoader.loadModels("tsa-dof", "tsa");
            FilamentLoader.loadDecorations("tsa-dof");
            PolymerResourcePackUtils.addModAssets("tsa-dof");
            PolymerResourcePackUtils.markAsRequired();
        } catch (Exception e) {
            LOGGER.error("Could not load some files!");
            e.printStackTrace();
        }
    }
}
