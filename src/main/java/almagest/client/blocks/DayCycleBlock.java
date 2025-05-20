package almagest.client.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@SuppressWarnings("null")
public class DayCycleBlock extends Block
{
    public static final IntegerProperty TIME = IntegerProperty.create("time", 0, 47);
    public static final IntegerProperty OBLIQUITY = IntegerProperty.create("obliquity", 0, 144);

    public DayCycleBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(TIME, 0).setValue(OBLIQUITY, 72));
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TIME, OBLIQUITY);
    }
}
