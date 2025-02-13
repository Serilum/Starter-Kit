package com.natamus.starterkit.networking.packets;

import com.natamus.collective.functions.MessageFunctions;
import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.starterkit.config.ConfigHandler;
import com.natamus.starterkit.functions.StarterCheckFunctions;
import com.natamus.starterkit.functions.StarterGearFunctions;
import com.natamus.starterkit.util.Reference;
import com.natamus.starterkit.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ToServerSendKitChoicePacket {
    public static final ResourceLocation CHANNEL = new ResourceLocation(Reference.MOD_ID, "to_server_send_kit_choice_packet");

    private final String kitName;

    public ToServerSendKitChoicePacket(String kitNameIn) {
        this.kitName = kitNameIn;
    }

    public static ToServerSendKitChoicePacket decode(FriendlyByteBuf buf) {
        String kitNameIn = buf.readUtf();

        return new ToServerSendKitChoicePacket(kitNameIn);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(kitName);
    }

    public static void handle(PacketContext<ToServerSendKitChoicePacket> ctx) {
        if (ctx.side().equals(Side.SERVER)) {
            ToServerSendKitChoicePacket packet = ctx.message();
            Player player = ctx.sender();

            if (StarterCheckFunctions.shouldPlayerReceiveStarterKit(player)) {
                Level level = player.level();
                if (level.getServer().isDedicatedServer()) {
                    if (ConfigHandler.announcePlayerKitChoiceInDedicatedServer) {
                        MessageFunctions.broadcastMessage(level, Component.literal(player.getName().getString() + " has chosen the '" + Util.formatKitName(packet.kitName) + "' kit!").withStyle(ChatFormatting.DARK_GREEN));
                    }
                }
                else {
                    MessageFunctions.sendMessage(player, "You have been given the '" + Util.formatKitName(packet.kitName) + "' starter kit.", ChatFormatting.DARK_GREEN, true);
                }

                StarterGearFunctions.giveStarterKit(player, null, packet.kitName);
            }
        }
    }
}
