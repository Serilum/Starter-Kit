package com.natamus.starterkit.events;

import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.natamus.starterkit.data.Constants;
import com.natamus.starterkit.data.ConstantsClient;
import com.natamus.starterkit.data.VariablesClient;
import com.natamus.starterkit.functions.StarterClientFunctions;
import com.natamus.starterkit.inventory.StarterKitInventoryScreen;
import com.natamus.starterkit.networking.packets.ToServerAnnounceModIsInstalledPacket;

public class StarterClientEvents {
	public static void onClientTick() {
		// Workaround because getConnection() is null for NeoForge on EntityJoinLevelEvent.
		if (VariablesClient.waitingForAnnouncement) {
			if (ConstantsClient.mc.getConnection() != null) {
				VariablesClient.waitingForAnnouncement = false;

				Dispatcher.sendToServer(new ToServerAnnounceModIsInstalledPacket());
			}
		}

		// Workaround because NeoForge needs to have setScreen() on a main thread.
		if (VariablesClient.openChooseKitScreen) {
			VariablesClient.openChooseKitScreen = false;

            StarterClientFunctions.showInitialChooseKitScreen();
		}

		if (VariablesClient.priorPlayerEquipment == null) {
			return;
		}

		if (ConstantsClient.mc.screen instanceof StarterKitInventoryScreen) {
			return;
		}

		if (!StarterClientFunctions.removeLocalPlayerEquipment()) {
			Constants.logger.warn(Constants.logPrefix + "Unable to remove local player equipment.");
			return;
		}

		if (!StarterClientFunctions.setPriorLocalPlayerEquipment()) {
			Constants.logger.warn(Constants.logPrefix + "Unable to set local player's prior equipment.");
			return;
		}

		StarterClientFunctions.clearStarterKitClientCache();
	}
}
