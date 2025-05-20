package almagest.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.phys.AABB;
import net.minecraft.client.particle.Particle;

@Mixin(Particle.class)
public interface ParticleAccessor
{
    @Accessor("bb")
    void setBB(AABB newBb);
}

