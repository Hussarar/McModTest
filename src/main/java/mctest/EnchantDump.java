package mctest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemCarrotOnAStick;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dumps every registered enchantment (vanilla + modded) to
 * <game dir>/enchantment_dump/enchantments.txt and enchantments.json
 *
 * Runs automatically every time a world/server starts (so all mods are loaded
 * and language files are available) and on demand with /dumpenchants
 */
@Mod(modid = EnchantDump.MODID, name = "Enchantment Dumper", version = "1.0.0",
        acceptedMinecraftVersions = "[1.12.2]")
public class EnchantDump {

    public static final String MODID = "enchantdump";
    private static final Logger LOG = LogManager.getLogger(MODID);
    private static final String[] ROMAN = {"0", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandBase() {
            @Override
            public String getName() {
                return "dumpenchants";
            }

            @Override
            public String getUsage(ICommandSender sender) {
                return "/dumpenchants";
            }

            @Override
            public int getRequiredPermissionLevel() {
                return 0;
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                try {
                    File dir = dump();
                    sender.sendMessage(new TextComponentString("Enchantments written to " + dir.getAbsolutePath()));
                } catch (Exception e) {
                    LOG.error("Dump failed", e);
                    sender.sendMessage(new TextComponentString("Dump failed: " + e));
                }
            }
        });

