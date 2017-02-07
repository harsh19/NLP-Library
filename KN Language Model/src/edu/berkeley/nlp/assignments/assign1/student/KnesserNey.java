package edu.berkeley.nlp.assignments.assign1.student;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.MemoryUsageUtils;

public class KnesserNey implements NgramLanguageModel
{
	
	static final String STOP = NgramLanguageModel.STOP;
	static final String START = NgramLanguageModel.START;
	long total = 0;
	int[] wordCounter = new int[10];
	int unigramFertilityCount[] = new int[500000];
	int unigramStarFertilityCount[] = new int[500000];
	float unigramLambda[] = new float[500000];
	
	
	KnesseyNeyUtils.openAddressHashingBigram bigramCounterTmp = new KnesseyNeyUtils.openAddressHashingBigram((int)(8374230/KnesserNeyConstants.LOAD_FACTOR)); //(14923457);
	
	// Use Following for open address hashing
	 KnesseyNeyUtils.openAddressHashingBigram bigramCounter = bigramCounterTmp;
	 KnesseyNeyUtils.openAddressHashing trigramCounter = new KnesseyNeyUtils.openAddressHashing((int)(41627672/0.60)); //(56123457);
	
	// Use following for Sorted keys implementation
	// KnesseyNeyUtils.binarySearchCounter trigramCounter = new KnesseyNeyUtils.binarySearchCounter(61123457);
	// KnesseyNeyUtils.binarySearchCounterBigram bigramCounter;

	long sum_fertility_counts;
	KnesseyNeyUtils.openAddressHashingCache trigramProbCache;
	long cacheHits,cacheMiss;
	long startTime = System.currentTimeMillis( );
	long prev_encoding; int prev_idx;

	public KnesserNey(Iterable<List<String>> sentenceCollection) {
		
		System.out.println("Building KnesserNey . . .");
		
		trigramProbCache = new KnesseyNeyUtils.openAddressHashingCache(KnesserNeyConstants.TRIGRAM_PROBABILITY_CACHE_SIZE);
		cacheHits = 0;
		cacheMiss = 0;
		prev_encoding = -1;
		prev_idx = -1;
		
		int sent = 0;
		long encoded_value=0;
		long encoded_value_next;
		int prev_word_idx, prev_prev_word_idx;
		int cur_word_idx; int ctr=0;
		for (List<String> sentence : sentenceCollection) {
			sent++;
			if (sent % 1000000 == 0) {
				System.out.println("On sentence " + sent);
				ctr++;
			}
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, START);
			stoppedSentence.add(STOP);
			prev_word_idx = -1;
			prev_prev_word_idx = -1; 
			for (String word : stoppedSentence) {
				cur_word_idx = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
				if (cur_word_idx >= wordCounter.length) {
					wordCounter = CollectionUtils.copyOf(wordCounter, wordCounter.length * 2);
				}
				wordCounter[cur_word_idx]++;
				if(prev_word_idx!=-1){
					encoded_value = KnesseyNeyUtils.getSimpleEncoding((long)prev_word_idx, (long)cur_word_idx, (short)20);
					bigramCounterTmp.incrementCount(encoded_value , 1);
					if(prev_prev_word_idx!=-1){
						encoded_value_next = KnesseyNeyUtils.getSimpleEncoding((long)prev_prev_word_idx, encoded_value, (short)40);
						trigramCounter.increment( encoded_value_next, 1);
					}
				}
				prev_prev_word_idx = prev_word_idx;
				prev_word_idx = cur_word_idx;	
			}
		}
		
		// Print various details
		System.out.println("Done building EmpiricalUnigramLanguageModel.");
		wordCounter = CollectionUtils.copyOf(wordCounter, EnglishWordIndexer.getIndexer().size());
		total = KnesseyNeyUtils.sum(wordCounter);
		System.out.println("total = "+total);
		System.out.println("wordCounter.length = " + wordCounter.length);
		System.out.println("trigram length = " + trigramCounter.getCount() );
		System.out.println("bigram length = " + bigramCounterTmp.getCount() );
		
