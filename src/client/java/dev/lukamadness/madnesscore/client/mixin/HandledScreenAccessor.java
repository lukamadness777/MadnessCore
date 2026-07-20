package dev.lukamadness.madnesscore.client.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {

    @Accessor("x")
    int madnesscore$getGuiLeft();

    @Accessor("y")
    int madnesscore$getGuiTop();

    @Accessor("backgroundWidth")
    int madnesscore$getBackgroundWidth();
}
