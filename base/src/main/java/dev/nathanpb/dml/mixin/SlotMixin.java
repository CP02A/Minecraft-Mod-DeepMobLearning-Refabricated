package dev.nathanpb.dml.mixin;

import dev.nathanpb.dml.data.TrialKeyData;
import dev.nathanpb.dml.data.TrialKeyDataKt;
import dev.nathanpb.dml.item.ItemTrialKey;
import dev.nathanpb.dml.trial.affix.UtilsKt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {

    // A trial key taken out of a crafting result gets its random affixes
    @Inject(at = @At("RETURN"), method = "remove")
    private void dml$onTake(int amount, CallbackInfoReturnable<ItemStack> ci) {
        ItemStack stack = ci.getReturnValue();
        if (stack == null || !(stack.getItem() instanceof ItemTrialKey)) {
            return;
        }

        if ((Object) this instanceof ResultSlot dis) {
            Player player = ((ICraftingResultSlotMixin) dis).dml$getPlayer();
            if (!player.level().isClientSide()) {
                TrialKeyData oldData = TrialKeyDataKt.getTrialKeyData(stack);
                if (oldData != null && oldData.getAffixes().isEmpty()) {
                    TrialKeyData newData = new TrialKeyData(stack);
                    newData.setAffixes(UtilsKt.pickRandomAffixes());
                }
            }
        }
    }
}
