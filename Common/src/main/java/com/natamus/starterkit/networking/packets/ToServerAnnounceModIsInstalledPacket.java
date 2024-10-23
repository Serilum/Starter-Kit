package com.natamus.starterkit.networking.packets;

import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.starterkit.data.Variables;
import com.natamus.starterkit.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ToServerAnnounceModIsInstalledPacket {
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "to_server_announce_mod_is_installed_packet");

    public ToServerAnnounceModIsInstalledPacket() {
    }

    public static ToServerAnnounceModIsInstalledPacket decode(FriendlyByteBuf buf) {
        return new ToServerAnnounceModIsInstalledPacket();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(PacketContext<ToServerAnnounceModIsInstalledPacket> ctx) {
        if (ctx.side().equals(Side.SERVER)) {
            Player player = ctx.sender();
            UUID uuid = player.getUUID();

            if (!Variables.playersWithModInstalledOnClient.contains(uuid)) {
                Variables.playersWithModInstalledOnClient.add(uuid);
            }

            Dispatcher.sendToClient(new ToClientReceiveKitDataPacket(Variables.starterGearEntries, Variables.starterKitDescriptions), (ServerPlayer)player);
        }
    }
}
