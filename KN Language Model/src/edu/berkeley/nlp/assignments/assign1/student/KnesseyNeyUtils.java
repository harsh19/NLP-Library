package edu.berkeley.nlp.assignments.assign1.student;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.print.DocFlavor.BYTE_ARRAY;

import junit.framework.Assert;

public class KnesseyNeyUtils {

	public static int getSimpleEncoding(int a, int b, short digs){
		return (a<<digs)+b;
	}
	
	public static long getSimpleEncoding(long a, long b, short digs){
		long res = (a<<digs)+b;
		if(res<0){
			System.out.println(" ---- "+a+"  "+b);
		}
		return (a<<digs)+b;
	}
	public static long getSimpleEncoding(long a, long b, long c, short digs){
		return (getSimpleEncoding(a,b,digs)<<digs)+c;
	}
	
	public static class openAddressHashing {
		private int tablesize;
		private int values[];
		private long keys[];
		private int count = 0;
		
		private int ranksToCounts[];
		private short ranks[];
		private boolean shiftedToRank;
		private Map<Integer,Short> mapperCountToRanks;
		
		public long[] getKeys(){
			return keys;
		}
		
		private void populateRanksToCounts(){
			mapperCountToRanks = new HashMap<Integer,Short>();
			Set<Integer> allVals = new HashSet<Integer>();
			for(int val:values){
				allVals.add(val);
			}
			
			ranksToCounts = new int[allVals.size()]; int i=0;
			Iterator<Integer> itr = allVals.iterator();
			while(itr.hasNext()){
				ranksToCounts[i] = itr.next(); i++;
			}
			Arrays.sort(ranksToCounts);
			short j=0;
			for(int val:ranksToCounts){
				mapperCountToRanks.put(val, j); j++;
			}

		}
		
		public void shiftToRanks(){
			populateRanksToCounts();
			//ranks = new short[tablesize];
			for(int i=0;i<tablesize;i++){
				ranks[i] = mapperCountToRanks.get(values[i]);
			}
			values = null;
			shiftedToRank = true;
			System.gc();
			System.gc();
			 
		}
		
		public openAddressHashing(int tableSize){
			this.tablesize = tableSize;
			values = new int[tablesize];
			keys = new long[tablesize];
			for(int i=0;i<tablesize;i++){
				keys[i]=-1;
			}
			count = 0;
			shiftedToRank = false;
			ranks = new short[tablesize];
		}
		
		public int getSize(){
			return tablesize;
		}
		
		public int getCount(){
			return count;
		}
		
		public int retrieveAtIndex(int i){
			if(shiftedToRank) return ranksToCounts[ranks[i]];
			return values[i];
		}
		
		private boolean insertAtPosition( long pos1, long k, int v){
			count++;
			int pos = (int) pos1;
			for(int i=pos;i<tablesize;i++){
				if(keys[i]==-1){
					keys[i]=k;
					values[i]=v;
					return true;
				}
			}
			for(int i=0;i<pos;i++){
				if(keys[i]==-1){
					keys[i]=k;
					values[i]=v;
					return true;
				}
			}
			return false;
		}
		
		public void insert(long key, int value){
			long hashval = getHashValue(key, tablesize);
			if(!insertAtPosition(hashval, key, value)){
				System.out.println("Error");
			}
		}
		
		
		public boolean increment(long key, int deltaValue){
			long hashval = getHashValue(key, tablesize);
			if( (int)(hashval) < 0 ) {
				System.out.println(key+"  "+hashval);
			}
			for(int i=(int)hashval;i<tablesize;i++){
				if(keys[i]==key){
					values[i]+=deltaValue;
					return true;
				}
				else if(keys[i]==-1){
					if(!insertAtPosition(hashval, key, deltaValue)){
						System.out.println("Error");
						return false;
					}
					return true;
				}
			}
			for(int i=0;i<hashval;i++){
				if(keys[i]==key){
					values[i]+=deltaValue;
					return true;
				}
				else if(keys[i]==-1){
					if(!insertAtPosition(hashval, key, deltaValue)){
						System.out.println("Error");
						return false;
					}
					return true;
				}
			}
			return false;
		}
		
