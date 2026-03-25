package almagest.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.Minecraft;

@Mixin(value = Minecraft.class, priority = 100)
public interface MinecraftAccessor
{
    @Accessor("frames")
    int getFrames();
}