		// Print memory usage at this point
		// --- MemoryUsageUtils.printMemoryUsage();
		
		
		int bigrams_counter = bigramCounterTmp.getCount();
		sum_fertility_counts = bigrams_counter;
		
		// Shioft to rank based model for trigram counter
		// TRIGRAM SHOFT TO SORTED KEYS....happens within this function if binarysearchcounter is being used
		// ------ trigramCounter.forceMerge();
		trigramCounter.shiftToRanks();

		//---------------------------------
		long maxUnigram = -1;
		for(int i=0;i<wordCounter.length;i++){
			if(wordCounter[i]>maxUnigram){
				maxUnigram = wordCounter[i];
			}
		}
		System.out.println(" Max. UNIGRAM COUNT = " + maxUnigram);

		//---------------------------------------------------------------------------------------------------------------------
		// 2. Go over bigrams and get fertility counts of unigrams
        populateUnigramFertilityCount();
		
		// 3. Go over trigrams w1 w2 w3. Update fertility count of w2,w3
        bigramCounterTmp.bigramFertilityCount = new int[bigramCounterTmp.tablesize];
		bigramCounterTmp.bigramLambda = new float[bigramCounterTmp.tablesize];
		populateBigramFertilityCount();
		
		// 4. Go over bigrams w1 w2. Update fertility count of *w1* by fertility count of w1w2
		populateUnigramStarFertilityCount();
		
		// 5. set lambdas' for unigrams 
		populateUnigramLambda();
		//Assert.assertFalse(true);
		
		// 6. set lambdas' for bigrams  
		populateBigramLambda();
		
		
		// 
		bigramCounterTmp.ranksForCounts = new short[bigramCounterTmp.tablesize];
		bigramCounterTmp.ranksForFertilityCounts = new short[bigramCounterTmp.tablesize];
		bigramCounterTmp.shiftToRanks();
		
		
			// BIGRAM SHOFT TO SORTED KEYS....
			//-------> 
		/*bigramCounter = new KnesseyNeyUtils.binarySearchCounterBigram(bigramCounterTmp);
			//-------> 
			bigramCounterTmp = null;
		*/
		

		System.gc();
		System.gc();
		
