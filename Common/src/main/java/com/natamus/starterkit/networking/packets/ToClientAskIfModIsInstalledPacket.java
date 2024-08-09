package com.natamus.starterkit.networking.packets;

import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.starterkit.data.VariablesClient;
import com.natamus.starterkit.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ToClientAskIfModIsInstalledPacket {
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "to_client_ask_if_mod_is_installed_packet");

    public ToClientAskIfModIsInstalledPacket() {
    }

    public static ToClientAskIfModIsInstalledPacket decode(FriendlyByteBuf buf) {
        return new ToClientAskIfModIsInstalledPacket();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(PacketContext<ToClientAskIfModIsInstalledPacket> ctx) {
        if (ctx.side().equals(Side.CLIENT)) {
            VariablesClient.waitingForAnnouncement = true;
        }
    }
}
