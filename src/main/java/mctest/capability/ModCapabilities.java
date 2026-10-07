package mctest.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModCapabilities {

    // 1. Der "Schlüssel" zu unserer Capability. Forge füllt das automatisch!
    @CapabilityInject(ISanityCapability.class)
    public static Capability<ISanityCapability> SANITY_CAP = null;

    // 2. Die Registrierung (Das "Amt")
    public static void register() {
        CapabilityManager.INSTANCE.register(ISanityCapability.class, new Capability.IStorage<ISanityCapability>() {

            // Wie speichern wir? (Da wir nur eine Zahl haben, nutzen wir NBTTagInt)
            @Override
            public NBTBase writeNBT(Capability<ISanityCapability> cap, ISanityCapability instance, EnumFacing side) {
                return new NBTTagInt(instance.getSanity());
            }

            // Wie laden wir?
            @Override
            public void readNBT(Capability<ISanityCapability> cap, ISanityCapability instance, EnumFacing side, NBTBase nbt) {
                instance.setSanity(((NBTPrimitive) nbt).getInt());
            }

        }, SanityCapability::new); // Erstellt automatisch neue SanityCapability Objekte
    }

    // 3. Das Anhängen an den Spieler (Die "Straße")
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(new ResourceLocation("mctest", "sanity"), new ICapabilitySerializable<NBTBase>() {
                private final SanityCapability instance = new SanityCapability();
                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                    return capability == SANITY_CAP;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                    // FIX: Wir geben jetzt immer dieselbe "instance" zurück!
                    return capability == SANITY_CAP ? (T) instance : null;
                }

                // Diese zwei Methoden nutzen einfach das Storage von oben zum Speichern/Laden
                @Override
                public NBTBase serializeNBT() {
                    return SANITY_CAP.getStorage().writeNBT(SANITY_CAP, getCapability(SANITY_CAP, null), null);
                }

                @Override
                public void deserializeNBT(NBTBase nbt) {
                    SANITY_CAP.getStorage().readNBT(SANITY_CAP, getCapability(SANITY_CAP, null), null, nbt);
                }
            });
        }
    }
}