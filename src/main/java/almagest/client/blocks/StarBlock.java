package almagest.client.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@SuppressWarnings("null")
public class StarBlock extends Block
{
    public static final BooleanProperty FANCY = BooleanProperty.create("fancy");
    public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 4);

    public StarBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FANCY, false).setValue(STATE, 0));
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FANCY, STATE);
    }
}
