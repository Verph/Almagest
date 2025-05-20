package almagest.client.blocks;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import almagest.Almagest;
import almagest.client.items.AItems;
import almagest.util.AHelpers;

public class ABlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Almagest.MOD_ID);

    public static final RegistryObject<Block> CELESTIAL_BODY = registerNoItem("celestial_bodies", () -> new CelestialBodyBlock(Properties.of()));
    public static final RegistryObject<Block> DAY_CYCLE = registerNoItem("day_cycle", () -> new DayCycleBlock(Properties.of()));

    public static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> blockSupplier)
    {
        return register(name, blockSupplier, (Function<T, ? extends BlockItem>) null);
    }

    public static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier)
    {
        return register(name, blockSupplier, block -> new BlockItem(block, new Item.Properties()));
    }

    public static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, Item.Properties blockItemProperties)
    {
        return register(name, blockSupplier, block -> new BlockItem(block, blockItemProperties));
    }

    public static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, @Nullable Function<T, ? extends BlockItem> blockItemFactory)
    {
        return AHelpers.registerBlock(ABlocks.BLOCKS, AItems.ITEMS, name, blockSupplier, blockItemFactory);
    }
}
