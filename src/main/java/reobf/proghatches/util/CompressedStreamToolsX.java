package reobf.proghatches.util;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class CompressedStreamToolsX {
	  public static void writeCompressed(NBTTagCompound p_74799_0_, OutputStream p_74799_1_) throws IOException
	    {
	        DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(p_74799_1_)));

	        try
	        {
	        	CompressedStreamTools.write(p_74799_0_, dataoutputstream);
	        }
	        finally
	        {
	            dataoutputstream.close();
	        }
	    }
	  
	  
}
