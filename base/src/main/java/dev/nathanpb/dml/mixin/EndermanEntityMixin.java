package dev.nathanpb.dml.mixin;

import dev.nathanpb.dml.event.VanillaEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enderman.class)
public class EndermanEntityMixin {

    @Inject(at = @At("HEAD"), method = "teleport(DDD)Z", cancellable = true)
    private void dml$onTeleport(double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
        Enderman entity = (Enderman) (Object) this;
        Vec3 pos = new Vec3(x, y, z);
        InteractionResult result = VanillaEvents.INSTANCE.getEndermanTeleportEvent()
            .invoker()
            .invoke(entity, pos);

        if (result == InteractionResult.FAIL) {
            ci.setReturnValue(false);
        }
    }

}