        try {
            File dir = dump();
            LOG.info("Enchantment dump written to {}", dir.getAbsolutePath());
        } catch (Exception e) {
            LOG.error("Automatic enchantment dump failed", e);
        }
    }

    public static File dump() throws IOException {
        File dir = new File(Loader.instance().getConfigDir().getParentFile(), "enchantment_dump");
        Files.createDirectories(dir.toPath());

        List<Enchantment> all = new ArrayList<>(ForgeRegistries.ENCHANTMENTS.getValuesCollection());
        all.sort(Comparator.comparing(e -> e.getRegistryName().toString()));

        // Every item in the game, once
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            ItemStack s = new ItemStack(item);
            if (!s.isEmpty()) {
                stacks.add(s);
            }
        }
        stacks.sort(Comparator.comparing(s -> s.getItem().getRegistryName().toString()));

        StringBuilder txt = new StringBuilder();
        JsonArray json = new JsonArray();

        for (Enchantment e : all) {
            String id = e.getRegistryName().toString();
            String name = I18n.translateToLocal(e.getName());

            // --- Enchantability cost per level ---
            List<String> costs = new ArrayList<>();
            JsonArray costJson = new JsonArray();
            for (int lvl = e.getMinLevel(); lvl <= e.getMaxLevel(); lvl++) {
                int min = e.getMinEnchantability(lvl);
                int max = e.getMaxEnchantability(lvl);
                costs.add("Level " + roman(lvl) + ": " + min + " - " + max);
                JsonObject c = new JsonObject();
                c.addProperty("level", lvl);
                c.addProperty("minEnchantability", min);
                c.addProperty("maxEnchantability", max);
                costJson.add(c);
            }

            // --- Incompatibilities ---
            List<String> incompatible = new ArrayList<>();
            for (Enchantment other : all) {
                if (other != e && !e.isCompatibleWith(other)) {
                    incompatible.add(other.getRegistryName().toString());
                }
            }

            // --- Applicable items ---
            List<String> table = new ArrayList<>();
            List<String> anvilOnly = new ArrayList<>();
            for (ItemStack s : stacks) {
                try {
                    String label = s.getItem().getRegistryName() + " (ench " + s.getItem().getItemEnchantability(s) + ")";
                    if (e.canApplyAtEnchantingTable(s)) {
                        table.add(label);
                    } else if (e.canApply(s)) {
                        anvilOnly.add(label);
                    }
                } catch (Throwable t) {
                    // some modded items misbehave with default stacks - skip them
                }
            }

            String type = e.type != null ? e.type.name() : "NONE";

            // --- Text output ---
            List<String> incompatNames = new ArrayList<>();
            for (Enchantment other : all) {
                if (other != e && !e.isCompatibleWith(other)) {
                    incompatNames.add(I18n.translateToLocal(other.getName()));
                }
            }
            // Which item categories (Swords, Armor, Tools ...) can take this enchantment
            Set<String> found = new HashSet<>();
            for (ItemStack s : stacks) {
                try {
                    if (e.canApply(s) || e.canApplyAtEnchantingTable(s)) {
                        found.add(categoryOf(s));
                    }
                } catch (Throwable t) {
                    // skip misbehaving modded items
                }
            }
            List<String> categories = collapse(found);

            txt.append("-------------------------------\n");
            txt.append(name).append('\n');
            txt.append("max lvl ").append(e.getMaxLevel()).append('\n');
            txt.append("Incompatible: ").append(incompatNames.isEmpty() ? "none" : String.join(", ", incompatNames)).append('\n');
            txt.append("Goes on: ").append(categories.isEmpty() ? "none" : String.join(", ", categories)).append('\n');
            txt.append("-------------------------------\n\n");

            // --- JSON output ---
            JsonObject o = new JsonObject();
            o.addProperty("id", id);
            o.addProperty("name", name);
            o.addProperty("numericId", Enchantment.getEnchantmentID(e));
            o.addProperty("rarity", e.getRarity().name());
            o.addProperty("rarityWeight", e.getRarity().getWeight());
            o.addProperty("category", type);
            o.addProperty("minLevel", e.getMinLevel());
            o.addProperty("maxLevel", e.getMaxLevel());
            o.addProperty("curse", e.isCurse());
            o.addProperty("treasure", e.isTreasureEnchantment());
            o.addProperty("allowedOnBooks", e.isAllowedOnBooks());
            o.add("enchantabilityPerLevel", costJson);
            o.add("incompatibleWith", toJson(incompatible));
            o.add("itemsEnchantingTable", toJson(table));
            o.add("itemsAnvilOnly", toJson(anvilOnly));
            json.add(o);
        }

        write(new File(dir, "enchantments.txt"), txt.toString());
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        write(new File(dir, "enchantments.json"), gson.toJson(json));
        return dir;
    }

    private static final String[] ORDER = {"Swords", "Axes", "Pickaxes", "Shovels", "Hoes",
            "Helmets", "Chestplates", "Leggings", "Boots", "Bows", "Fishing Rods", "Shears",
            "Elytra", "Shields", "Flint and Steel", "Carrot on a Stick", "Other items"};
    private static final List<String> ARMOR = Arrays.asList("Helmets", "Chestplates", "Leggings", "Boots");
    private static final List<String> TOOLS = Arrays.asList("Axes", "Pickaxes", "Shovels", "Hoes");

    private static String categoryOf(ItemStack s) {
        Item i = s.getItem();
        if (i instanceof ItemArmor) {
            switch (((ItemArmor) i).getEquipmentSlot()) {
                case HEAD: return "Helmets";
                case CHEST: return "Chestplates";
                case LEGS: return "Leggings";
                case FEET: return "Boots";
                default: return "Other items";
            }
        }
        if (i instanceof ItemSword) return "Swords";
        if (i instanceof ItemAxe) return "Axes";
        if (i instanceof ItemPickaxe) return "Pickaxes";
        if (i instanceof ItemSpade) return "Shovels";
        if (i instanceof ItemHoe) return "Hoes";
        if (i instanceof ItemBow) return "Bows";
        if (i instanceof ItemFishingRod) return "Fishing Rods";
        if (i instanceof ItemShears) return "Shears";
        if (i instanceof ItemElytra) return "Elytra";
        if (i instanceof ItemShield) return "Shields";
        if (i instanceof ItemFlintAndSteel) return "Flint and Steel";
        if (i instanceof ItemCarrotOnAStick) return "Carrot on a Stick";
        return "Other items";
    }

    /** Orders the categories and merges the four armor pieces into "Armor" and the four tools into "Tools". */
    private static List<String> collapse(Set<String> found) {
        boolean armorAll = found.containsAll(ARMOR);
        boolean toolsAll = found.containsAll(TOOLS);
        List<String> out = new ArrayList<>();
        for (String c : ORDER) {
            if (!found.contains(c)) continue;
            if (armorAll && ARMOR.contains(c)) {
                if (c.equals("Helmets")) out.add("Armor");
                continue;
            }
            if (toolsAll && TOOLS.contains(c)) {
                if (c.equals("Axes")) out.add("Tools");
                continue;
            }
            out.add(c);
        }
        return out;
    }

    private static String roman(int n) {
        return n >= 0 && n < ROMAN.length ? ROMAN[n] : String.valueOf(n);
    }

    private static void appendList(StringBuilder sb, List<String> list) {
        if (list.isEmpty()) {
            sb.append("    (none)\n");
            return;
        }
        for (String s : list) {
            sb.append("    - ").append(s).append('\n');
        }
    }

    private static JsonArray toJson(List<String> list) {
        JsonArray a = new JsonArray();
        for (String s : list) {
            a.add(s);
        }
        return a;
    }

    private static void write(File f, String content) throws IOException {
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(f.toPath()), StandardCharsets.UTF_8)) {
            w.write(content);
        }
    }
}


