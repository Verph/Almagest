package almagest.client.renderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;

import almagest.client.RenderHelpers;

public class ARenderTypes
{
    public static final VertexFormat STAR_VERTEX_FORMAT = VertexFormat.builder()
        .add("Position", VertexFormatElement.POSITION)
        .add("UV0", VertexFormatElement.UV0)
        .add("Color", VertexFormatElement.COLOR)
        .add("UV2", VertexFormatElement.UV2)
        .build();

    public static final RenderType STAR_RENDERTYPE = RenderType.create(
        "star",
        STAR_VERTEX_FORMAT,
        VertexFormat.Mode.TRIANGLES,
        65536,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(() -> AShaders.STAR_SHADER))
            .setTextureState(new RenderStateShard.TextureStateShard(
                RenderHelpers.STAR,
                false,
                false
            ))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );
}
