package reobf.proghatches.gt.metatileentity.util;

import appeng.api.networking.events.MENetworkStorageEvent;

public interface IMEStorageChangeAwareness {
	public void storageChange(MENetworkStorageEvent w);
}
