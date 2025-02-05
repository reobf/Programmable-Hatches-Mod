package reobf.proghatches.main.mixin.mixins.part2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;

import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import reobf.proghatches.main.MyMod;

@Mixin(value = ModularUIContainer.class, remap = false)
public abstract class MixinRecursiveSlotClickProtection extends Container {

    private int count;

    @Override
    protected void retrySlotClick(int slotid, int p_75133_2_, boolean p_75133_3_, EntityPlayer p_75133_4_) {
        try {
            count++;
            if (count > 10) {
                if (this.inventorySlots.get(slotid) instanceof BaseSlot) {

                    BaseSlot bs = (BaseSlot) this.inventorySlots.get(slotid);
                    if (bs.getItemHandler() instanceof MappingItemHandler) {

                        MyMod.LOG.warn(
                            "Container retrySlotClick recursive call is too deep(10), it's prevented to avoid crash.");
                        MyMod.LOG.warn(
                            "This might be normal when shift-click a stack of many items(like 10M), nothing to worry about, carry on~");
                        return;
                    }

                }
            }

            if (count > 50) {

                MyMod.LOG
                    .warn("Container retrySlotClick recursive call is too deep(50), it's prevented to avoid crash.");
                MyMod.LOG.warn(
                    "Player who opens this container:" + getContext().getPlayer()
                        .getDisplayName()
                        + "@["
                        + getContext().getPlayer().posX
                        + ","
                        + getContext().getPlayer().posY
                        + ","
                        + getContext().getPlayer().posZ
                        + ",DIM="
                        + getContext().getPlayer()
                            .getEntityWorld().provider.dimensionId
                        + "]");

                Thread.dumpStack();
            }

            super.retrySlotClick(slotid, p_75133_2_, p_75133_3_, p_75133_4_);
        } finally {
            count--;
        }
    }

    @Shadow(remap = false)
    public abstract ModularUIContext getContext();

    /*
     * @Inject(method="transferItem",at = { @At("INVOKE_ASSIGN"), })
     * public void check(BaseSlot fromSlot, ItemStack fromStack,CallbackInfoReturnable r){}
     */

}
