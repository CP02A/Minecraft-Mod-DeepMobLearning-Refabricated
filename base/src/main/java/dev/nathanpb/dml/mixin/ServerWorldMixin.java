package dev.nathanpb.dml.mixin;

import dev.nathanpb.dml.event.VanillaEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {

    // A listener that returns FAIL cancels the explosion (it may explode again without breaking blocks)
    @Inject(at = @At("HEAD"), method = "explode", cancellable = true)
    private void dml$onExplode(
            Entity entity,
            DamageSource damageSource,
            ExplosionDamageCalculator behavior,
            double x,
            double y,
            double z,
            float power,
            boolean createFire,
            Level.ExplosionInteraction explosionSource,
            ParticleOptions smallParticles,
            ParticleOptions largeParticles,
            WeightedList<ExplosionParticleInfo> blockParticles,
            Holder<SoundEvent> sound,
            CallbackInfo ci
    ) {
        if (power > 0F) {
            Level world = (Level) (Object) this;
            BlockPos pos = BlockPos.containing(x, y, z);
            InteractionResult result = VanillaEvents.INSTANCE.getWorldExplosionEvent()
                .invoker()
                .invoke(world, entity, damageSource, behavior, pos, power, createFire, explosionSource);

            if (result == InteractionResult.FAIL) {
                ci.cancel();
            }
        }
    }
}
