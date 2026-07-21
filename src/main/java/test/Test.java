package test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.mpy.runtime.PyObj;
import net.mpy.vm.Builtins;
import net.mpy.vm.Compiler;
import net.mpy.vm.HostFunction;
import net.mpy.vm.Vm;
import reobf.mpy4oc.main.MyMod;

public class Test {
public static void main(String args[]) throws Exception {
	MyMod.MPY_CROSS="D:\\MPY4OC\\run\\client\\mpy_binary\\mpy-cross-win-x64.exe";
	//System.out.println("aa");
	byte b[]=MyMod.compile(
			"""
a=199
exec("print(a)")
""");


	Vm vm = new Vm(null, null);
	vm.setCompiler(new Compiler() {
		
		@Override
		public byte[] compile(String source, String filename, String mode) throws Exception {
			return MyMod.compile(source);
		}
	});
	vm.startCell(null, b);
	while (!vm.isFinished()) {
	    int[] budget = {2000};
	    vm.step(budget); 
	   
	    System.out.println(budget[0]);
	    // 每 tick 给预算
	    if (vm.isYielding()) {System.out.println("a");}//break;             // cell 里 yieldJava() 真正挂起到宿主
	}
	
	

}


}
