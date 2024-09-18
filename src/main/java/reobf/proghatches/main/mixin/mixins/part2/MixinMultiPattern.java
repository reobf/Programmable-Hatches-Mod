package reobf.proghatches.main.mixin.mixins.part2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.item.AEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import reobf.proghatches.eucrafting.IInstantCompletable;
import reobf.proghatches.gt.metatileentity.util.IMultiplePatternPushable;
import reobf.proghatches.main.mixin.MixinCallback;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public class MixinMultiPattern<T extends IMultiplePatternPushable & ICraftingMedium> {
	@Unique
	boolean isMulti;
	@Unique
	T medium;

	@ModifyVariable(method = "executeCrafting", at = @At(value = "INVOKE", target = "pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"))
	public ICraftingMedium b(ICraftingMedium a) {
		isMulti = a instanceof IMultiplePatternPushable;
		if (isMulti)
			medium = (T) a;
		return a;
	}

	@Unique
	InventoryCrafting inv;

	@ModifyArg(method = "executeCrafting", at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingMedium;pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"))
	public InventoryCrafting a(InventoryCrafting a) {
		if (isMulti)
			inv = a;
		return a;
	}

	@Unique
	ICraftingPatternDetails detail;

	@ModifyArg(method = "executeCrafting", at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingMedium;pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"))
	public ICraftingPatternDetails b(ICraftingPatternDetails a) {
		if (isMulti)
			detail = a;
		return a;
	}

	@Unique
	java.util.Map.Entry e;

	@ModifyVariable(method = "executeCrafting", at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingMedium;pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"))
	public java.util.Map.Entry b(java.util.Map.Entry a) {
		if (isMulti)
			e = a;
		return a;
	}

	@Shadow
	private MachineSource machineSrc;
	@Shadow
	private IItemList<IAEItemStack> waitingFor;

	@Shadow
	private void postChange(final IAEItemStack diff, final BaseActionSource src) {
	};

	@Shadow
	private void postCraftingStatusChange(final IAEItemStack diff) {
	};

	@Shadow
	private MECraftingInventory inventory;
	private static final IAEItemStack[] EMPTY= new IAEItemStack[0];
	@Inject(at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "markDirty"), method = "executeCrafting")
	public void b(IEnergyGrid eg, CraftingGridCache cc, CallbackInfo ci) {

		if (isMulti) {
			int used = 0;
			
			LinkedList<Object> is=new LinkedList<>();
			for(int i=0;i<inv.getSizeInventory();i++){
				if(inv.getStackInSlot(i)!=null){
					is.addLast(inv.getStackInSlot(i));
				}
			}
			ListIterator<Object> itr = is.listIterator();
			while(itr.hasNext()){
				Object o=itr.next();
				if(o==null){itr.remove();}
				if(((ItemStack)o).getItem() instanceof ItemFluidPacket){
					o=(ItemFluidDrop.newStack(ItemFluidPacket.getFluidStack((ItemStack) o)));
				}
				itr.set(AEItemStack.create((ItemStack) o));
			}
			
			
			IAEItemStack[] input = is.toArray(EMPTY);
					
			int[] nums = new int[input.length];
			for (int x = 0; x < input.length; x++) {
				IAEItemStack tmp = input[x].copy().setStackSize(Integer.MAX_VALUE);
				final IAEItemStack ais = this.inventory.extractItems(tmp, Actionable.MODULATE, this.machineSrc);
				if (ais != null)
					nums[x] = (int) ais.getStackSize();
			}
			try {

				int best = Integer.MAX_VALUE;
				boolean any = false;
				for (int x = 0; x < input.length; x++) {
					if (input[x].getStackSize() > 0) {
						int num = (int) (nums[x] / input[x].getStackSize());
						if (num < best)
							best = num;
						any = true;
					}
				}
				if (any == false) {
					return;
				}

				long num = MixinCallback.getter.apply(e.getValue());

				int maxtry = Math.min(Math.min(
						(int) (num > (Integer.MAX_VALUE - 1) ? (Integer.MAX_VALUE - 1) : num) - 1, remainingOperations),

						best);

				if (maxtry <= 0) {
					return;
				}
				used = medium.pushPatternMulti(detail, inv, maxtry);

				MixinCallback.setter.accept(e.getValue(), num - used);

				if (used > 0) {
					for (IAEItemStack out : detail.getCondensedOutputs()) {
						out = out.copy().setStackSize(used * out.getStackSize());
						this.postChange(out, this.machineSrc);
						this.waitingFor.add(out.copy());
						this.postCraftingStatusChange(out.copy());
					}
				}

			} finally {
				//return all unused
				for (int x = 0; x < input.length; x++) {
					this.inventory.injectItems(input[x].copy().setStackSize(
							/* all availavle - pushed recipes*per recipe */
							nums[x] - used * input[x].getStackSize()), Actionable.MODULATE, this.machineSrc);

				}
			}

		}

	}

	@Shadow
	int remainingOperations;

	@Inject(at = @At(value = "RETURN"), method = "executeCrafting")
	public void ret(IEnergyGrid eg, CraftingGridCache cc, CallbackInfo ci) {
		detail = null;
		e = null;
		inv = null;
		medium = null;

	}

}
