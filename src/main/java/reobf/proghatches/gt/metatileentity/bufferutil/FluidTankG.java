package reobf.proghatches.gt.metatileentity.bufferutil;

import java.util.ArrayList;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;

import appeng.api.storage.data.IAEFluidStack;

//arr might be empty
public class FluidTankG {

    ArrayList<FluidStack> arr = new ArrayList<FluidStack>();
    public boolean isEmpty(){
    	if(arr.size()==1)return arr.get(0).amount<=0;
    	
    	//return !arr.stream().filter(s->s.amount>0).findFirst().isPresent();
    	int size = arr.size();
    	for(int i=0;i<size;i++){
    		if(arr.get(i).amount>0)return false;
    	}	
    	return true;
    	
    }
    public long getFluidAmount() {
    	if(arr.size()==1)return arr.get(0).amount;
        /*return arr.stream()
            .mapToLong(s -> s.amount)
            .sum();*/
        long sum=0;
        
    	int size = arr.size();
    	for(int i=0;i<size;i++){
    		sum=sum+arr.get(i).amount;
    	}	
    	return sum;
        
        
        
    }

    /**
     * return value is readonly!!!!
     */
    @Nullable
    public FluidStack getFluid() {
    	if(arr.size()==1)return arr.get(0);
        if (arr.size() > 0) {
            FluidStack f = arr.get(0)
                .copy();
            f.amount = (int) Math.min(getFluidAmount(), Integer.MAX_VALUE);
            if (f.amount <= 0) return null;
            return f;
        }
        return null;
    }

    public void setFluid(FluidStack object) {
        arr.clear();
        if (object != null) arr.add(object);

    }

    public NBTBase writeToNBT(NBTTagCompound nbtTagCompound) {
        if (arr.size() > 0) {
            FluidTank tk = new FluidTank(0);
            tk.setFluid(arr.get(0));
            tk.writeToNBT(nbtTagCompound);
        }
        NBTTagList lst = new NBTTagList();
        for (int i = 1; i < arr.size(); i++) {

            NBTTagCompound t = new NBTTagCompound();
            arr.get(i)
                .writeToNBT(t);
            // t.setInteger("ICount", arr.get(i).stackSize);
            lst.appendTag(t);

        }

        nbtTagCompound.setTag("therest", lst);

        return nbtTagCompound;
    }

    public void readFromNBT(NBTTagCompound compoundTag) {
        arr.clear();
        FluidTank tk = new FluidTank(0);
        tk.readFromNBT(compoundTag);
        if (tk.getFluidAmount() > 0) arr.add(tk.getFluid());
        if (compoundTag.hasKey("therest")) {

            NBTTagList lst = (NBTTagList) compoundTag.getTag("therest");
            for (int ix = 0; ix < lst.tagCount(); ix++) {
                NBTTagCompound TAG = lst.getCompoundTagAt(ix);

                FluidStack isX = FluidStack.loadFluidStackFromNBT(TAG);
                if (isX != null) {
                    arr.add(isX);
                }

            }

        }
    }

    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null) {
            return 0;
        }
        if (!doFill) {
            return resource.amount;
        }

        if (arr.size() > 0 && arr.get(0)
            .getFluid() != resource.getFluid()) {
            return 0;
        }
        int todo = resource.amount;
        for (FluidStack is : arr) {
            int cando = Math.min(

                Integer.MAX_VALUE
                    // 64

                    - is.amount,
                todo);
            todo -= cando;
            is.amount += cando;
            if (todo <= 0) return resource.amount;
        }
        FluidStack i = resource.copy();
        i.amount = todo;
        arr.add(i);

        return resource.amount;
    }

    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (getFluidAmount() == 0) {
            return null;
        }
        if(arr.size()==0)return null;
        FluidStack cp = arr.get(0)
            .copy();
        if (!doDrain) {
            cp.amount = (int) Math.min(getFluidAmount(), Integer.MAX_VALUE);
            return cp;
        }
        cp.amount = maxDrain;
        for (FluidStack fs : arr) {
            int todo = Math.min(fs.amount, maxDrain);
            fs.amount -= todo;
            maxDrain -= todo;
            if (maxDrain <= 0) break;
        }

        cp.amount = cp.amount - maxDrain;

        adjust();
        return cp;
    }

    public FluidTankInfo getInfo() {

        return new FluidTankInfo(getFluid(), Integer.MAX_VALUE);
    }

    public FluidStack[] flat() {
        adjust();
        return arr.toArray(new FluidStack[arr.size()]);
    }

    public void adjust() {
        boolean dirty = false;
        for (int i = 0; i < arr.size() - 1; i++) {
            if (arr.get(i).amount < Integer.MAX_VALUE) {
                if (arr.get(i + 1).amount > 0) {
                    int todo = Math.min(Integer.MAX_VALUE - arr.get(i).amount, arr.get(i + 1).amount);
                    arr.get(i).amount += todo;
                    arr.get(i + 1).amount -= todo;
                    dirty = true;

                }
            }

        }

        if (dirty) {
            Iterator<FluidStack> it = arr.iterator();
            while (it.hasNext()) {

                if (it.next().amount <= 0) {
                    it.remove();
                }

            }

        }
    }

    public void fromAE(@Nonnull IAEFluidStack possible, int intmaxs) {
        if (possible == null) {
            arr.clear();
            return;
        }

        long all = possible.getStackSize();
        all = Math.min(all, intmaxs * 1L * Integer.MAX_VALUE);

        long maxs = all / (1L * Integer.MAX_VALUE);
        long remain = all - maxs * Integer.MAX_VALUE;
        for (int i = 0; i < maxs; i++) {
            FluidStack is = possible.getFluidStack();
            is.amount = Integer.MAX_VALUE;
            arr.add(is);
        }
        if (remain > 0) {
            FluidStack is = possible.getFluidStack();
            is.amount = (int) Math.min(remain, Integer.MAX_VALUE);
            arr.add(is);
        }

    }

    public void amountAcc(long l) {
        if (l == 0) {
            return;
        }
        if(arr.size()==0){
        	//this should not happed
        	return;}
        long todo = l;
        for (FluidStack is : arr) {
            long cando = Math.min(

                Integer.MAX_VALUE
                    // 64

                    - is.amount,
                todo);
            todo -= cando;
            is.amount += cando;
            if (todo <= 0) return;
        }
        while (todo > 0) {
            int t = (int) Math.min(Integer.MAX_VALUE, todo);
            todo -= t;
            FluidStack i = arr.get(0)
                .copy();
            i.amount = t;
            arr.add(i);
        }

    }

}
