package almagest.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TrackingEmitter;

@Mixin(value = ParticleEngine.class, priority = 999999)
public abstract class ParticleEngineMixin
{
    @Shadow @Mutable @Final private static final int MAX_PARTICLES_PER_LAYER = 100000000;

    /*@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/EvictingQueue;create(I)Lcom/google/common/collect/EvictingQueue;", ordinal = 0))
    private <E> EvictingQueue<E> redirectEvictingQueueCreate(int maxSize)
    {
        return EvictingQueue.create(1048576);
    }*/

    @Overwrite(remap = true)
    public void tick()
    {
        ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
        ParticleEngineAccessor accessor = (ParticleEngineAccessor) particleEngine;

        accessor.getParticles().forEach((p_288249_, p_288250_) -> {
            accessor.getLevel().getProfiler().push(p_288249_.toString());
            accessor.invokeTickParticleList(p_288250_);
            accessor.getLevel().getProfiler().pop();
        });
        if (!accessor.getTrackingEmitters().isEmpty())
        {
            List<TrackingEmitter> list = Lists.newArrayList();

            for (TrackingEmitter trackingemitter : accessor.getTrackingEmitters())
            {
                trackingemitter.tick();
                if (!trackingemitter.isAlive())
                {
                    list.add(trackingemitter);
                }
            }

            accessor.getTrackingEmitters().removeAll(list);
        }

        Particle particle;
        if (!accessor.getParticlesToAdd().isEmpty())
        {
            while ((particle = accessor.getParticlesToAdd().poll()) != null)
            {
                accessor.getParticles().computeIfAbsent(particle.getRenderType(), (p_107347_) -> {
                return EvictingQueue.create(100000000);
                }).add(particle);
            }
        }
    }
}
