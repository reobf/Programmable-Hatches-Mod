package reobf.proghatches.gt.metatileentity.util;

import java.util.function.ObjLongConsumer;

import appeng.api.storage.data.IAEStack;

public interface IMEOutputCacheDrain {

    void proghatches$drainCache(ObjLongConsumer<IAEStack<?>> sink);
}
