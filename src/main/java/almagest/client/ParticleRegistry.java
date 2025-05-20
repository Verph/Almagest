package almagest.client;

import java.util.Optional;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;

import almagest.util.AHelpers;

import static almagest.Almagest.MOD_ID;

public class ParticleRegistry extends SpriteSourceProvider
{
	public static final ResourceLocation SQUARE_WHITE_LOCATION = AHelpers.identifier("particles/white");

	public static TextureAtlasSprite SQUARE_WHITE;

	public ParticleRegistry(PackOutput output, ExistingFileHelper fileHelper)
	{
		super(output, fileHelper, MOD_ID);
	}

	@Override
	protected void addSources()
	{
		addSprite(SQUARE_WHITE_LOCATION);
    }

	public void addSprite(ResourceLocation resource)
    {
		atlas(SpriteSourceProvider.PARTICLES_ATLAS).addSource(new SingleFile(resource, Optional.empty()));
	}

	@SuppressWarnings("deprecation")
	public static void registerParticles(TextureStitchEvent.Post event)
    {
		if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_PARTICLES))
        {
			return;
		}
		SQUARE_WHITE = event.getAtlas().getSprite(SQUARE_WHITE_LOCATION);
    }
}
