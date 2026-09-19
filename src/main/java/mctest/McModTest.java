package mctest;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = McModTest.MODID, version = McModTest.VERSION, name = McModTest.NAME, dependencies = "required-after:fermiumbooter")
public class McModTest {
    public static final String MODID = "mctest";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "McModTest";
    public static final Logger LOGGER = LogManager.getLogger();
	
	@Instance(MODID)
	public static McModTest instance;
	
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}