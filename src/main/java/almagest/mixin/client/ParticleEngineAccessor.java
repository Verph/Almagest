package almagest.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor
{
    @Accessor("level")
    ClientLevel getLevel();

    @Accessor("particles")
    Map<ParticleRenderType, Queue<Particle>> getParticles();

    @Accessor("trackingEmitters")
    Queue<TrackingEmitter> getTrackingEmitters();

    @Accessor("particlesToAdd")
    Queue<Particle> getParticlesToAdd();

    @Invoker("tickParticleList")
    void invokeTickParticleList(Collection<Particle> pParticles);
}
