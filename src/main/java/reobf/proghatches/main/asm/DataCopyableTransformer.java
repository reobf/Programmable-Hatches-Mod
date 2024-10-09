package reobf.proghatches.main.asm;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.launchwrapper.IClassTransformer;
import reobf.proghatches.main.asm.MUITransformer.TheNode;
import reobf.proghatches.main.asm.repack.objectwebasm.ClassReader;
import reobf.proghatches.main.asm.repack.objectwebasm.ClassWriter;
import reobf.proghatches.main.asm.repack.objectwebasm.MethodVisitor;
import reobf.proghatches.main.asm.repack.objectwebasm.Opcodes;
import reobf.proghatches.main.asm.repack.objectwebasm.tree.ClassNode;
import reobf.proghatches.main.asm.repack.objectwebasm.tree.MethodNode;
import reobf.proghatches.main.mixin.LateMixinPlugin;

public class DataCopyableTransformer implements IClassTransformer{
boolean done;
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		exit:if ((!done) && name.equals("reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder")) {
			done=true;
			Boolean dataOrbSupport=null;
			try{
				ModContainer gt = Loader.instance().getModList().stream()
				.filter(s->s.getModId().equals("gregtech"))
				.findAny().get();
				String ver=gt.getMetadata().version;
				if(ver.contains("-")){
					ver=ver.substring(0,ver.lastIndexOf('-'));
				}
				
				//"5.09.49.31"
				String[] vers=ver.split("\\.");
				lab:{
				if(Integer.valueOf(vers[0])-5<0)dataOrbSupport=false;
				if(Integer.valueOf(vers[0])-5>0)dataOrbSupport=true;
				if(dataOrbSupport!=null)break lab;
				if(Integer.valueOf(vers[1])-9<0)dataOrbSupport=false;
				if(Integer.valueOf(vers[1])-9>0)dataOrbSupport=true;
				if(dataOrbSupport!=null)break lab;
				if(Integer.valueOf(vers[2])-49<0)dataOrbSupport=false;
				if(Integer.valueOf(vers[2])-49>0)dataOrbSupport=true;
				if(dataOrbSupport!=null)break lab;
				if(Integer.valueOf(vers[3])-31<0)dataOrbSupport=false;
				if(Integer.valueOf(vers[3])-31>=0)dataOrbSupport=true;
				if(dataOrbSupport!=null)break lab;
				}
				
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("Unable to detect gt version... No data orb support!");
					
				}
					;
			
			
			if(dataOrbSupport==null){
				System.out.println("Class is loaded too early! Cannot add data orb support.");
				Thread.dumpStack();break exit;
			}if(!dataOrbSupport){break exit;}
			ClassReader classReader = new ClassReader(basicClass);
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
			ClassNode n=new ClassNode(Opcodes.ASM5){
				
			@Override
			public void visit(int version, int access, String name, String signature, String superName,
					String[] interfaces) {
				
				super.visit(version, access, name, signature, superName, 
						new String[]{"gregtech/api/interfaces/IDataCopyable"}
						);
			}
			};
			
			classReader.accept(n, ClassReader.EXPAND_FRAMES);
			n.accept(classWriter);
			
			
			return classWriter.toByteArray();
		
		}
		
		return basicClass;
	}
	
}
