package dev.nathanpb.dml.mixin;

import dev.nathanpb.dml.event.VanillaEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin {

    // The damage after armor, before enchantments and effects
    @ModifyArg(
        method = "actuallyHurt",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"),
        index = 1
    )
    private float dml$modifyDamage(DamageSource source, float amount) {
        Player dis = (Player) (Object) this;
        return VanillaEvents.INSTANCE.getLivingEntityDamageEvent()
            .invoker()
            .invoke(new VanillaEvents.LivingEntityDamageContext(dis, source, amount))
            .getDamage();
    }


    @Inject(at = @At("HEAD"), method = "tick")
    private void dml$tick(CallbackInfo ci) {
        VanillaEvents.INSTANCE.getPlayerEntityTickEvent()
            .invoker()
            .invoke((Player) (Object) this);
    }

}
