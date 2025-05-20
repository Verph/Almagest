package almagest.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.gson.JsonElement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import almagest.Almagest;
import almagest.client.RenderEventHandler;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.DataHelpers;

import static almagest.client.data.PlanetDataManager.*;

public class NebulaDataManager
{
    public static NebulaDataManager NEBULA_DATA;
    public final List<NebulaData> nebulas;

    public static final ConcurrentMap<Integer, Vec3> POSITION_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Vec3> POSITION_ADJ_CACHE = new ConcurrentHashMap<>();

    public NebulaDataManager()
    {
        this.nebulas = initData("stellar_data", "nebulae");
    }

    public List<NebulaData> initData(String directory, String name)
    {
        List<NebulaData> data = new ArrayList<>();
        Map.Entry<ResourceLocation, JsonElement> entry = DataHelpers.scanDirectoryForFile(directory, name);
        DataResult<NebulaDataList> dataResult = NebulaDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
        dataResult.get().ifLeft(nebulaDataList -> {
            data.addAll(nebulaDataList.nebulas());
        }).ifRight((partialResult) -> {
            Almagest.LOGGER.warn("Failed to read nebula data {}", partialResult.message());
        });
        return data;
    }

    /*@SuppressWarnings("null")
    @Override
    public void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
    {
        nebulas.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet())
        {
            DataResult<NebulaDataList> dataResult = NebulaDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            dataResult.get().ifLeft(planetDataList -> {
                if (planetDataList.replace()) {
                    nebulas.clear();
                }
                nebulas.addAll(planetDataList.nebulas());
            }).ifRight((partialResult) -> {
                Almagest.LOGGER.warn("Failed to read nebula data {}", partialResult.message());
            });
        }
    }*/

    public static void clearCaches()
    {
        POSITION_CACHE.clear();
        POSITION_ADJ_CACHE.clear();
    }

    public List<NebulaData> get()
    { 
        return nebulas;
    }

    public Optional<NebulaData> get(String name)
    { 
        return nebulas.stream()
                .filter(nebula -> !name.equals("Unknown") && !nebula.getAlphaNameOrDefault().equals("Unknown") && (nebula.alphanumericName().equals(name) || nebula.name().equals(name)))
                .findFirst();
    }

    /**
     * List of nebulas data.
     *
     * @param replace Indicates whether to replace the existing list.
     * @param nebulas List of nebulas.
     */
    public record NebulaDataList(boolean replace, List<NebulaData> nebulas)
    {
        public static final Codec<NebulaDataList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(NebulaDataList::replace),
                NebulaData.CODEC.listOf().fieldOf("nebulas").forGetter(NebulaDataList::nebulas)
        ).apply(instance, NebulaDataList::new));
    }

    public record NebulaData(int id, String alphanumericName, String name, String constellation, double apparentMagnitude, double rightAscension, double declination, List<Double> pos, String texture, boolean customModel)
    {
        public static final Codec<NebulaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("ID", 0).forGetter(NebulaData::id),
            Codec.STRING.optionalFieldOf("AlphanumericName", "Unknown").forGetter(NebulaData::alphanumericName),
            Codec.STRING.optionalFieldOf("Name", "Unknown").forGetter(NebulaData::name),
            Codec.STRING.optionalFieldOf("Constellation", "Unknown").forGetter(NebulaData::constellation),
            Codec.DOUBLE.optionalFieldOf("ApparentMagnitude", 5.0).forGetter(NebulaData::apparentMagnitude),
            Codec.DOUBLE.optionalFieldOf("RightAscension", 0.0).forGetter(NebulaData::rightAscension),
            Codec.DOUBLE.optionalFieldOf("Declination", 0.0).forGetter(NebulaData::declination),
            Codec.DOUBLE.listOf().optionalFieldOf("Pos", List.of(0.0, 0.0, 0.0)).forGetter(NebulaData::pos),
            Codec.STRING.optionalFieldOf("Texture", "Unknown").forGetter(NebulaData::texture),
            Codec.BOOL.optionalFieldOf("CustomModel", false).forGetter(NebulaData::customModel)
        ).apply(instance, NebulaData::new));

        public int getID()
        {
            return this.id();
        }

        public String getAlphaNameOrDefault()
        {
            return this.alphanumericName.equals("Unknown") ? this.name : this.alphanumericName;
        }

        /*public Vec3 getCachedPosa()
        {
            final int id = this.id();
            if (POSITION_CACHE.containsKey(id))
            {
                return POSITION_CACHE.get(id);
            }
            Vec3 pos = getAdjustedPosa();
            POSITION_CACHE.put(id, pos);
            return pos;
        }

        public Vec3 getPosa()
        {
            double x = this.pos().get(0);
            double y = this.pos().get(1);
            double z = this.pos().get(2);
            double time = Minecraft.getInstance().level.getTimeOfDay(Minecraft.getInstance().getPartialTick()) * 360.0D;
            return AHelpers.xRot(AHelpers.yRot(new Vec3(x, y, z), Math.toRadians(27.0D)), Math.toRadians(time + 70.0D));
        }

        public Vec3 getAdjustedPosa()
        {
            Vec3 posAdd = getPos().normalize().scale(Config.COMMON.nebulaDistanceAdd.get());
            Vec3 pos = (getPos().add(posAdd)).scale(Config.COMMON.nebulaDistanceMult.get());
            POSITION_CACHE.put(this.id(), pos);
            return pos;
        }*/

        public Vec3 getPos()
        {
            double x = this.pos().get(0);
            double y = this.pos().get(1);
            double z = this.pos().get(2);
            Vec3 pos = new Vec3(x, y, z);
            Vec3 posAdd = pos.normalize().scale(Config.COMMON.nebulaDistanceAdd.get());
            return pos.add(posAdd).scale(Config.COMMON.nebulaDistanceMult.get());
        }

        public Vec3 getCachedPos()
        {
            final int id = this.id();
            if (POSITION_CACHE.containsKey(id))
            {
                return POSITION_CACHE.get(id);
            }
            //Vec3 pos = AHelpers.yRot(getPos(), Math.toRadians(27.0D));
            Vec3 pos = getPos();
            POSITION_CACHE.put(id, pos);
            return pos;
        }

        public Vec3 getAdjustedPos(boolean updateCache)
        {
            double i = OBSERVER_INCLINATION;
            double s = OBSERVER_SEASON;
            double l = RenderEventHandler.latitude;
            double d = OBSERVER_TIME_OF_DAY + 1.22173047639603065D;

            Vec3 pos = getCachedPos();
            pos = StarDataManager.getRotation(pos);
            pos = StarDataManager.getRotation2(pos);
            pos = StarDataManager.getRotation3(pos);
            pos = rotZ(d, pos);
            pos = rotX(Math.toRadians(270.0D), pos);
            pos = rotZ(i, pos);
            pos = rotX(-l + s, pos);
            if (updateCache)
            {
                POSITION_ADJ_CACHE.put(this.id(), pos);
            }
            return pos;
        }

        public Vec3 getCachedAdjPos(boolean updateCache)
        {
            final int id = this.id();
            if (POSITION_ADJ_CACHE.containsKey(id))
            {
                return POSITION_ADJ_CACHE.get(id);
            }
            return getAdjustedPos(updateCache);
        }

        public ResourceLocation getTexture()
        {
            return AHelpers.identifier("textures/nebulas/" + this.getAlphaNameOrDefault().toLowerCase(Locale.ROOT).replace(" ", "_") + ".png");
        }
    }
}
