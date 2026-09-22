package com.natamus.starterkit.networking.packets;

import com.google.common.collect.Maps;
import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.starterkit.data.VariablesClient;
import com.natamus.starterkit.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

import java.util.HashMap;

public class ToClientReceiveKitDataPacket {
    public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "to_client_receive_kit_data_packet");

    private final HashMap<String, String> packetStarterGearEntries;
    private final HashMap<String, String> packetStarterKitDescriptions;

    public ToClientReceiveKitDataPacket(HashMap<String, String> entriesIn, HashMap<String, String> descriptionsIn) {
        this.packetStarterGearEntries = entriesIn;
        this.packetStarterKitDescriptions = descriptionsIn;
    }

    public static ToClientReceiveKitDataPacket decode(FriendlyByteBuf buf) {
        HashMap<String, String> entriesIn = ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8).decode(buf);
        HashMap<String, String> descriptionsIn = ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8).decode(buf);

        return new ToClientReceiveKitDataPacket(entriesIn, descriptionsIn);
    }

    public void encode(FriendlyByteBuf buf) {
        // Due to ambiguity on NeoForge, the lambda's are replaced
        ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8).encode(buf, packetStarterGearEntries); // FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf
        ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8).encode(buf, packetStarterKitDescriptions); // FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf
    }

    public static void handle(PacketContext<ToClientReceiveKitDataPacket> ctx) {
        if (ctx.side().equals(Side.CLIENT)) {
            ToClientReceiveKitDataPacket packet = ctx.message();

            VariablesClient.cachedStarterGearEntries = packet.packetStarterGearEntries;
            VariablesClient.cachedStarterKitDescriptions = packet.packetStarterKitDescriptions;

            VariablesClient.openChooseKitScreen = true;
        }
    }
}
