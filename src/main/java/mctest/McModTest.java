package mctest;

import mctest.CreativeTab.klugtab;
import mctest.WorldGen.KlugWorldGen;
import mctest.blocks.CustomBlock;
import mctest.capability.ISanityCapability;
import mctest.capability.ModCapabilities;
import mctest.capability.SanitySyncPacket;
import mctest.enchantments.EnchantKlug;
import mctest.items.KlugGem;
import mctest.items.KlugSword;
import mctest.structures.KlugShrine;
import mctest.util.ShaderHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSword;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = McModTest.MODID, version = McModTest.VERSION, name = McModTest.NAME, dependencies = "required-after:fermiumbooter")
public class McModTest {
    public static final String MODID = "mctest";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "McModTest";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final Item.ToolMaterial KLUG_MATERIAL = EnumHelper.addToolMaterial("KlugMaterial", 2000, 10,100f,100,20);

    public static final CreativeTabs KLUG_TAB = new klugtab("klugtab");
    public static final Block Custom_b = new CustomBlock(Material.ROCK);
    public static final ItemBlock itemCustom_b = new ItemBlock(Custom_b);
    public static final ItemSword klugsword = new KlugSword();
    public static final Enchantment ENCHANT_KLUG = new EnchantKlug(Enchantment.Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND});
    public static final Item KLUG_GEM = new KlugGem();
    // Das ist unser Netzwerk-Kanal. "mctest" ist der Name, "1" ist die Protokoll-Version
    public static final net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper NETWORK = net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.newSimpleChannel("mctest");

	@Instance(MODID)
	public static McModTest instance;

	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        MinecraftForge.EVENT_BUS.register(this);
        ModCapabilities.register();

        MinecraftForge.EVENT_BUS.register(ModCapabilities.class);
        NETWORK.registerMessage(SanitySyncPacket.Handler.class, SanitySyncPacket.class, 0, net.minecraftforge.fml.relauncher.Side.CLIENT);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        GameRegistry.registerWorldGenerator(new KlugWorldGen(), 0);

        GameRegistry.registerWorldGenerator(new KlugShrine(), 40);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @SubscribeEvent
    public void onBlockRegister(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(Custom_b);
    }

    @SubscribeEvent
    public void onItemRegister(RegistryEvent.Register<Item> event){
        event.getRegistry().register(itemCustom_b.setRegistryName(Custom_b.getRegistryName()));
        event.getRegistry().register(klugsword);
        event.getRegistry().register(KLUG_GEM);
    }

    @SubscribeEvent
    public void onModelRegister(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemCustom_b, 0, new ModelResourceLocation(itemCustom_b.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(klugsword, 0, new ModelResourceLocation(klugsword.getRegistryName(), "inventory"));
    }

    private boolean shouldApplySanityEffect() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) return false;
        if (!mc.player.hasCapability(ModCapabilities.SANITY_CAP, null)) return false;
        ISanityCapability cap = mc.player.getCapability(ModCapabilities.SANITY_CAP, null);
        return cap != null && cap.getSanity() < 50;
    }

    // HUD pass: only when NO screen is open
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (Minecraft.getMinecraft().currentScreen != null) return; // the screen pass handles it
        if (!shouldApplySanityEffect()) return;

        ShaderHelper.loadShader();
        ShaderHelper.renderShaderOverlay();
    }

    // Screen pass: inventory, chests, pause menu, etc.
    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!shouldApplySanityEffect()) return;

        ShaderHelper.loadShader();
        ShaderHelper.renderShaderOverlay();
    }

    @SubscribeEvent
    public void onEnchantmentRegister(RegistryEvent.Register<Enchantment> event){
        event.getRegistry().register(ENCHANT_KLUG.setRegistryName("klug_enchant"));
    }
}