		//----------------------------------------------------------------------------------------
		// --- MemoryUsageUtils.printMemoryUsage();
		//Assert.assertFalse(true);
		
	}
	
	//-----------------------------------------------------------------------------------------------------
	private void populateUnigramFertilityCount(){
		long bigram_key;
		long w2;
		long temp_arr[] = null;
		temp_arr = bigramCounterTmp.keys;
		for(int i=0;i<bigramCounterTmp.getSize();i++){
			bigram_key = temp_arr[i];
			if(bigram_key==-1){
				continue;
			}
		w2 = KnesseyNeyUtils.getBits(bigram_key, (short)0, (short)19);
		unigramFertilityCount[(int)w2]+=1;
		w2 = KnesseyNeyUtils.getBits(bigram_key, (short)20, (short)39);
		unigramLambda[(int)w2]+=1;
		}
		System.out.println(" done with step 2 ");
	}
	
	private void populateBigramFertilityCount(){
		long temp_arr[]; long trigram_key,w;
		temp_arr = trigramCounter.getKeys();
		for(int i=0;i<trigramCounter.getSize();i++){
			trigram_key = temp_arr[i];
			if(trigram_key==-1){
				continue;
			}
			w = KnesseyNeyUtils.getBits(trigram_key, (short)0, (short)39); //w2,w3
			bigramCounterTmp.incrementFertilityCount(w, 1);
			// Update lambda w1w2 by 1
			w = KnesseyNeyUtils.getBits(trigram_key, (short)20, (short)59); //w1,w2
			bigramCounterTmp.incrementLambda(w, 1);
		}
		System.out.println(" done with step 3 ");
	}
	
	private void populateUnigramStarFertilityCount(){
		long temp_arr[] = null;  long bigram_key,w1;
		temp_arr = bigramCounterTmp.keys;
		for(int i=0;i<bigramCounterTmp.getSize();i++){
			bigram_key = temp_arr[i];
			if(bigram_key==-1){
				continue;
			}
			w1 = KnesseyNeyUtils.getBits(bigram_key, (short)20, (short)39);
			unigramStarFertilityCount[(int)w1]+= bigramCounterTmp.retrieveFertilityCountByIndex(i);
			//System.out.println(" ************** Setting for w2 with increment "+(int)w2+" "+bigramCounter.retrieveFertilityCountByIndex(i));
		}
		System.out.println(" done with step 4 ");
	}
	
	private void populateUnigramLambda(){
	for(int i=0;i<wordCounter.length;i++){
		if(wordCounter[i]==0){
			System.out.println("breaking at i = "+i);
			break;
		}
		if(unigramLambda[i]<0.01 || unigramStarFertilityCount[i]==0){
			unigramLambda[i] =  1.0f;
		}
		else {
			unigramLambda[i] = ((float)KnesserNeyConstants.D2 * unigramLambda[i]) / (unigramStarFertilityCount[i]);
		}
	}
	System.out.println(" done with step 5 ");
	}
	
	private void populateBigramLambda(){
		long temp_arr[]; long bigram_key;
		temp_arr = bigramCounterTmp.keys;
		float lambda_val;
		for(int i=0;i<bigramCounterTmp.getSize();i++){
			bigram_key = temp_arr[i];
			if(bigram_key==-1){
				continue;
			}
			lambda_val = bigramCounterTmp.retrieveLambdaByIndex(i);
			if(lambda_val<0.01 || bigramCounterTmp.retrieveCountByIndex(i)==0){
				bigramCounterTmp.insertLambdaAtIndex(i, 1.0f);
			}
			else {
				lambda_val = (lambda_val*(float)KnesserNeyConstants.D3)/( bigramCounterTmp.retrieveCountByIndex(i) );
				bigramCounterTmp.insertLambdaAtIndex(i, lambda_val);
			}
		}
		System.out.println(" done with step 6 ");
	}
	
	//---------------------------------------------------------------------------------------------------------

	public int getOrder() {
		return 3;
	}

	private double getThirdOrderProbability(long w1, long w2, long w3){
		 //System.out.println("getThirdOrderProbability "+w1+" "+w2+" "+w3);
		if(w1>=wordCounter.length){
			return getSecondOrderProbability(w2, w3);
		}
		 long right_encoding = KnesseyNeyUtils.getSimpleEncoding(w1, w2, (short)20 );
		 int idx;
		 
		 //idx = bigramCounter.retrieveIndex(right_encoding);
		  if(right_encoding == prev_encoding){ 
			  idx = prev_idx;
		  }
		  else {
			  idx = bigramCounter.retrieveIndex(right_encoding);
			  prev_idx = idx;
			  prev_encoding = right_encoding;
		  }
		 
		 if(idx<0){ // w1,w2 bigram does not exist
			 return ( getSecondOrderProbability(w2, w3) );
		 }
		 long trigram_count  = 0;
	     trigram_count = trigramCounter.retrieve( KnesseyNeyUtils.getSimpleEncoding(w1, w2, w3, (short)20 )); 
		 if(trigram_count==-1){
			 return ( bigramCounter.bigramLambda[idx]*getSecondOrderProbability(w2,w3) );
		 }
		 // ranksToCounts[bigramCounter.ranksForCounts[idx]]
		 return ( (1.0*trigram_count - KnesserNeyConstants.D3)/(1.0*bigramCounter.retrieveCountByIndex(idx)) + bigramCounter.retrieveLambdaByIndex(idx)*getSecondOrderProbability(w2,w3) );
		 //return ( (1.0*trigram_count - KnesserNeyConstants.D3)/(1.0*bigramCounter.bigramCount[idx]) + bigramCounter.bigramLambda[idx]*getSecondOrderProbability(w2,w3) );
	}
	private double getSecondOrderProbability(long w2, long w3){
		//addToLogger("getSecondOrderProbability");
		//System.out.println("getThirdOrderProbability "+" "+w2+" "+w3);
		if(w2>=wordCounter.length){
			return getFirstOrderProbability(w3);
		}
		long unigram_star_fertility_count = unigramStarFertilityCount[(int) w2];
		if(unigram_star_fertility_count<=0){
			return getFirstOrderProbability(w3);
		}
		long bigram_count = bigramCounter.retrieveFertilityCount(KnesseyNeyUtils.getSimpleEncoding(w2, w3, (short)20));
		if(bigram_count==-1){
			return unigramLambda[(int) w2]*1.0*getFirstOrderProbability(w3) ;
		}
		//System.out.println("w2,w3, bi_count  :: " +w2+" "+w3+" "+bigramCounter.retrieveFertilityCount(KnesseyNeyUtils.getSimpleEncoding(w2, w3, (short)20)) );
		return ( (1.0*bigram_count - KnesserNeyConstants.D2) / (1.0 * unigram_star_fertility_count) )    + unigramLambda[(int) w2]*1.0*getFirstOrderProbability(w3) ;       
	}
	private double getFirstOrderProbability(long w3){
		//System.out.println("getThirdOrderProbability "+w3);
		return 1.0*unigramFertilityCount[(int) w3]/(1.0*sum_fertility_counts) ;       
	}
	public double getNgramLogProbability(int[] ngram, int from, int to) {
		
		if(ngram[to-1]>= wordCounter.length) return KnesserNeyConstants.DEFAULT_LOG_PROBABILITY_VALUE;

		//return Math.log((word < 0 || word >= wordCounter.length) ? 1.0 : wordCounter[word] / (total + 1.0));
		if( (to-from)==3){
			long key = KnesseyNeyUtils.getSimpleEncoding(ngram[from], ngram[from+1], ngram[from+2], (short)20);
			double val = trigramProbCache.retrieve(key);
			if( val < 0.001){
				return val;
			}
			else{
				val =  Math.log( getThirdOrderProbability(ngram[from],ngram[from+1],ngram[from+2]) );
				trigramProbCache.insert(key, (float)val);
				return val;
			}
			
		}
		else if( (to-from)==2){
			return Math.log(getSecondOrderProbability(ngram[from], ngram[from+1]) );
		}
		else if((to-from)==1){
			return Math.log( getFirstOrderProbability(ngram[from]) );
		}

		return KnesserNeyConstants.DEFAULT_LOG_PROBABILITY_VALUE;
	}

	//---------------------------------------------------------------------------------------------------------


	public long getCount(int[] ngram) {
		int word = ngram[0];
		if (word < 0 || word >= wordCounter.length) return 0;
		if(ngram.length==1){
			return wordCounter[ngram[0]];
		}
		else if(ngram.length == 2){
			if (word < 0 || word >= wordCounter.length) return 0;
			if (ngram[1] < 0 || ngram[1] >= wordCounter.length) return 0;
			return bigramCounter.retrieveCount(KnesseyNeyUtils.getSimpleEncoding(ngram[0], ngram[1], (short)20));
		}
		else if(ngram.length==3){
			if (word < 0 || word >= wordCounter.length) return 0;
			if (ngram[1] < 0 || ngram[1] >= wordCounter.length) return 0;
			if (ngram[2] < 0 || ngram[2] >= wordCounter.length) return 0;
			long val = trigramCounter.retrieve( KnesseyNeyUtils.getSimpleEncoding(ngram[0], ngram[1], ngram[2], (short)20) );
			if(val<0)val=0;
			return val;
		}
		return 0;
	}

}

