package reobf.proghatches.main.mixin.mixins.part2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.inv.MEInventoryCrafting;
import appeng.util.item.AEItemStack;
import reobf.proghatches.ae.ICondenser;
import reobf.proghatches.ae.cpu.IExternalManagerHolder;
import reobf.proghatches.gt.metatileentity.multi.LargeProgrammingCircuitProvider;
import reobf.proghatches.gt.metatileentity.util.IMultiplePatternPushable;
import reobf.proghatches.main.mixin.MixinCallback;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class MixinMultiPattern<T extends ICraftingMedium> {

    /*
     * @Unique
     * boolean isMulti;
     */
    /*
     * @Unique
     * T medium;
     *//*
        * @ModifyVariable( require = 1,method = "executeCrafting", at = @At(value = "INVOKE", target =
        * "pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"
        * ))
        * public ICraftingMedium b(ICraftingMedium a,@Share("isMulti") LocalBooleanRef isMulti) {
        * isMulti .set(a instanceof IMultiplePatternPushable);
        * //medium = (T) a;
        * return a;
        * }
        */

    @Inject(
        require = 1,
        method = "executeCrafting",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/networking/crafting/ICraftingMedium;pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"))
    public void a(CallbackInfo x, @Local MEInventoryCrafting local, @Share("inv") LocalRef<MEInventoryCrafting> inv) {

        inv.set(local);
    }

    /*
     * @Unique
     * ICraftingPatternDetails detail;
     * @ModifyArg( require = 1,method = "executeCrafting", at = @At(value = "INVOKE", target =
     * "Lappeng/api/networking/crafting/ICraftingMedium;pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"
     * ))
     * public ICraftingPatternDetails b(ICraftingPatternDetails a) {
     * detail = a;
     * return a;
     * }
     */
    /*
     * @Unique
     * java.util.Map.Entry e;
     * @ModifyVariable( require = 1,method = "executeCrafting", at = @At(value = "INVOKE", target =
     * "Lappeng/api/networking/crafting/ICraftingMedium;pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"
     * ))
     * public java.util.Map.Entry b(java.util.Map.Entry a) {
     * e = a;
     * return a;
     * }
     */
    @Shadow
    private MachineSource machineSrc;
    @Shadow
    private IItemList<IAEItemStack> waitingFor;

    @Shadow
    private void postChange(final IAEStack ais, final BaseActionSource src) {};

    @Shadow
    private void postCraftingStatusChange(final IAEStack diff) {};

    @Shadow
    private MECraftingInventory inventory;
    private static final IAEItemStack[] EMPTY = new IAEItemStack[0];

    @SuppressWarnings("deprecation")
	@Inject(
        require = 1,
        at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "markDirty"),
        method = "executeCrafting")
    public void MixinMultiPattern_executeCrafting(IEnergyGrid eg, CraftingGridCache cc, CallbackInfo ci2,
        @Local ICraftingMedium medium, @Local ICraftingPatternDetails detail, @Local java.util.Map.Entry e,
        @Share("inv") LocalRef<MEInventoryCrafting> inv0/*
                                                       * ,
                                                       * @Share("isMulti") LocalBooleanRef isMulti
                                                       */) {

        boolean inf = false;
        // System.out.println(medium);
        if (medium instanceof LargeProgrammingCircuitProvider) {
            if (((LargeProgrammingCircuitProvider) medium).instant()) inf = true;

        }
        InventoryCrafting inv = inv0.get();
        // if (isMulti.get()) {
        if (medium instanceof IMultiplePatternPushable) {
            int used = 0;

            LinkedList<Object> is = new LinkedList<>();
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (inv.getStackInSlot(i) != null) {
                    is.addLast(inv.getStackInSlot(i));
                }
            }
            ListIterator<Object> itr = is.listIterator();
            while (itr.hasNext()) {
                Object o = itr.next();
                if (o == null) {
                    itr.remove();
                }
                if (((ItemStack) o).getItem() instanceof ItemFluidPacket) {
                    o = (ItemFluidDrop.newStack(ItemFluidPacket.getFluidStack((ItemStack) o)));
                }
                itr.set(AEItemStack.create((ItemStack) o));
            }

            IAEItemStack[] input = is.toArray(EMPTY);

            long[] nums = new long[input.length];
            for (int x = 0; x < input.length; x++) {
                IAEItemStack tmp = input[x].copy()
                    .setStackSize(Long.MAX_VALUE);
                final IAEStack ais = this.inventory.extractItems(tmp, Actionable.MODULATE, this.machineSrc);
                if (ais != null) {
                    nums[x] =  ais.getStackSize();
                    this.postChange(ais, this.machineSrc);
                }
            }
            try {

            	long best = Long.MAX_VALUE;
                boolean any = false;
                for (int x = 0; x < input.length; x++) {
                    if (input[x].getStackSize() > 0) {
                        long num =  (nums[x] / input[x].getStackSize());
                        if (num < best) best = num;
                        any = true;
                    }
                }
                if (any == false) {
                    // return;
                }

                long num = MixinCallback.getter.apply(e.getValue());
                /* final */ long max = getMaxSkips();
                if (inf) {
                    max = Integer.MAX_VALUE - 1;
                }

                int maxtry = (int) Math.min(
                    Math.min(
                        (int) (num > (Integer.MAX_VALUE - 1) ? (Integer.MAX_VALUE - 1) : num) - 1,
                        /*(int) */(remainingOperations + max)),

                         best
                    
                		);

                if (maxtry <= 0) {
                    return;
                }

                int[] retarr = ((IMultiplePatternPushable) medium).pushPatternMulti(detail, inv, maxtry);
                used = retarr[0];
                int parallelused = used;// retarr.length>1?retarr[1]:used;

                if (max != Integer.MAX_VALUE) remainingOperations -= Math.max(parallelused - max, 0);
                MixinCallback.setter.accept(e.getValue(), num - used);

                if (used > 0) {
                    for (IAEItemStack out : detail.getCondensedOutputs()) {
                        out = out.copy()
                            .setStackSize(used * out.getStackSize());
                        this.postChange(out, this.machineSrc);
                        this.waitingFor.add(out.copy());
                        this.postCraftingStatusChange(out.copy());
                    }
                }

            } finally {
                // return all unused
                for (int x = 0; x < input.length; x++) {
                    this.inventory.injectItems(
                        input[x].copy()
                            .setStackSize(
                                /* all availavle - pushed recipes*per recipe */
                                nums[x] - used * input[x].getStackSize()),
                        Actionable.MODULATE,
                        this.machineSrc);

                }
            }

        } else {

            /* final */ long max = getMaxSkips();
            if (inf) max = Integer.MAX_VALUE - 1;
            if (max <= 0) return;
            // int now = temp1.getOrDefault(detail, 0);

            stop: for (int i = 0; i < max; i = (i < Integer.MAX_VALUE - 10) ? (i + 1) : i) {
                if (MixinCallback.getter.apply(e.getValue()) <= 1) {
                    break stop;
                }
                if (medium.isBusy()) {
                    break stop;
                }

                if (detail.isCraftable()) {
                    break stop;// that's impossible to be done in same tick
                }
                MEInventoryCrafting ic = detail.isCraftable() ? new MEInventoryCrafting(new ContainerNull(), 3, 3)
                		 : new MEInventoryCrafting(new ContainerNull(), detail.getAEInputs().length, 1);
                @SuppressWarnings("deprecation")
				final IAEItemStack[] input = detail.getInputs();
                boolean found = true;
               /* for (int x = 0; x < input.length; x++) {
                    // System.out.println(input[x]);
                    if (input[x] != null && input[x].getStackSize() > 0) {
                        found = false;
                        for (IAEItemStack ias : getExtractItems(input[x], detail)) {
                            // System.out.println(ias);
                            final IAEStack ais = this.inventory
                                .extractItems(ias, Actionable.MODULATE, this.machineSrc);
                            final ItemStack is = ais == null ? null : ais.getItemStack();
                            // System.out.println(ais);
                            if (ias.getStackSize() != ((is == null) ? 0 : is.stackSize)) continue;

                            found = true;
                            ic.setInventorySlotContents(x, is);
                            if (!detail.canBeSubstitute() && is.stackSize == input[x].getStackSize()) {
                                this.postChange(input[x], this.machineSrc);
                                break;
                            } else {
                                this.postChange(AEItemStack.create(is), this.machineSrc);
                            }
                        }
                        if (!found) {
                            break;
                        }
                    }
                }*/
                for (int x = 0; x < input.length; x++) {
                    if (input[x] != null) {
                        found = false;
                        for (IAEStack ias : getExtractItems(input[x], detail)) {
                            IAEStack tempStack = ias.copy();
                            /*if (detail.isCraftable()
                                    && !detail.isValidItemForSlot(x, tempStack, this.getWorld()))
                                continue;
*/
                            final IAEStack<?> aes = this.inventory.extractItems(tempStack, Actionable.MODULATE);
                            if (aes != null) {
                                found = true;
                                ic.setInventorySlotContents(x, aes);
                                if (!detail.canBeSubstitute()
                                        && aes.getStackSize() == input[x].getStackSize()) {
                                    this.postChange(input[x], this.machineSrc);
                                    break;
                                } else {
                                    this.postChange(aes, this.machineSrc);
                                }
                            }
                        }
                        if (!found) {
                            break;
                        }
                    }
                }
                // System.out.println(found);
                if (!found) {
                    // put stuff back..
                    for (int x = 0; x < ic.getSizeInventory(); x++) {
                        ItemStack is = ic.getStackInSlot(x);
                        if (is != null) {
                            if (is.getItem() instanceof ItemFluidPacket) {

                                is = ItemFluidDrop.newStack(ItemFluidPacket.getFluidStack(is));

                            }

                            this.inventory.injectItems(AEItemStack.create(is), Actionable.MODULATE, this.machineSrc);
                        }
                    }
                    ic = null;
                    break stop;
                }
                if (medium.pushPattern(detail, ic)) {
                    MixinCallback.setter.accept(e.getValue(), MixinCallback.getter.apply(e.getValue()) - 1);
                    for (IAEItemStack out : detail.getCondensedOutputs()) {

                        this.postChange(out, this.machineSrc);
                        this.waitingFor.add(out.copy());
                        this.postCraftingStatusChange(out.copy());
                    }
                } else {
                    if (ic != null) {
                        // put stuff back..
                        for (int x = 0; x < ic.getSizeInventory(); x++) {
                            ItemStack is = ic.getStackInSlot(x);

                            if (is != null) {
                                if (is.getItem() instanceof ItemFluidPacket) {

                                    is = ItemFluidDrop.newStack(ItemFluidPacket.getFluidStack(is));

                                }
                                this.inventory
                                    .injectItems(AEItemStack.create(is), Actionable.MODULATE, this.machineSrc);
                            }
                        }
                    }
                    break stop;

                }

            }

        }

    }

    @Shadow
    private ArrayList<IAEStack<?>> getExtractItems(IAEStack ingredient, ICraftingPatternDetails patternDetails) {
        return null;
    };

    @Shadow
    int remainingOperations;

    /*
     * @Inject( require = 1,at = @At(value = "RETURN"), method = "executeCrafting")
     * public void ret(IEnergyGrid eg, CraftingGridCache cc, CallbackInfo ci) {
     * //detail = null;
     * //e = null;
     * //inv = null;
     * //medium = null;
     * if (getMaxSkips() <= 0)
     * return;
     * //temp1.clear();
     * }
     */

    ////////// xxxxxxxxxx
    // @Shadow
    // private int remainingOperations;
    // @Unique
    // boolean skip;
    /*
     * @Unique
     * Reference2IntOpenHashMap<ICraftingPatternDetails> temp1 = new
     * Reference2IntOpenHashMap<ICraftingPatternDetails>();
     */
    private long maxSkips;
    /*
     * @Inject(at = @At(value = "RETURN"), method = "updateCraftingLogic")
     * public void a(final IGrid grid, final IEnergyGrid eg, final
     * CraftingGridCache cc, CallbackInfo it) {
     * }
     */

    /*
     * @ModifyVariable(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
     * target =
     * "Lappeng/me/cluster/implementations/CraftingCPUCluster;remainingOperations"
     * ), method = "executeCrafting")
     * public ICraftingPatternDetails a(ICraftingPatternDetails m) {
     * if(getMaxSkips()<=0)return m; int now= temp1 .getOrDefault(m, 0); skip
     * =getMaxSkips()>=now;
     * if(now<Integer.MAX_VALUE-10) temp1.put(m,now+1);
     * return m; }
     */

    /*
     * @WrapWithCondition(remap = false, at = {
     * @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target =
     * "Lappeng/me/cluster/implementations/CraftingCPUCluster;remainingOperations")
     * }, method = { "executeCrafting" }) private boolean b(CraftingCPUCluster
     * thiz,int neo) { if(getMaxSkips()<=0)return true;
     * return !skip; }
     */
    private long getMaxSkips() {
    	if(((IExternalManagerHolder)this).getIExternalManager()!=null){
    		
    		return ((IExternalManagerHolder)this).getIExternalManager().getCondenser();
    		
    		
    	}
        return maxSkips;
    }

    @Inject(require = 1, at = @At(value = "RETURN"), method = "addTile")
    public void addTile(TileCraftingTile te, CallbackInfo it) {
        if (te instanceof ICondenser) {
            ICondenser con = (ICondenser) te;
            if (con.isinf()) {
                maxSkips = Integer.MAX_VALUE;
            } else {

                maxSkips = Long.max(maxSkips, maxSkips + con.getSkips());
            }

        }
    }

}
