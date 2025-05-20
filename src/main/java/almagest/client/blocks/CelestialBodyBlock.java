package almagest.client.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import almagest.client.CelestialObjectHandler;

@SuppressWarnings("null")
public class CelestialBodyBlock extends Block
{
    public static final StringListProperty CELESTIAL_BODY = StringListProperty.create("body", CelestialObjectHandler.HAS_CUSTOM_MODELS);
    public static final BooleanProperty ALTERNATIVE = BooleanProperty.create("alternative");

    public CelestialBodyBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(CELESTIAL_BODY, "earth").setValue(ALTERNATIVE, false));
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(CELESTIAL_BODY, ALTERNATIVE);
    }
}
