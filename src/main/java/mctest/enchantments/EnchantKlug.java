package mctest.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentFireAspect;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;

public class EnchantKlug extends Enchantment {
    public EnchantKlug(Rarity rarityIn, EnumEnchantmentType typeIn, EntityEquipmentSlot[] slots) {
        super(rarityIn, typeIn, slots);
    }

    @Override
    public boolean canApplyTogether(Enchantment other){
        return !(other instanceof EnchantmentFireAspect);
    }

    @Override
    public void onEntityDamaged(EntityLivingBase user, Entity target, int level) {
        // Only run this on the server side to prevent glitches
        if (!user.world.isRemote) {

            // Create the lightning bolt at the target's exact location
            EntityLightningBolt lightning = new EntityLightningBolt(
                    user.world,
                    target.posX,
                    target.posY,
                    target.posZ,
                    false // 'false' means it will set fires and do damage
            );

            // Add it to the world
            user.world.addWeatherEffect(lightning);
        }
    }
}