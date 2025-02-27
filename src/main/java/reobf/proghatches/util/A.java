package reobf.proghatches.util;

import java.nio.ByteBuffer;

import sun.nio.ch.DirectBuffer;

public class A {
public static void main(String[] args){
	ByteBuffer b = ByteBuffer.allocateDirect(10);
	sun.nio.ch.DirectBuffer a = (DirectBuffer) b;
	a.cleaner().clean();
	b.put((byte) 12);
	b.flip();
	System.out.println(b.get());
	
	
	
	
}
}
