package edu.berkeley.nlp.assignments.align.student;

import java.util.HashMap;
import java.util.Map;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;
import edu.berkeley.nlp.util.StringIndexer;
import edu.berkeley.nlp.util.TIntOpenHashMap;
import edu.berkeley.nlp.util.TIntOpenHashMap.Entry;
import java.util.ArrayList;
import java.util.List;

public class Model1Solver
{
	StringIndexer englishIndexer;
	StringIndexer frenchIndexer;
	
	IntCounter tcountsEF;
	IntCounter probsEF;
	IntCounter cumulativeCountsE;
	IntCounter cumulativeCountsF;
	
	Iterable<SentencePair> trainingData;
        int nullWordIdx;
        List<Pair<int[],int[]>> dataset;
        int numOfIterations=15;
        
        boolean useFirstAsEnglish;
	
	private int getEncoding(int eng, int frn){
		return Utils.getEncoding(eng, frn);
	}
	private Pair<Integer,Integer> getReverseEncoding(int code){
		return Utils.getReverseEncoding(code);
	}
        
        public IntCounter getProbsEF(){
            IntCounter ret = new IntCounter();
            for(int ef:probsEF.keySet()){
                ret.put(ef,Math.log(probsEF.get(ef)));
            }
            return ret;
        }
        
    private void indexData(Iterable<SentencePair> trainingData){
        //Indexing words
        nullWordIdx = englishIndexer.addAndGetIndex(Utils.NULL_WORD);
        //frenchIndexer.add(Utils.NULL_WORD);
        int e,f;
        int i=0;
        int idxWord=0;
        for(SentencePair sPair:trainingData){
                List<String> englishWordsData;
                List<String> frenchWordsData;
                if(this.useFirstAsEnglish){
                    englishWordsData = sPair.englishWords;
                    frenchWordsData = sPair.frenchWords;
                }
                else{
                    englishWordsData = sPair.frenchWords;
                    frenchWordsData = sPair.englishWords;
                }
            
                int[] engwords; 
                int[] fenchwords;
                int nullCount = Math.max(1,(int)(Math.ceil(englishWordsData.size()*0.25)));
                //nullCount=1;
                engwords = new int[englishWordsData.size()+nullCount];
                fenchwords = new int[frenchWordsData.size()];
                idxWord=0;
                while(nullCount>0){
                    nullCount--;
                    engwords[idxWord++] = nullWordIdx;
                }

                for(String englishWord:englishWordsData){
                        e = englishIndexer.addAndGetIndex(englishWord);
                        engwords[idxWord++]=e;
                }
                idxWord=0;
                for(String frenchWord:frenchWordsData){
                        f = frenchIndexer.addAndGetIndex(frenchWord);
                        fenchwords[idxWord++]=f;
                        for(String englishWord:englishWordsData){
                                e = englishIndexer.addAndGetIndex(englishWord);
                                int ef = Utils.getEncoding(e,f);
                                probsEF.incrementCount(ef, 1);
                        }
                }
                i++;
                if(i%1000==0){
                        System.out.println("iteration = "+i);
                }
                dataset.add(new Pair<int[],int[]>(engwords,fenchwords));
        }
        System.out.println("Model1 index data: englishIndexer.size() = "+englishIndexer.size());
        System.out.println("Model1 index data: frenchIndexer.size() = "+frenchIndexer.size());
}
	
    public Model1Solver(Iterable<SentencePair> trainingData, boolean useFirstAsEnglish){ 
        this(trainingData,useFirstAsEnglish,new StringIndexer(),new StringIndexer());
    }    
    
	public Model1Solver(Iterable<SentencePair> trainingData, boolean useFirstAsEnglish, StringIndexer englishIndexer, 
            StringIndexer frenchIndexer){
		this.trainingData = trainingData;
                this.useFirstAsEnglish = useFirstAsEnglish;
		this.englishIndexer = englishIndexer;
                this.frenchIndexer = frenchIndexer;
                System.out.println("INIT: englishIndexer.size() = "+englishIndexer.size());
                System.out.println("INIT: frenchIndexer.size() = "+frenchIndexer.size());                
		cumulativeCountsE = new IntCounter();
		cumulativeCountsF = new IntCounter();
		tcountsEF = new IntCounter();
		probsEF = new IntCounter();
                dataset = new ArrayList<Pair<int[],int[]>>();
                
                nullWordIdx = englishIndexer.addAndGetIndex(Utils.NULL_WORD);
		
		//Indexing words
		/*int e,f;
		int i=0;
		for(SentencePair sPair:trainingData){
			for(String englishWord:sPair.englishWords){
				e = englishIndexer.addAndGetIndex(englishWord);
			}
			for(String frenchWord:sPair.frenchWords){
				f = frenchIndexer.addAndGetIndex(frenchWord);
                                int englishLength = sPair.englishWords.size();
                                int nullCount = Math.max(1,(int)(englishLength*0.25));
                                while(nullCount>0){
                                    nullCount--;
                                    //sPair.englishWords.add(Utils.NULL_WORD);
                                }
				for(String englishWord:sPair.englishWords){
					e = englishIndexer.addAndGetIndex(englishWord);
					int ef = getEncoding(e,f);
					probsEF.incrementCount(ef, 1);
				}
			}
			i++;
			if(i%1000==0){
				System.out.println("iteration = "+i);
			}
		}*/
                indexData(trainingData);
                train();
		
	}
	
	////////////////////////////////////////////////////////////////////////
	private void train(){
		
		//init
		initTraining();
		
                System.out.println(" ---------\n RUNNING EM ...");
		for(int i=0;i<numOfIterations;i++){
                        if(i%1==0){
                            System.out.println("i= "+i);
                        }
			performEM();
		}
	}
	
