package almagest.client.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import almagest.client.data.CelestialDataManager;

@SuppressWarnings("null")
public class CelestialBodyBlock extends Block
{
    public static final int MIN = 0;
    public static final int MAX = 15;
    public static StringListProperty CELESTIAL_BODY = StringListProperty.create("body", CelestialDataManager.getBlockStateData());
    public static IntegerProperty VARIANT = IntegerProperty.create("variant", MIN, MAX);
    public static BooleanProperty ALTERNATIVE = BooleanProperty.create("alternative");

    public CelestialBodyBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().trySetValue(CELESTIAL_BODY, "earth").trySetValue(VARIANT, 0).trySetValue(ALTERNATIVE, false));
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(CELESTIAL_BODY, VARIANT, ALTERNATIVE);
    }
}
