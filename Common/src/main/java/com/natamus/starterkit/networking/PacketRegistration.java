package com.natamus.starterkit.networking;

import com.natamus.collective.implementations.networking.api.Network;
import com.natamus.starterkit.networking.packets.*;

public class PacketRegistration {

    public void init() {
        initClientPackets();
        initServerPackets();
    }

    private void initClientPackets() {
        Network.registerPacket(ToClientAskIfModIsInstalledPacket.CHANNEL, ToClientAskIfModIsInstalledPacket.class, ToClientAskIfModIsInstalledPacket::encode, ToClientAskIfModIsInstalledPacket::decode, ToClientAskIfModIsInstalledPacket::handle)

        .registerPacket(ToClientReceiveKitDataPacket.CHANNEL, ToClientReceiveKitDataPacket.class, ToClientReceiveKitDataPacket::encode, ToClientReceiveKitDataPacket::decode, ToClientReceiveKitDataPacket::handle)

        .registerPacket(ToClientSelectFirstSlotPacket.CHANNEL, ToClientSelectFirstSlotPacket.class, ToClientSelectFirstSlotPacket::encode, ToClientSelectFirstSlotPacket::decode, ToClientSelectFirstSlotPacket::handle);
    }

    private void initServerPackets() {
        Network.registerPacket(ToServerAnnounceModIsInstalledPacket.CHANNEL, ToServerAnnounceModIsInstalledPacket.class, ToServerAnnounceModIsInstalledPacket::encode, ToServerAnnounceModIsInstalledPacket::decode, ToServerAnnounceModIsInstalledPacket::handle)

        .registerPacket(ToServerSendKitChoicePacket.CHANNEL, ToServerSendKitChoicePacket.class, ToServerSendKitChoicePacket::encode, ToServerSendKitChoicePacket::decode, ToServerSendKitChoicePacket::handle);
    }
}
