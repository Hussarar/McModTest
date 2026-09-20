package mctest;

import mctest.blocks.CustomBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = McModTest.MODID, version = McModTest.VERSION, name = McModTest.NAME, dependencies = "required-after:fermiumbooter")
public class McModTest {
    public static final String MODID = "mctest";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "McModTest";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final Block Custom_b = new CustomBlock(Material.ROCK);

	@Instance(MODID)
	public static McModTest instance;

	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event){

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @SubscribeEvent
    public void onBlockRegister(RegistryEvent.Register<Block> event) {

        event.getRegistry().register(Custom_b);
    }
}