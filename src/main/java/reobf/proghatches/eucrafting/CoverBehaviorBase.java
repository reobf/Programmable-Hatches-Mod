package reobf.proghatches.eucrafting;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;

import gregtech.api.covers.CoverContext;
import gregtech.api.interfaces.ITexture;
import gregtech.common.covers.Cover;
import io.netty.buffer.ByteBuf;

public abstract class CoverBehaviorBase<T extends ISer> extends Cover {

    public CoverBehaviorBase(@NotNull CoverContext context, Class<? extends ISer> iser, ITexture coverFGTexture) {
        super(context, coverFGTexture);
        typeToken = (Class<T>) iser;
        initializeData(/* context.getCoverInitializer() */);

    }

    Class<T> typeToken;

    protected abstract T initializeDataSer();

    public T coverData;

    protected void initializeData() {

        if (coverData == null) coverData = (T) initializeDataSer();
    }

    @Override
    protected void readDataFromNbt(NBTBase nbt) {
        final T ret = initializeDataSer();
        ret.loadDataFromNBT(nbt);
        coverData = ret;
        super.readDataFromNbt(nbt);
    }

    @Override
    public void readDataFromPacket(ByteArrayDataInput byteData) {
        final T ret = initializeDataSer();
        ret.readFromPacket(byteData);
        coverData = ret;
        super.readDataFromPacket(byteData);
    }

    @Override
    public NBTTagCompound saveDataToNbt() {

        return (NBTTagCompound) coverData.saveDataToNBT();
    }

    @Override
    public void writeDataToByteBuf(ByteBuf byteBuf) {
        coverData.writeToByteBuf(byteBuf);
        super.writeDataToByteBuf(byteBuf);
    }

    private static final String NBT_DATA = "d";
}
