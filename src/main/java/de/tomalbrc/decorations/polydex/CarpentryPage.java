package de.tomalbrc.decorations.polydex;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.registry.ItemGroupRegistry;
import eu.pb4.polydex.api.v1.recipe.*;
import eu.pb4.polydex.impl.book.InternalPageTextures;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class CarpentryPage implements PolydexPage {
    public static final ItemStack TYPE_ICON = new GuiElementBuilder(BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("tsa", "carpentry_table")).orElseThrow().value()).setName(Component.translatable("polydex.tsa.carpentry.recipes").withStyle(ChatFormatting.GOLD)).asStack();

    private List<PolydexIngredient<?>> ingredients;

    private final DecorationData decorationData;

    public CarpentryPage(DecorationData decorationData) {
        this.decorationData = decorationData;
        this.ingredients = PolydexCompat.INFO_MAP.get(decorationData.id()).ingredients();
    }

    public @Nullable Component texture(ServerPlayer player) {
        return InternalPageTextures.SMITHING;
    }

    @Override
    public Identifier identifier() {
        return this.decorationData.id();
    }

    @Override
    public ItemStack typeIcon(ServerPlayer serverPlayerEntity) {
        return BuiltInRegistries.ITEM.get(decorationData.id()).orElseThrow().value().getDefaultInstance();
    }

    @Override
    public ItemStack entryIcon(@Nullable PolydexEntry polydexEntry, ServerPlayer serverPlayerEntity) {
        return ItemGroupRegistry.TAB_GROUPS.get(decorationData.group()).getIconItem();
    }

    @Override
    public void createPage(@Nullable PolydexEntry polydexEntry, ServerPlayer serverPlayerEntity, PageBuilder layer) {
        if (polydexEntry == null)
            return;

        int i = 0;

        this.ingredients = PolydexCompat.INFO_MAP.get(polydexEntry.identifier()).ingredients();

        layer.set(4,0, TYPE_ICON);

        for (var ingredient : this.ingredients) {
            layer.setIngredient(i + 2, 2, ingredient);
            i++;

            if (i == 3) {
                break;
            }
        }

        layer.setOutput(6, 2, BuiltInRegistries.ITEM.get(polydexEntry.identifier()).orElseThrow().value().getDefaultInstance());
    }

    @Override
    public List<PolydexIngredient<?>> ingredients() {
        return this.ingredients;
    }

    @Override
    public List<PolydexCategory> categories() {
        return List.of(PolydexCompat.CATEGORY); // just 1 cat. for all for now...
    }

    @Override
    public boolean isOwner(MinecraftServer minecraftServer, PolydexEntry polydexEntry) {
        ItemStack itemStack = (ItemStack) polydexEntry.stack().getBacking();
        if (itemStack.getItem() instanceof DecorationItem decorationItem) {
            return Objects.equals(decorationItem.getDecorationData().group(), decorationData.group());
        }
        return false;
    }
}