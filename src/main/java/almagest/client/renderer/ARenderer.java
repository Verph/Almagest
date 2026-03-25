package almagest.client.renderer;

import net.minecraft.client.renderer.ShaderInstance;

import almagest.client.CelestialObjectHandler;
import almagest.client.particle.CelestialObject;
import almagest.config.Config;

public class ARenderer
{
    public static void setShaderUniforms(long gameTime)
    {
        ShaderInstance shader = AShaders.STAR_SHADER;
        if (shader == null) return;

        CelestialObject observer = CelestialObjectHandler.observerObject;
        double d = observer.timeOfDay;
        double lo = -CelestialObjectHandler.longitude;
        double i = observer.inclination;
        double o = observer.obliquity;
        double s = observer.season;
        double la = -CelestialObjectHandler.latitude;

        double rotY = d + lo;
        double rotX = i + o + s + la;

        shader.safeGetUniform("uRotY").set((float) rotY);
        shader.safeGetUniform("uRotX").set((float) rotX);

        shader.safeGetUniform("uStarDistanceMult").set(Config.COMMON.starDistanceMult.get().floatValue());
        shader.safeGetUniform("uStarDistanceAdd").set(Config.COMMON.starDistanceAdd.get().floatValue());
        shader.safeGetUniform("uUniverseScale").set(Config.COMMON.universeScale.get().floatValue());

        shader.safeGetUniform("uScale").set(Config.COMMON.scale.get().floatValue());
        shader.safeGetUniform("uGamma").set(Config.COMMON.gamma.get().floatValue());
        shader.safeGetUniform("uSlope").set(Config.COMMON.slope.get().floatValue());
        shader.safeGetUniform("uExposure").set(Config.COMMON.exposure.get().floatValue());

        shader.safeGetUniform("uTime").set(gameTime + CelestialObjectHandler.partialTicks);
        shader.safeGetUniform("uAtmosphereFactor").set((float) CelestialObjectHandler.atmosphereFactor);
    }
}