		public int retrieve(long key){
			long hashval = getHashValue(key, tablesize);
			for(int i=(int)hashval;i<tablesize;i++){
				if(keys[i]==key){
					if(shiftedToRank) return ranksToCounts[ranks[i]];
					return values[i];
				}
				else if(keys[i]==-1){
					return -1;
				}
			}
			for(int i=0;i<hashval;i++){
				if(keys[i]==key){
					if(shiftedToRank) return ranksToCounts[ranks[i]];
					return values[i];
				}
				else if(keys[i]==-1){
					return -1;
				}
			}
			return -1;
		}
		
		public void reset(){
			for(int i=0;i<tablesize; i++){
				keys[i]=-1; values[i] = 0;
			}
			count = 0;
		}
	}
	
	
	public static class openAddressHashingBigram {
		int tablesize;
		public long keys[];
		int bigramCount[];
		int bigramFertilityCount[];
		float bigramLambda[];
		private int count = 0;
		
		int ranksToCounts[];
		short ranksForCounts[];
		short ranksForFertilityCounts[];
		boolean shiftedToRank;
		Map<Integer,Short> mapperCountToRanks;

		public openAddressHashingBigram(int tableSize){
			this.tablesize = tableSize;
			bigramCount = new int[tablesize];
			//bigramFertilityCount = new int[tablesize];
			//bigramLambda = new double[tablesize];
			keys = new long[tablesize];
			for(int i=0;i<tablesize;i++){
				keys[i]=-1;
			}
			count = 0;
		}
		
		public int getSize(){
			return tablesize;
		}
		
		public int getCount(){
			return count;
		}
		
		// ----------------------------------  SHIFTING TO RANKS INSTEAD OF COUNTS
		
		private void populateRanksToCounts(){
			mapperCountToRanks = new HashMap<Integer,Short>();
			Set<Integer> allVals = new HashSet<Integer>();
			for(int val:bigramCount){
				allVals.add(val);
			}
			for(int val:bigramFertilityCount){
				allVals.add(val);
			}
			
			ranksToCounts = new int[allVals.size()]; 
			int i=0;
			Iterator<Integer> itr = allVals.iterator();
			while(itr.hasNext()){
				ranksToCounts[i] = itr.next(); i++;
			}
			Arrays.sort(ranksToCounts);
			short j=0;
			for(int val:ranksToCounts){
				mapperCountToRanks.put(val, j); j++;
			}
			System.out.println("Number of unique counts in bigram counts and fertility counts tables are "+j);

		}
		
		public void shiftToRanks(){
			populateRanksToCounts();
			//ranks = new short[tablesize];
			for(int i=0;i<tablesize;i++){
				ranksForCounts[i] = mapperCountToRanks.get(bigramCount[i]);
				ranksForFertilityCounts[i] = mapperCountToRanks.get(bigramFertilityCount[i]);
			}
			bigramCount = null;
			bigramFertilityCount = null;
			shiftedToRank = true;
			System.gc();
			System.gc();
			 
		}
		// --------------------------------
		
		private boolean insertAtPosition( long pos1, long k, int v, float doublev, int type){
			count++;
			int pos = (int) pos1;
			if(type==1){
				for(int i=pos;i<tablesize;i++){
					if(keys[i]==-1){
						keys[i]=k;
						bigramCount[i]=v;
						return true;
					}
				}
				for(int i=0;i<pos;i++){
					if(keys[i]==-1){
						keys[i]=k;
						bigramCount[i]=v;
						return true;
					}
				}
				return false;
			}
			else if(type==3){
				for(int i=pos;i<tablesize;i++){
					if(keys[i]==-1){
						keys[i]=k;
						bigramLambda[i]=doublev;
						return true;
					}
				}
				for(int i=0;i<pos;i++){
					if(keys[i]==-1){
						keys[i]=k;
						bigramLambda[i]=doublev;
						return true;
					}
				}
				return false;
				
			}
			else if(type==2){
				for(int i=pos;i<tablesize;i++){
					if(keys[i]==-1){
						keys[i]=k;
						bigramFertilityCount[i]=v;
						return true;
					}
				}
				for(int i=0;i<pos;i++){
					if(keys[i]==-1){
						keys[i]=k;
						bigramFertilityCount[i]=v;
						return true;
					}
				}
				return false;
			}
			return false;
		}
		
