package reobf.proghatches.oc;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.common.component.Screen;
import li.cil.oc.common.component.traits.VideoRamDevice;
import li.cil.oc.server.component.FileSystem;
import li.cil.oc.server.component.GraphicsCard;
import li.cil.oc.server.machine.ArgumentsImpl;

@Architecture.Name("?")
@Architecture.NoMemoryRequirements
public class Arch implements Architecture {

    private li.cil.oc.server.machine.Machine machine;

    public Arch(Machine machine) {
        this.machine = (li.cil.oc.server.machine.Machine) machine;
    }

    @Override
    public boolean isInitialized() {

        return true;
    }

    @Override
    public boolean recomputeMemory(Iterable<ItemStack> components) {

        return true;
    }

    @Override
    public boolean initialize() {

        return true;
    }

    @Override
    public void close() {

    }


    
	@Override
    public void runSynchronized() {
  
		System.out.println(MinecraftServer.getServer().getTickCounter());
		/* 	machine.components().forEach((a,b)->{
    		
    		System.out.println(a+" "+b);
    	});;*/
    	
    	List<li.cil.oc.common.component.Screen> scs=new ArrayList<>();
    List<li.cil.oc.server.component.FileSystem> fss=new ArrayList<>();
    List<GraphicsCard> gcs=new ArrayList<>();
        machine.node()
            .network()
            .nodes()
            .forEach(s -> {

                if (s.host() instanceof li.cil.oc.common.component.Screen) {

                    var  sc = (Screen) s.host();
                   scs.add(sc);
                    // sc.set(1, 1, "hello", false);

                } ;
                if (s.host() instanceof GraphicsCard g) {
               
                	gcs.add(g);
                	
                }
                if (s.host() instanceof FileSystem i) {
                	fss.add(i);
               // 	li.cil.oc.api.network.Environment  e=i;
              /*  	try {
						int handle=i.fileSystem().open("/init.wasm", li.cil.oc.api.fs.Mode.Read);
						var h=i.fileSystem().getHandle(handle);
						byte[] get;
						h.read(get=new byte[(int) h.length()]);
						System.out.println(new String(get));
						h.close();
                	} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

                } ;     */          
                }
                }

            
                );
        String gets=null;
        String addr=null;
        for(var fs:fss) {

              	try {
						int handle=fs.fileSystem().open("/init.wasm", li.cil.oc.api.fs.Mode.Read);
						var h=fs.fileSystem().getHandle(handle);
						byte[] get;
						h.read(get=new byte[(int) h.length()]);
						gets=new String(get);
						h.close();
						addr=fs.node().address();
						break;
              	} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
        }
        GraphicsCard firstcard=gcs.size()>0?gcs.get(0):null;
        Screen firstscreen=gcs.size()>0?scs.get(0):null;
        if(firstcard!=null&&firstscreen!=null) {
        	machine.invoke(firstcard.node().address(), "bind", new Object[] {firstscreen.node().address()});
       // 	firstcard.setDepth(machine, null);
         Object[] get = machine.invoke(firstcard.node().address(), "getResolution", new Object[] {});
     	machine.invoke(firstcard.node().address(), "fill", new Object[] {1, 1, get[0], get[1], " "});
       
       // 	machine.invoke(firstcard.node().address(), "set", new Object[] {1,1,"he"});
        /*	firstcard.bind(machine, new ArgumentsImpl((Seq<Object>)JavaConverters.asScalaBufferConverter(
         		    Arrays.<Object>asList(firstscreen.node().address())
         			).asScala().seq()));
        	firstcard.*/
     	
     	
     	if(gets==null)
		{
     		machine.invoke(firstcard.node().address(), "set", new Object[] {1,1,"no bootable devices"});
     		machine.invoke(firstcard.node().address(), "set", new Object[] {1,2, "no /init.wasm found"});
		}else {
 		machine.invoke(firstcard.node().address(), "set", new Object[] {1,1,"booting from FileSystem: "+addr});
 		machine.invoke(firstcard.node().address(), "set", new Object[] {1,2, gets});
	
	   }
     	
        	
        }
        
        
        
     /*	g.bind(machine, new ArgumentsImpl((Seq)JavaConverters.asScalaBufferConverter(
     		    Arrays.asList("arg1")
     			).asScala().seq()));
        */
        
      /*  for(Screen s:scs) {
       if(gets==null)
    		{
    	   
    	   s.set(1, 1, "no bootable devices", false);
       s.set(1, 2, "no /init.wasm found", false);
       }
    	   else {
    		   s.set(1, 1, "booting from FileSystem: "+addr, false);
        	s.set(1, 2, gets, false);
    	   }
        }
        
        */
        
        
    }

    @Override
    public ExecutionResult runThreaded(boolean isSynchronizedReturn) {

        return new li.cil.oc.api.machine.ExecutionResult.SynchronizedCall();
    }

    @Override
    public void onSignal() {

    }

    @Override
    public void onConnect() {

    }

    @Override
    public void load(NBTTagCompound nbt) {
int a=nbt.getInteger("aa");

if(a==123)System.exit(0);
    }

    @Override
    public void save(NBTTagCompound nbt) {
nbt.setInteger("aa", 123);
    }

}
