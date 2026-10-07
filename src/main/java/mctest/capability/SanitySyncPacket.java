package mctest.capability;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SanitySyncPacket implements IMessage {

    private int currentSanity;

    // WICHTIG: Forge braucht immer einen leeren Konstruktor!
    public SanitySyncPacket() {}

    // Dieser Konstruktor wird vom Server benutzt, um die Nachricht zu erstellen
    public SanitySyncPacket(int currentSanity) {
        this.currentSanity = currentSanity;
    }

    // Schreibt die Daten in die "Postkarte" (Server -> Netzwerk)
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(currentSanity);
    }

    // Liest die Daten aus der "Postkarte" (Netzwerk -> Client)
    @Override
    public void fromBytes(ByteBuf buf) {
        this.currentSanity = buf.readInt();
    }

    // Der Empfänger (Client), der die Postkarte öffnet
    public static class Handler implements IMessageHandler<SanitySyncPacket, IMessage> {
        @Override
        public IMessage onMessage(SanitySyncPacket message, MessageContext ctx) {
            // Wir müssen sicherstellen, dass dieser Code auf dem Client-Thread läuft
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EntityPlayer player = Minecraft.getMinecraft().player;
                if (player != null && player.hasCapability(ModCapabilities.SANITY_CAP, null)) {
                    // Hier aktualisieren wir die Capability des Clients mit dem Wert vom Server!
                    ISanityCapability cap = player.getCapability(ModCapabilities.SANITY_CAP, null);
                    cap.setSanity(message.currentSanity);
                }
            });
            return null; // Wir schicken keine Antwort zurück
        }
    }
}