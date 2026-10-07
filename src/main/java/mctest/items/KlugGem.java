package mctest.items;

import mctest.McModTest;
import mctest.capability.ISanityCapability;
import mctest.capability.ModCapabilities;
import mctest.capability.SanitySyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KlugGem extends Item {
    public static final Logger LOGGER = LogManager.getLogger();
    public KlugGem(){
        this.setRegistryName("KlugGem");
        this.setTranslationKey("");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        ISanityCapability sanitycap = playerIn.getCapability(ModCapabilities.SANITY_CAP, null);
        if(!worldIn.isRemote) {
            String health = "" + playerIn.getHealth();
            BlockPos playerWorldPos = playerIn.getPosition();
            if (playerIn.onGround) {
                String light = "" + worldIn.getLight(playerWorldPos);
                worldIn.isDaytime();
                //LOGGER.debug(light);
                //LOGGER.debug(playerWorldPos);
            }


            sanitycap.addSanity(-5);
            McModTest.NETWORK.sendTo(new SanitySyncPacket(sanitycap.getSanity()), (net.minecraft.entity.player.EntityPlayerMP) playerIn);
            //LOGGER.debug(health);
            LOGGER.debug("I AM Server" + sanitycap.getSanity());
        }
        if (worldIn.isRemote) {
            if (sanitycap.getSanity() < 50) {
                Minecraft.getMinecraft().getSoundHandler().playSound(
                        new PositionedSoundRecord(
                                SoundEvents.MUSIC_MENU.getSoundName(),
                                SoundCategory.PLAYERS,
                                1.0F,                           // volume
                                1.0F,                           // pitch
                                false,                          // repeat
                                0,                              // repeat delay
                                ISound.AttenuationType.NONE,
                                (float) playerIn.posX,
                                (float) (playerIn.posY + playerIn.getEyeHeight()),
                                (float) playerIn.posZ
                        )
                );
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
    }
}