		public void insertCount(long key, int value){
			long hashval = getHashValue(key, tablesize);
			if(!insertAtPosition(hashval, key, value,0.0f, 1)){
				System.out.println("Error");
			}
		}
		
		public void insertFertilityCount(long key, int value){
			long hashval = getHashValue(key, tablesize);
			if(!insertAtPosition(hashval, key, value,0.0f, 2)){
				System.out.println("Error");
			}
		}
		
		public void insertLambda(long key, float value){
			long hashval = getHashValue(key, tablesize);
			if(!insertAtPosition(hashval, key, 0,value,3)){
				System.out.println("Error");
			}
		}
		public void insertLambdaAtIndex(int i, float value){
			bigramLambda[i] = value;
		}
		
		
		public boolean increment(long key, int deltaValue, int type){
			long hashval = getHashValue(key, tablesize);
			if( (int)(hashval) < 0 ) {
				System.out.println(key+"  "+hashval);
			}
			if(type==1){
				//System.out.println(" HASHVAL, tableSize = "+(int)hashval + " "+tablesize + " "+count);
				for(int i=(int)hashval;i<tablesize;i++){
					if(keys[i]==key){
						bigramCount[i]+=deltaValue;
						return true;
					}
					else if(keys[i]==-1){
						if(!insertAtPosition(hashval, key, deltaValue,0.0f,type)){
							System.out.println("Error");
							return false;
						}
						return true;
					}
				}
				//System.out.println(" **HASHVAL = "+(int)hashval);
				for(int i=0;i<hashval;i++){
					if(keys[i]==key){
						bigramCount[i]+=deltaValue;
						return true;
					}
					else if(keys[i]==-1){
						if(!insertAtPosition(hashval, key, deltaValue,0.0f,type)){
							System.out.println("Error");
							return false;
						}
						return true;
					}
				}
				//System.out.println(" ***HASHVAL = "+(int)hashval);
			}
			else{ // == 2
				for(int i=(int)hashval;i<tablesize;i++){
					if(keys[i]==key){
						bigramFertilityCount[i]+=deltaValue;
						return true;
					}
					else if(keys[i]==-1){
						if(!insertAtPosition(hashval, key, deltaValue,0.0f,type)){
							System.out.println("Error");
							return false;
						}
						return true;
					}
				}
				for(int i=0;i<hashval;i++){
					if(keys[i]==key){
						bigramFertilityCount[i]+=deltaValue;
						return true;
					}
					else if(keys[i]==-1){
						if(!insertAtPosition(hashval, key, deltaValue,0.0f,type)){
							System.out.println("Error");
							return false;
						}
						return true;
					}
				}
			}
			return false;
		}
		public boolean incrementCount(long key, int deltaValue){
			return increment(key, deltaValue, 1);
		}
		public boolean incrementFertilityCount(long key, int deltaValue){
			return increment(key, deltaValue, 2);
		}
		public boolean incrementLambda(long key, float deltaValue){
			long hashval = getHashValue(key, tablesize);
			int type = 3;
			//System.out.println(" HASHVAL, tableSize = "+(int)hashval + " "+tablesize + " "+count);
			for(int i=(int)hashval;i<tablesize;i++){
				if(keys[i]==key){
					bigramLambda[i]+=deltaValue;
					return true;
				}
				else if(keys[i]==-1){
					if(!insertAtPosition(hashval, key, 0,deltaValue,type)){
						System.out.println("Error");
						return false;
					}
					return true;
				}
			}
			//System.out.println(" **HASHVAL = "+(int)hashval);
			for(int i=0;i<hashval;i++){
				if(keys[i]==key){
					bigramLambda[i]+=deltaValue;
					return true;
				}
				else if(keys[i]==-1){
					if(!insertAtPosition(hashval, key, 0, deltaValue,type)){
						System.out.println("Error");
						return false;
					}
					return true;
				}
			}
			return false;
		}
		
