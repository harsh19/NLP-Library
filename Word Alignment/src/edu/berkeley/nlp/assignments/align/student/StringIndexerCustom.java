package edu.berkeley.nlp.assignments.align.student;

import java.util.HashMap;
import java.util.Map;


public class StringIndexerCustom {
	private Map<String,Integer> indexer;
	private Map<Integer,String> reverseIndeser;
	private int counter ;
	public StringIndexerCustom(){
		indexer = new HashMap<String, Integer>();
		reverseIndeser = new HashMap<Integer,String>();
		counter= 0 ;
	}
	public int addAndGet(String s){
		addToIndex(s);
		return indexer.get(s).intValue();
	}
	public int getIndex(String s){
		if(!indexer.containsKey(s)){
			System.out.println("Word not found!! "+s);
			return -1;
		}
		return indexer.get(s).intValue();
	}
	public void addToIndex(String s){
		if(!indexer.containsKey(s)){
			indexer.put(s, counter);
			reverseIndeser.put(counter, s);
			//System.out.println("indexing..."+s+"->"+counter);
			counter++;
		}
	}
	public int[] getIndices(String s[]){
		int ret[] = new int[s.length];
		int i=0;
		for(String word:s ){
			ret[i] = getIndex(word);
			i++;
		}
		return ret;
	}
	public int[] indexAndGetIndices(String s[]){
		for(String word:s ){
			addToIndex(word);
		}
		return getIndices(s);
	}
	public String[] getWordsFromIndices(int indices[]){
		String ret[] = new String[indices.length];
		int i=0;
		for(int idx:indices){
			ret[i] = reverseIndeser.get(idx);
			i++;
		}
		return ret;
	}
	public int getSize(){
		return counter;
	}
}
