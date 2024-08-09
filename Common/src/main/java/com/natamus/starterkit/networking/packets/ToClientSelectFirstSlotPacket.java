package com.natamus.starterkit.networking.packets;

import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.starterkit.functions.StarterClientFunctions;
import com.natamus.starterkit.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ToClientSelectFirstSlotPacket {
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "to_client_select_first_slot_packet");

    public ToClientSelectFirstSlotPacket() {
    }

    public static ToClientSelectFirstSlotPacket decode(FriendlyByteBuf buf) {
        return new ToClientSelectFirstSlotPacket();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(PacketContext<ToClientSelectFirstSlotPacket> ctx) {
        if (ctx.side().equals(Side.CLIENT)) {
            StarterClientFunctions.selectFirstSlot();
        }
    }
}