		public int retrieveIndex(long key){
			long hashval = getHashValue(key, tablesize);
			for(int i=(int)hashval;i<tablesize;i++){
				if(keys[i]==key){
					return i;
				}
				else if(keys[i]==-1){
					return -1;
				}
			}
			for(int i=0;i<hashval;i++){
				if(keys[i]==key){
					return i;
				}
				else if(keys[i]==-1){
					return -1;
				}
			}
			return -1;
		}
		public int retrieveCount(long key){
			long hashval = getHashValue(key, tablesize);
			for(int i=(int)hashval;i<tablesize;i++){
				if(keys[i]==key){
					return bigramCount[i];
				}
				else if(keys[i]==-1){
					return -1;
				}
			}
			for(int i=0;i<hashval;i++){
				if(keys[i]==key){
					return bigramCount[i];
				}
				else if(keys[i]==-1){
					return -1;
				}
			}
			return -1;
		}
		public int retrieveCountByIndex(int idx){
			if(shiftedToRank){
				return ranksToCounts[ranksForCounts[idx]];
			}
			return bigramCount[(int)idx];
		}
		public int retrieveFertilityCount(long key){
			long hashval = getHashValue(key, tablesize);
			for(int i=(int)hashval;i<tablesize;i++){
				if(keys[i]==key){
					if(shiftedToRank){
						return ranksToCounts[ranksForFertilityCounts[i]];
					}
					return bigramFertilityCount[i];
				}
				else if(keys[i]==-1){
					return -1;
				}
			}
			for(int i=0;i<hashval;i++){
				if(keys[i]==key){
					if(shiftedToRank){
						return ranksToCounts[ranksForFertilityCounts[i]];
					}
					return bigramFertilityCount[i];
				}
				else if(keys[i]==-1){
					return -1;
				}
			}
			return -1;
		}
		public int retrieveFertilityCountByIndex(int idx){
			if(shiftedToRank){
				return ranksToCounts[ranksForFertilityCounts[idx]];
			}
			return bigramFertilityCount[idx];
		}
		public double retrieveLambda(long key){
			long hashval = getHashValue(key, tablesize);
			for(int i=(int)hashval;i<tablesize;i++){
				if(keys[i]==key){
					return bigramLambda[i];
				}
				else if(keys[i]==-1){
					return -1;
				}
			}
			for(int i=0;i<hashval;i++){
				if(keys[i]==key){
					return bigramLambda[i];
				}
				else if(keys[i]==-1){
					return -1;
				}
			}
			return -1;
		}
		public float retrieveLambdaByIndex(long idx){
			return bigramLambda[(int)idx];
		}
	}
	
	
	public static long getHashValue(long key, long limit){ // 0 to (limit-1)
		//return ( (( ((key<<30)>>>30)+(key>>>30)) )*  3875239)%limit;
		return ( (( ((key<<30)>>>30)+(key>>>30)) )* 99929)%limit; //( (key & ((1<<40)-1) ) ^ (key & ((1<<60)-(1<<20)) ) )%limit; //(29*key+1333)%limit;
		//return (Math.abs(key))%limit; //99929  
	}
	
	
	
	public static long getBits(long x, short from, short to){
		long tmp =1L<<(to+1);
		tmp-=1;
		if(from>0)tmp = tmp-( (1L<<from)-1  );
		x = x & tmp;
		//now reduce number considering bits 0 - from-1 are removed
		if(from>0){x =  x>>(from);}
		return x;
	}
	
	
	public static class openAddressHashingCache {
		private int tablesize;
		private float values[];
		private long keys[];
		
		public openAddressHashingCache(int tableSize){
			this.tablesize = tableSize;
			values = new float[tablesize];
			keys = new long[tablesize];
		}
		
		public int getSize(){
			return tablesize;
		}
		
		public double retrieveAtIndex(int i){
			return values[i];
		}
		
		
		public void insert(long key, float value){
			int pos = (int) getHashValue(key, tablesize);
			keys[pos]=key;
			values[pos]=value;
		}
		
		
		public double retrieve(long key){
			int i = (int)getHashValue(key, tablesize);
			return (keys[i]==key) ? values[i]:999.0;
		}
	}
	
	public static long sum(int[] a) {
		if (a == null) { return 0; }
		long result = 0;
		for (int i = 0; i < a.length; i++) {
			result += a[i];
		}
		return result;
	}
	
	public static class binarySearchCounter {
		private int tablesize;
		private int values[];
		private long keys[];
		//public long keysSearch[];
		private int count = 0;
		private openAddressHashing tempSpace = null;
		private final int setoffValue = 42000000; //41628000;
		private int mergeThreshold;
		
		int ranksToCounts[];
		short ranks[];
		boolean shiftedToRank;
		Map<Integer,Short> mapperCountToRanks;
		
