package dev.nathanpb.dml.modular_armor.mixin;

import dev.nathanpb.dml.modular_armor.EntityStatusEffectsKt;
import dev.nathanpb.dml.modular_armor.InitKt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class ClientEntityMixin {

    // Soul Vision: living entities near the player glow (client side only)
    @Inject(at = @At("RETURN"), method = "isCurrentlyGlowing", cancellable = true)
    private void dml$soulVisionGlow(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide() && !cir.getReturnValue()) {
            if (self instanceof LivingEntity dis) {
                Player player = Minecraft.getInstance().player;
                if (player != null && dis != player && player.hasEffect(EntityStatusEffectsKt.getSOUL_VISION_EFFECT())) {
                    MobEffectInstance soulVision = player.getEffect(EntityStatusEffectsKt.getSOUL_VISION_EFFECT());

                    if (soulVision != null && player.distanceTo(dis) <= ((soulVision.getAmplifier() + 1) * InitKt.getModularArmorConfig().getGlitchArmor().getSoulVisionRangeMultiplier())) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
}
