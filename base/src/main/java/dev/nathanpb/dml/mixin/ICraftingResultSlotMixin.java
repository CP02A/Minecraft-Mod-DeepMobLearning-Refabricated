package dev.nathanpb.dml.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ResultSlot.class)
public interface ICraftingResultSlotMixin {
    @Accessor("player")
    Player dml$getPlayer();
}
