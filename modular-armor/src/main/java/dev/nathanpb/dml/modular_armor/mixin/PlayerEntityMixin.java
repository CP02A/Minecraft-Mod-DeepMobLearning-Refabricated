package dev.nathanpb.dml.modular_armor.mixin;

import dev.nathanpb.dml.DeepMobLearningKt;
import dev.nathanpb.dml.modular_armor.accessor.IUndyingCooldown;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin implements IUndyingCooldown {

    @Unique
    private static final String dml$UNDYING_KEY = DeepMobLearningKt.MOD_ID + ":undyingLastUsage";

    @Unique
    private Long dml$undyingLastUsage;

    @Override
    public @Nullable Long dml$getUndyingLastUsage() {
        return dml$undyingLastUsage;
    }

    @Override
    public void dml$setUndyingLastUsage(@Nullable Long time) {
        dml$undyingLastUsage = time;
    }

    @Inject(at = @At("RETURN"), method = "readAdditionalSaveData")
    private void dml$readUndying(ValueInput input, CallbackInfo ci) {
        dml$undyingLastUsage = input.getLong(dml$UNDYING_KEY).orElse(null);
    }

    @Inject(at = @At("RETURN"), method = "addAdditionalSaveData")
    private void dml$writeUndying(ValueOutput output, CallbackInfo ci) {
        if (dml$undyingLastUsage != null) {
            output.putLong(dml$UNDYING_KEY, dml$undyingLastUsage);
        }
    }
}
