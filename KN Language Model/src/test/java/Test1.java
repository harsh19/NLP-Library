package test.java;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import edu.berkeley.nlp.assignments.assign1.student.KnesseyNeyUtils;

public class Test1 {

	@Test
	public void test() {
		assertEquals(9, KnesseyNeyUtils.getSimpleEncoding(2, 1, (short)2));
		long x = 1024*1024;
		x = x*x;
		long a=(long)(x+1),b=1<<20,c=1; short d=20;
		assertEquals(a, KnesseyNeyUtils.getSimpleEncoding(b, c, d));
		
		x = 1024;
		x=(x*x*x);
		a=(long)(x+1);
		b=1<<10;
		c=1;
		d=20;
		assertEquals(a, KnesseyNeyUtils.getSimpleEncoding(b, c, d));
		d=10;
		assertEquals(1+(a-1)*1024, KnesseyNeyUtils.getSimpleEncoding(a-1, 1L, d));
		
		x=(1L<<40);
		x=x<<20;
		assertEquals(1L<<60, x);
	}
	
	@Test
	public void test_OpenAddressHashing() {
		KnesseyNeyUtils.openAddressHashing obj = new KnesseyNeyUtils.openAddressHashing(10);
		obj.insert(5, 10);
		obj.insert(15, 2);
		obj.insert(2, 13);
		assertEquals(10, obj.retrieve(5) );
		assertEquals(2, obj.retrieve(15) );
		assertEquals(13, obj.retrieve(2) );
		for(int i=21;i<=27;i++){
			obj.insert(i, i);
		}
		for(int i=21;i<=27;i++){
			assertEquals(i, obj.retrieve(i) );
		}
	}
	
	
	@Test
	public void test_getHashValue() {
	long a = 11231231231233L;
	long b = 10000;
	assertEquals(1233, KnesseyNeyUtils.getHashValue(a,b) );
	assertEquals(1233, (int) KnesseyNeyUtils.getHashValue(a,b) );
	}
	
	
	@Test
	public void test_getBits() {
		
		long a = ((3L) << 3)+(6L);
		assertEquals(6, KnesseyNeyUtils.getBits(a, (short)0, (short)2) );
		assertEquals(3, KnesseyNeyUtils.getBits(a, (short)3, (short)5) );
		
		a = ((13L) << 20)+(7L);
		assertEquals(13, KnesseyNeyUtils.getBits(a, (short)20, (short)39) );
		assertEquals(7, KnesseyNeyUtils.getBits(a, (short)0, (short)19) );
	}
	
	@Test
	public void test_bigramCounter() {
		
		KnesseyNeyUtils.openAddressHashingBigram obj = new KnesseyNeyUtils.openAddressHashingBigram(135);
		long unigramFertilityCount[] = new long[135];
		
		int arr[];
		arr = new int[]{0,1,2,3,4,5,100};
		short digs = 10;
		for(int i=0; i<arr.length-1;i++){
			obj.incrementCount(KnesseyNeyUtils.getSimpleEncoding(arr[i], arr[i+1], digs ), 1);
		}
		
		arr = new int[]{0,1,2,100};
		for(int i=0; i<arr.length-1;i++){
			obj.incrementCount(KnesseyNeyUtils.getSimpleEncoding(arr[i], arr[i+1], digs ), 1);
		}
		
		arr = new int[]{0,7,5,100};
		for(int i=0; i<arr.length-1;i++){
			obj.incrementCount(KnesseyNeyUtils.getSimpleEncoding(arr[i], arr[i+1], digs ), 1);
		}
		
		for(int i=0;i<obj.getSize();i++){
			long bigram_key = obj.keys[i];
			if(bigram_key==-1){
				continue;
			}
			long w2 = KnesseyNeyUtils.getBits(bigram_key, (short)0, (short)(digs-1));
			unigramFertilityCount[(int)w2]+=1;
		}
		System.out.println(" done with step 2 ");
		
		arr = new int[]{0,1,2,3,4,5,7,100};
		int vals[] = new int[]{0,1,1,1,1,2,1,2};
		for(int i=0;i<arr.length;i++){
			System.out.println(unigramFertilityCount[arr[i]]);
			assertEquals(vals[i], unigramFertilityCount[arr[i]]);
		}
		
	}
	
	
}