		public binarySearchCounter(int size){
			this.tablesize = setoffValue;
			values = new int[tablesize];
			keys = new long[tablesize];
			for(int i=0; i<tablesize; i++){
				keys[i] = -1;
			}
			count = 0;
			shiftedToRank = false;
			ranks = new short[tablesize];
			tempSpace = new openAddressHashing(size-setoffValue);
			mergeThreshold = (int)(0.73*(size-setoffValue));
		}
		
		private void populateRanksToCounts(){
			mapperCountToRanks = new HashMap<Integer,Short>();
			Set<Integer> allVals = new HashSet<Integer>();
			for(int i=0; i<count; i++){
				allVals.add(values[i]);
			}
			
			ranksToCounts = new int[allVals.size()]; int i=0;
			Iterator<Integer> itr = allVals.iterator();
			while(itr.hasNext()){
				ranksToCounts[i] = itr.next(); i++;
			}
			Arrays.sort(ranksToCounts);
			short j=0;
			for(int val:ranksToCounts){
				mapperCountToRanks.put(val, j); j++;
			}

		}
		
		private void populateRanks(){
			for(int i=0;i<count;i++){
				ranks[i] = mapperCountToRanks.get(values[i]);
			}
			values = null;
		}
		
		public void shiftToRanks(){
			if(tempSpace.count>0){
				merge();
				tempSpace.reset();
			}
			tempSpace=null;
			System.gc();
			System.gc();
			populateRanksToCounts();
			//ranks = new short[tablesize];
			populateRanks();
			System.gc();
			shiftedToRank = true;
			System.gc();
			System.gc();
			System.out.println("TOTAL TRIGRAM COUNT AFTER RANK CONVERSION IS "+count);
		}
		
		public int getSize(){
			return tablesize;
		}
		
		public int getCount(){
			return count;
		}
		
		public int retrieveAtIndex(int i){
			//System.out.println(" retrieving value at i = "+i);
			if(shiftedToRank) return ranksToCounts[ranks[i]];
			return values[i];
		}
		
		public void insert(long key, int value){
			tempSpace.insert(key, value);
			if(tempSpace.getCount()>mergeThreshold){
				merge();
				tempSpace.reset();
			}
		}
		
		public void forceMerge(){
				merge();
				tempSpace.reset();
		}
		
		
		public boolean increment(long key, int deltaValue){
			boolean res = tempSpace.increment(key, deltaValue);
			if(tempSpace.getCount()>mergeThreshold){
				merge();
				tempSpace.reset();
			}
			return res;
		}
		
		private void merge(){
			int idx; long key;
			long tmp[] = tempSpace.keys;
			int tmp_values[] = tempSpace.values;
			int lim = tempSpace.getSize();
			int increasedCounter = count;
			for(int i=0; i<lim; i++){
				key = tmp[i];
				if(key==-1){
					continue;
				}
				idx = getIndex(key);
				if(idx==-1){
					if(KnesserNeyConstants.IGNORE_SMALL_COUNTS) {
						if (tmp_values[i]<=1) {
							continue;
						}
					}
					keys[increasedCounter] = key;
					values[increasedCounter] = tmp_values[i];
					increasedCounter++;
				}
				else{
					values[idx]+=tmp_values[i];
				}
			}
			count = increasedCounter;
			quickSort(0,count-1);
		}
		
		public int getIndex(long key){
			int start = 0, mid;
			int end = count-1;
			while(start<=end){
				mid = (start+end)/2;
				if(keys[mid]==key){
					if(mid<0){
						System.out.println(" -------------------->>>>> ");
					}
					return mid;
				}
				else if(keys[mid]<key){
					start = mid+1;
				}
				else{
					end = mid-1;
				}
			}
			return -1;
		}
		
