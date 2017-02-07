package edu.berkeley.nlp.assignments.align.student;

import java.util.HashMap;
import java.util.Map;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.Pair;
import edu.berkeley.nlp.util.StringIndexer;
import edu.berkeley.nlp.util.TIntOpenHashMap;
import edu.berkeley.nlp.util.TIntOpenHashMap.Entry;

public class HeuristicAligner implements WordAligner
{
	StringIndexer englishIndexer;
	StringIndexer frenchIndexer;
	TIntOpenHashMap<Integer> englishCounter;
	TIntOpenHashMap<Integer> frenchCounter;	
	TIntOpenHashMap< Integer > counts;
	//Map<Integer,Double> scores;
	int thresh=34000;
	
	private int getEncoding(int eng, int frn){
		return thresh*eng+frn;
	}
	private Pair<Integer,Integer> getReverseEncoding(int code){
		return new Pair<Integer,Integer>(code/thresh,code%thresh);
	}
	
	public HeuristicAligner(Iterable<SentencePair> trainingData){
		englishIndexer = new StringIndexer();
		frenchIndexer = new StringIndexer();
		englishCounter = new TIntOpenHashMap<Integer>();
		frenchCounter = new TIntOpenHashMap<Integer>();
		counts = new TIntOpenHashMap<Integer>();
		//scores = new HashMap<Integer,Double>();
		int e,f;
		int i=0;
		for(SentencePair sPair:trainingData){
			for(String englishWord:sPair.englishWords){
				e = englishIndexer.addAndGetIndex(englishWord);
				englishCounter.put(e, englishCounter.get(e)+1);
			}
			for(String frenchWord:sPair.frenchWords){
				f = frenchIndexer.addAndGetIndex(frenchWord);
				frenchCounter.put(f, frenchCounter.get(f)+1);
				for(String englishWord:sPair.englishWords){
					e = englishIndexer.addAndGetIndex(englishWord);
					int fe = getEncoding(e, f);
					counts.put(fe, counts.get(fe)+1);
				}
			}
			i++;
			if(i%1000==0){
				System.out.println("iteration = "+i);
			}
		}
		System.out.println("englishIndexer.size() = "+englishIndexer.size());
		System.out.println("frenchIndexer.size() = "+frenchIndexer.size());
		System.out.println("counts.size() = "+counts.size());

		for(Entry entry:counts.entrySet()){
			Integer x = (Integer) entry.key;
			Pair<Integer,Integer> engFrench = getReverseEncoding(x.intValue());
			int count = entry.getValue();
		}
		
	}
	
	private int getEnglishEncoding(String w){
		return englishIndexer.addAndGetIndex(w);
	}
	private int getFrenchEncoding(String w){
		return frenchIndexer.addAndGetIndex(w);
	}
	private int getEnglishCount(int w){
		return englishCounter.get(w);
	}
	private int getFrenchCount(int w){
		return frenchCounter.get(w);
	}
	private int getEnglishCount(String w){
		return getEnglishCount(getEnglishEncoding(w));
	}
	private int getFrenchCount(String w){
		return getFrenchCount( getFrenchEncoding(w) );
	}
	
	private double getScore(String eng, String frn){
		int combinedCount = counts.get(getEncoding(getEnglishEncoding(eng), getFrenchEncoding(frn)));
		return (1.0*combinedCount)/( Math.sqrt(getEnglishCount(eng)) * Math.sqrt(getFrenchCount(frn)) );
                //return (1.0*combinedCount)/( getEnglishCount(eng) * getFrenchCount(frn) );
		//return (2.0*combinedCount)/( getEnglishCount(eng) + getFrenchCount(frn) );
	}
	
	@Override
	public Alignment alignSentencePair(SentencePair sentencePair) {
		Alignment alignment = new Alignment();
		int numFrenchWords = sentencePair.getFrenchWords().size();
		int numEnglishWords = sentencePair.getEnglishWords().size();
		for (int frenchPosition = 0; frenchPosition < numFrenchWords; frenchPosition++) {
			int englishPosition=0; double maxScore=Double.NEGATIVE_INFINITY; 
			double curScore;
			for(int j=0; j<numEnglishWords; j++){
				curScore = getScore(sentencePair.getEnglishWords().get(j), 
						sentencePair.getFrenchWords().get(frenchPosition));
				if(curScore>maxScore){
					maxScore=curScore;
					englishPosition = j;
				}
				
			}
			alignment.addAlignment(englishPosition, frenchPosition, true);
			/*int englishPosition = frenchPosition;
			if (englishPosition < numEnglishWords) 
				alignment.addAlignment(englishPosition, frenchPosition, true);
			*/
		}
		return alignment;
	}
}
