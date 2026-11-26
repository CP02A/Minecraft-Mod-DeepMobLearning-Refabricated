package dev.nathanpb.dml.modular_armor.mixin;

import dev.nathanpb.dml.modular_armor.effects.EndermenProofVisionEffect;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enderman.class)
public class EndermanEntityMixin {

    @Inject(at = @At("RETURN"), method = "isBeingStaredBy", cancellable = true)
    private void dml$endermenProofVision(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            InteractionResult result = EndermenProofVisionEffect.Companion.trigger(player);

            if (result == InteractionResult.FAIL) {
                cir.setReturnValue(false);
            }
        }
    }
}