	private void performEM(){
		for(int ef:tcountsEF.keySet()){
			tcountsEF.put(ef, 0.0);
		}
		for(int e:cumulativeCountsE.keySet()){
			cumulativeCountsE.put(e, 0.0);
		}
		int i=0,ef;
		
                for(i=0; i< dataset.size(); i++){
                    // for each training data point
                    int curEnglish[] = dataset.get(i).getFirst();
                    int curFrench[] = dataset.get(i).getSecond();
                    cumulativeCountsF = new IntCounter();
                    for(int f:curFrench){
                        for(int e:curEnglish){
                                ef = getEncoding(e,f);
                                cumulativeCountsF.incrementCount(f, probsEF.get(ef));
                               // System.out.println(cumulativeCountsF.get(f));
                        }
                    }
                    for(int f:curFrench){
                            for(int e:curEnglish){
                                    ef = getEncoding(e,f);
                                    tcountsEF.incrementCount(ef, probsEF.get(ef)/cumulativeCountsF.get(f));
                                    cumulativeCountsE.incrementCount(e, probsEF.get(ef)/cumulativeCountsF.get(f));
                            }
                    }
                
                }
                /*for(SentencePair sPair:trainingData){
			cumulativeCountsF = new IntCounter();
			for(String frenchWord:sPair.frenchWords){
				f = frenchIndexer.addAndGetIndex(frenchWord);
				for(String englishWord:sPair.englishWords){
					e = englishIndexer.addAndGetIndex(englishWord);
					ef = getEncoding(e,f);
					cumulativeCountsF.incrementCount(f, probsEF.get(ef));
                                       // System.out.println(cumulativeCountsF.get(f));
				}
			}
			for(String frenchWord:sPair.frenchWords){
				f = frenchIndexer.addAndGetIndex(frenchWord);
				for(String englishWord:sPair.englishWords){
					e = englishIndexer.addAndGetIndex(englishWord);
					ef = getEncoding(e,f);
					tcountsEF.incrementCount(ef, probsEF.get(ef)/cumulativeCountsF.get(f));
					cumulativeCountsE.incrementCount(e, probsEF.get(ef)/cumulativeCountsF.get(f));
				}
			}
		}*/
		
		for(int efkey:probsEF.keySet()){
			probsEF.put(efkey, tcountsEF.get(efkey)/cumulativeCountsE.get( 
                                getReverseEncoding(efkey).getFirst().intValue() ));
		}
		
		
	}

	private void initTraining(){
		for(int ef:probsEF.keySet()){
			Pair<Integer,Integer> pairef = getReverseEncoding(ef);
			cumulativeCountsE.incrementCount(pairef.getFirst(), probsEF.get(ef));
                        //cumulativeCountsE.incrementCount(pairef.getSecond(), 1);
		}
		for(int ef:probsEF.keySet()){
			Pair<Integer,Integer> pairef = getReverseEncoding(ef);
                        int e = pairef.getFirst();
			probsEF.put(ef,(probsEF.get(ef))/cumulativeCountsE.get( e ));
		}
	}
	////////////////////////////////////////////////////////////////////////

	
	private int getEnglishEncoding(String w){
		return englishIndexer.addAndGetIndex(w);
	}
	private int getFrenchEncoding(String w){
		return frenchIndexer.addAndGetIndex(w);
	}
        
        public double getScore(String e, String f){
            //System.out.println("---" + e+":"+f+" score = " + (probsEF.get( getEncoding(getEnglishEncoding(e), getFrenchEncoding(f)) )) );
            return probsEF.get( getEncoding(getEnglishEncoding(e), getFrenchEncoding(f)) );
        }


	public Alignment alignSentencePair(SentencePair sentencePair) {
		Alignment alignment = new Alignment();
		 List<String> frenchWordData; 
                List<String> englishWordData;
                if(this.useFirstAsEnglish){
                    englishWordData = sentencePair.getEnglishWords();
                    frenchWordData = sentencePair.getFrenchWords();
                }
                else{
                    englishWordData = sentencePair.getFrenchWords();
                    frenchWordData = sentencePair.getEnglishWords();
                }
                int numFrenchWords = frenchWordData.size();
                int numEnglishWords = englishWordData.size();
                
		for (int frenchPosition = 0; frenchPosition < numFrenchWords; frenchPosition++) {
			
                    int englishPosition=0; double maxScore=Double.NEGATIVE_INFINITY; 
			double curScore; String e;
			for(int j=0; j<numEnglishWords; j++){
                                e = englishWordData.get(j);
				curScore = getScore(e, frenchWordData.get(frenchPosition));
				if(curScore>maxScore){
					maxScore=curScore;
					englishPosition = j;
				}
				
			}
                        e = Utils.NULL_WORD;
                        curScore = getScore(e, frenchWordData.get(frenchPosition));
                        if(curScore>maxScore){
                                maxScore=curScore;
                                englishPosition = nullWordIdx;
                        }
                        
                        //System.out.println(" maxvalidx = " + frenchPosition+":" + englishPosition);
                        if(englishPosition!=nullWordIdx){
                            if(this.useFirstAsEnglish){
                                alignment.addAlignment(englishPosition, frenchPosition, true);
                            }
                            else{
                                alignment.addAlignment(frenchPosition, englishPosition, true);                                
                            }
                        }
                    
			/*int englishPosition = frenchPosition;
			if (englishPosition < numEnglishWords) 
				alignment.addAlignment(englishPosition, frenchPosition, true);
			*/
		}
		return alignment;
	}
}
