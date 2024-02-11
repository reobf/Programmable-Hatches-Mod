package reobf.proghatches.main.mixin;

import java.util.Collection;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.modify.BeforeLoadLocal;
import org.spongepowered.asm.mixin.injection.modify.ModifyVariableInjector;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;

public class StoreInjectionPoint extends BeforeLoadLocal{
public StoreInjectionPoint(InjectionPointData d) {
	
super(d,Opcodes.ISTORE,true);

}
	@Override
	public boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes) {
	boolean[] b=new boolean[1];
		insns.forEach(s->{
			
			if(s.getOpcode()==Opcodes.ASTORE
					
					
					
					){
				//System.out.println(((VarInsnNode)s).);
				b[0]=true;
				nodes.add(s);
				
			}
			
		
		});
		
		return b[0];
	}

}
