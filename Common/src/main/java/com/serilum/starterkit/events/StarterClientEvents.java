package com.serilum.starterkit.events;

import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.serilum.starterkit.data.Constants;
import com.serilum.starterkit.data.ConstantsClient;
import com.serilum.starterkit.data.VariablesClient;
import com.serilum.starterkit.functions.StarterClientFunctions;
import com.serilum.starterkit.inventory.StarterKitInventoryScreen;
import com.serilum.starterkit.networking.packets.ToServerAnnounceModIsInstalledPacket;

public class StarterClientEvents {
	public static void onClientTick() {
		// Workaround because getConnection() is null for NeoForge on EntityJoinLevelEvent.
		if (VariablesClient.waitingForAnnouncement) {
			if (ConstantsClient.mc.getConnection() != null) {
				VariablesClient.waitingForAnnouncement = false;

				Dispatcher.sendToServer(new ToServerAnnounceModIsInstalledPacket());
			}
		}

		// Adds compatibility with other modded startup screens.
		if (VariablesClient.openChooseKitScreen) {
			if (ConstantsClient.mc.gui.screen()!= null) {
				VariablesClient.anotherScreenWasOpen = true;
			}
			else if (VariablesClient.anotherScreenWasOpen || VariablesClient.openChooseKitScreenTicks > 60) {
				VariablesClient.openChooseKitScreen = false;
				VariablesClient.anotherScreenWasOpen = false;
				VariablesClient.openChooseKitScreenTicks = 0;

				StarterClientFunctions.showInitialChooseKitScreen();
			}

			VariablesClient.openChooseKitScreenTicks++;
		}

		if (VariablesClient.priorPlayerEquipment == null) {
			return;
		}

		if (ConstantsClient.mc.gui.screen()instanceof StarterKitInventoryScreen) {
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
