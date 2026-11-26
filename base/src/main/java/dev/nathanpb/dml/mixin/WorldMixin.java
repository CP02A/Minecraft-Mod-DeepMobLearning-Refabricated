package dev.nathanpb.dml.mixin;

import dev.nathanpb.dml.accessor.ITrialWorldPersistenceAccessor;
import dev.nathanpb.dml.trial.Trial;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(Level.class)
public class WorldMixin implements ITrialWorldPersistenceAccessor {
    @Unique
    private final List<Trial> dml$runningTrials = new ArrayList<>();

    @Override
    public List<Trial> dml$getRunningTrials() {
        return dml$runningTrials;
    }
}