		public int retrieve(long key){
			 int idx = getIndex(key);
			 if(idx<0)return -1;
			 return ranksToCounts[ranks[idx]];
		}
		public long[] getKeys(){
			return keys;
		}
		
		
		 private void quickSort(int low, int high) {
		        int i = low,j = high;
		        long pivot = keys[low+(high-low)/2];
		        while (i <= j) {
		            while (keys[i] < pivot) { i++; }
		            while (keys[j] > pivot) { j--; }
		            if (i <= j) {
		                switchNums(i, j);
		                i++;
		                j--;
		            }
		        }
		        if (low < j)
		            quickSort(low, j);
		        if (i < high)
		            quickSort(i, high);
		    }
		   private void switchNums(int i, int j) {
		        long temp = keys[i];
		        keys[i] = keys[j];
		        keys[j] = temp;
		        int tmp = values[i];
		        values[i] = values[j];
		        values[j] = tmp;
		        
		    }
		     
	}
	
	
	public static class binarySearchCounterBigram {
		int tablesize;
		public long keys[];
		float bigramLambda[];
		private int count = 0;
		int ranksToCounts[];
		short ranksForCounts[];
		short ranksForFertilityCounts[];

		public binarySearchCounterBigram(openAddressHashingBigram obj){
			this.tablesize = obj.getCount();
			//tablesize = 8000000;
			
			keys = new long[tablesize];
			ranksForCounts = new short[tablesize];
			ranksForFertilityCounts = new short[tablesize];
			bigramLambda = new float[tablesize];
			int lim = obj.getSize(); 
			count = 0;
			for(int i=0;i<lim;i++){
				if(obj.keys[i]<0){
					continue;
				}
				keys[count]=obj.keys[i];
				keys[count] = obj.keys[i];
				ranksForCounts[count] = obj.ranksForCounts[i];
				ranksForFertilityCounts[count] = obj.ranksForFertilityCounts[i];
				bigramLambda[count] = obj.bigramLambda[i];
				count++;
			}
			lim = obj.ranksToCounts.length;
			ranksToCounts = new int[lim];
			for(int i=0; i<lim; i++){
				ranksToCounts[i] = obj.ranksToCounts[i];
			}
			quickSort(0,count-1);
			
		}
		

		
		 private void quickSort(int low, int high) {
		        int i = low,j = high;
		        long pivot = keys[low+(high-low)/2];
		        while (i <= j) {
		            while (keys[i] < pivot) { i++; }
		            while (keys[j] > pivot) { j--; }
		            if (i <= j) {
		                switchNums(i, j);
		                i++;
		                j--;
		            }
		        }
		        if (low < j)
		            quickSort(low, j);
		        if (i < high)
		            quickSort(i, high);
		    }
		   private void switchNums(int i, int j) {
		        long temp = keys[i];
		        keys[i] = keys[j];
		        keys[j] = temp;
		        short tmp = ranksForCounts[i];
		        ranksForCounts[i] = ranksForCounts[j];
		        ranksForCounts[j] = tmp;
		        tmp = ranksForFertilityCounts[i];
		        ranksForFertilityCounts[i] = ranksForFertilityCounts[j];
		        ranksForFertilityCounts[j] = tmp;
		        float tmpf = bigramLambda[i];
		        bigramLambda[i] = bigramLambda[j];
		        bigramLambda[j] = tmpf;
		        
		    }
		
		public int getSize(){
			return tablesize;
		}
		
		public int getCount(){
			return count;
		}
		
		public int getIndex(long key){
			int start = 0, mid;
			int end = count-1;
			while(start<=end){
				mid = (start+end)/2;
				if(keys[mid]==key){
					if(mid<0){
						System.out.println(" -------------------->>>>> ");
					}
					return mid;
				}
				else if(keys[mid]<key){
					start = mid+1;
				}
				else{
					end = mid-1;
				}
			}
			return -1;
		}
		
		
		public int retrieveIndex(long key){
			return getIndex(key);
		}
		public int retrieveCount(long key){
			int idx = getIndex(key);
			if(idx<0)return -1;
			return ranksToCounts[ranksForCounts[idx]];
		}
		public int retrieveCountByIndex(int idx){
			return ranksToCounts[ranksForCounts[idx]];
		}
		public int retrieveFertilityCount(long key){
			int idx = getIndex(key);
			if(idx<0)return -1;
			return ranksToCounts[ranksForFertilityCounts[idx]];
		}
		public int retrieveFertilityCountByIndex(int idx){
			return ranksToCounts[ranksForFertilityCounts[idx]];
		}

		public double retrieveLambda(long key){
			int idx = getIndex(key);
			if(idx<0)return -1;
			return bigramLambda[idx];
		}
		public float retrieveLambdaByIndex(int idx){
			return bigramLambda[idx];
		}
	}

}



