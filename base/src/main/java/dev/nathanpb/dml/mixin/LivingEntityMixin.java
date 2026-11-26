package dev.nathanpb.dml.mixin;

import dev.nathanpb.dml.accessor.ILivingEntityReiStateAccessor;
import dev.nathanpb.dml.entity.SystemGlitchEntity;
import dev.nathanpb.dml.event.VanillaEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ILivingEntityReiStateAccessor {

    @Unique
    private boolean dml$isInREIScreen = false;

    @Inject(at = @At("HEAD"), method = "getMaxHealth", cancellable = true)
    private void dml$getMaxHealth(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof SystemGlitchEntity dis) {
            if (dis.getTier() != null) {
                cir.setReturnValue(dis.getTier().getSystemGlitchMaxHealth());
            }
        }
    }

    // The damage before armor (Player overrides actuallyHurt, see PlayerEntityMixin)
    @ModifyArg(
        method = "actuallyHurt",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"),
        index = 1
    )
    private float dml$modifyDamage(DamageSource source, float amount) {
        LivingEntity dis = (LivingEntity) (Object) this;
        return VanillaEvents.INSTANCE.getLivingEntityDamageEvent()
                .invoker()
                .invoke(new VanillaEvents.LivingEntityDamageContext(dis, source, amount))
                .getDamage();
    }

    @Override
    public boolean dml$isInReiScreen() {
        return dml$isInREIScreen;
    }

    @Override
    public void dml$setInReiScreen(boolean flag) {
        dml$isInREIScreen = flag;
    }

}
