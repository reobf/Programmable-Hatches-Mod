package reobf.proghatches.main.mixin.mixins.part2;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.MECraftingInventory;
import appeng.crafting.v2.CraftingContext;
import appeng.crafting.v2.CraftingRequest;
import appeng.crafting.v2.resolvers.CraftingTask;
import appeng.crafting.v2.resolvers.ExtractItemResolver;
import reobf.proghatches.ae.IIsExtractFromInvAllowed;

@Mixin(value = ExtractItemResolver.ExtractItemTask.class, remap = false)
public abstract class MixinExtractIntercept extends CraftingTask {

    protected MixinExtractIntercept(CraftingRequest request, int priority) {
        super(request, priority);

    }

    @Inject(require = 1, method = "extractExact", at = { @At("HEAD") }, cancellable = true)

    public void extract(CraftingContext context, MECraftingInventory source, List<IAEItemStack> removedList,
        CallbackInfo c) {
        if (!((IIsExtractFromInvAllowed) (Object) context).isAllowed(this.request.stack)) {

            c.cancel();
        }

    }
}
