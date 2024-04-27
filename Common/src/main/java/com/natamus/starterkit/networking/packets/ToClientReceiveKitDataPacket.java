package com.natamus.starterkit.networking.packets;

import com.google.common.collect.Maps;
import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.starterkit.data.VariablesClient;
import com.natamus.starterkit.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class ToClientReceiveKitDataPacket {
    public static final ResourceLocation CHANNEL = new ResourceLocation(Reference.MOD_ID, "to_client_receive_kit_data_packet");

    private static HashMap<String, String> packetStarterGearEntries;
    private static HashMap<String, String> packetStarterKitDescriptions;

    public ToClientReceiveKitDataPacket(HashMap<String, String> entriesIn, HashMap<String, String> descriptionsIn) {
        packetStarterGearEntries = entriesIn;
        packetStarterKitDescriptions = descriptionsIn;
    }

    public static ToClientReceiveKitDataPacket decode(FriendlyByteBuf buf) {
        HashMap<String, String> entriesIn = buf.readMap(Maps::newHashMapWithExpectedSize, FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf);
        HashMap<String, String> descriptionsIn = buf.readMap(Maps::newHashMapWithExpectedSize, FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf);

        return new ToClientReceiveKitDataPacket(entriesIn, descriptionsIn);
    }

    public void encode(FriendlyByteBuf buf) {
        // Due to ambiguity on NeoForge, the lambda's are replaced
        buf.writeMap(packetStarterGearEntries, (a, b) -> a.writeUtf(b), (a, b) -> a.writeUtf(b)); // FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf
        buf.writeMap(packetStarterKitDescriptions, (a, b) -> a.writeUtf(b), (a, b) -> a.writeUtf(b)); // FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf
    }

    public static void handle(PacketContext<ToClientReceiveKitDataPacket> ctx) {
        if (ctx.side().equals(Side.CLIENT)) {
            VariablesClient.cachedStarterGearEntries = packetStarterGearEntries;
            VariablesClient.cachedStarterKitDescriptions = packetStarterKitDescriptions;

            VariablesClient.openChooseKitScreen = true;
        }
    }
}
