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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HMMSolver
{
	StringIndexer englishIndexer;
	StringIndexer frenchIndexer;
                
        HMMParams hmmParamsEven;
        HMMParams hmmParamsOdd;
        HMM hmm;
        
        List<Pair<int[],int[]>> dataset;
        boolean useFirstAsEnglish;
        boolean useModel1ToInit = true;
        int efKeyset[];
        int transitionKeySet[];
        boolean debug = Utils.debug;
        private int numOfIterations=10;
        	

        /////////////////////////////////////////
        
         private Pair<Integer,Integer> indexData(Iterable<SentencePair> trainingData){
        //Indexing words
        englishIndexer.add(Utils.NULL_WORD);
        //frenchIndexer.add(Utils.NULL_WORD);
        int e,f;
        int i=0;
        int maxEnglishSentenceLength = 0; 
        int maxFrenchSentenceLength = 0;
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
                engwords = new int[englishWordsData.size()+1];
                fenchwords = new int[frenchWordsData.size()];
                
                
                idxWord=0;
                engwords[idxWord++] = englishIndexer.addAndGetIndex(Utils.NULL_WORD);
                for(String englishWord:englishWordsData){
                        e = englishIndexer.addAndGetIndex(englishWord);
                        engwords[idxWord++]=e;
                }
                maxEnglishSentenceLength = Math.max(maxEnglishSentenceLength, 
                        engwords.length);
                maxFrenchSentenceLength = Math.max(maxFrenchSentenceLength, 
                        frenchWordsData.size());
                idxWord=0;
                for(String frenchWord:frenchWordsData){
                        f = frenchIndexer.addAndGetIndex(frenchWord);
                        fenchwords[idxWord++]=f;
                        for(String englishWord:englishWordsData){
                                e = englishIndexer.addAndGetIndex(englishWord);
                                int ef = Utils.getEncoding(e,f);
                                //hmmParamsEven.emmissionProbs.put(ef, 0.0);
                                hmmParamsOdd.emmissionProbs.put(ef, 1.0);
                        }
                        e=englishIndexer.addAndGetIndex(Utils.NULL_WORD);
                        int ef = Utils.getEncoding(e,f);
                        //hmmParamsEven.emmissionProbs.put(ef, 0.0);
                        hmmParamsOdd.emmissionProbs.put(ef, 1.0);
                }
                i++;
                if(i%1000==0){
                        System.out.println("iteration = "+i);
                }
                dataset.add(new Pair<int[],int[]>(engwords,fenchwords));
        }
        System.out.println("englishIndexer.size() = "+englishIndexer.size());
        System.out.println("frenchIndexer.size() = "+frenchIndexer.size());
        System.out.println("maxEnglishSentenceLength = " + maxEnglishSentenceLength);
        return new Pair<>(maxEnglishSentenceLength, maxFrenchSentenceLength);
}
	
        private void printIndexers(){
            System.out.println(" --------------- PRINTING INDEXERS....");
            System.out.println("-- ENGLISH::");
            for(int i=0; i<englishIndexer.size(); i++){
                System.out.println("i, word: "+i+" "+englishIndexer.get(i));
            }
            System.out.println("-- FRENCH::");
            for(int i=0; i<frenchIndexer.size(); i++){
                System.out.println("i, word: "+i+" "+frenchIndexer.get(i));
            }

            System.out.println();
        }
        
        public void resetData(){
            dataset=null;
        }
        
	public HMMSolver(Iterable<SentencePair> trainingData, boolean useFirstAsEnglish) throws Exception{
            this.englishIndexer = new StringIndexer();
            this.frenchIndexer = new StringIndexer();
            hmmParamsEven = new HMMParams();
            hmmParamsOdd = new HMMParams();
            dataset = new ArrayList<Pair<int[],int[]>>();
            this.useFirstAsEnglish = useFirstAsEnglish;

            Pair<Integer,Integer> maxEnglishFrenchSentenceLength = indexData(trainingData);
            int maxEnglishSentenceLength = maxEnglishFrenchSentenceLength.getFirst();
            int maxFrenchSentenceLength = maxEnglishFrenchSentenceLength.getSecond();
            if(debug){
                   printIndexers();
            }

            //INIT efset and transitionSet
            int sz =0;
            for(Integer x:hmmParamsOdd.emmissionProbs.keySet()){
                sz++;
            }
            efKeyset = new int[sz];
            sz=0;
            for(Integer x:hmmParamsOdd.emmissionProbs.keySet()){
                efKeyset[sz++]=x;
            }

            Set<Integer> scountEncodingSet = new HashSet<Integer>();
            for(int i=0; i<maxEnglishSentenceLength;i++){
                for(int j=0; j<maxEnglishSentenceLength;j++){
                    int transitionEncoding = Utils.getSCountEncoding(i, j, true);
                    scountEncodingSet.add(transitionEncoding);
                }
            }
            int numOfTransitionCodes = scountEncodingSet.size();
            transitionKeySet = new int[numOfTransitionCodes];
            sz=0;
            scountEncodingSet = new HashSet<Integer>();
            for(int i=0; i<maxEnglishSentenceLength;i++){
                for(int j=0; j<maxEnglishSentenceLength;j++){
                    int transitionEncoding = Utils.getSCountEncoding(i, j, true);
                    if(!scountEncodingSet.contains(transitionEncoding)){
                        transitionKeySet[sz++] = transitionEncoding;
                        scountEncodingSet.add(transitionEncoding);
                    }
                }
            }

            // INTITIAL PARAMS
            double priorInitVal = Math.log( 1.0/(maxEnglishSentenceLength) );
            double emmissionInitVal = Math.log(1.0/(frenchIndexer.size()) );
            double transitionInitVal = Math.log( 1.0/(maxEnglishSentenceLength) );
            System.out.println("--------------------- INIT...");
            System.out.println(" priorInitVal = "+ priorInitVal + "\n");
            System.out.println(" transitionInitVal = "+ transitionInitVal + "\n");
            int xx = efKeyset[0];
            int e = Utils.getReverseEncodingFirstElement(xx);
            int f = Utils.getReverseEncodingSecondElement(xx);
            System.out.println("emmissionInitVal example: "+ englishIndexer.get(e) + " "+frenchIndexer.get(f) + " " + emmissionInitVal);
            if(this.useModel1ToInit){
                System.out.println(" Using Model1 to init..... ");
                Model1Solver model1solver = new Model1Solver(trainingData, useFirstAsEnglish, englishIndexer, frenchIndexer);
                hmmParamsOdd.emmissionProbs = model1solver.getProbsEF();
                System.out.println("After model1: englishIndexer.size() = "+englishIndexer.size());
                System.out.println("After model1: frenchIndexer.size() = "+frenchIndexer.size());    
                for(int efkey: efKeyset){
                    if(-0.000000001<hmmParamsOdd.emmissionProbs.get(efkey) ){
                        //System.out.println(" Found efkey with 0 val: val= " + hmmParamsOdd.emmissionProbs.get(efkey) ); 
                        hmmParamsOdd.emmissionProbs.put(efkey, emmissionInitVal);;
                    }
                }
            }
            else{
                for(int efkey: efKeyset){
                    hmmParamsOdd.emmissionProbs.put(efkey, emmissionInitVal);
                    //e = Utils.getReverseEncodingFirstElement(x);
                    //f = Utils.getReverseEncodingSecondElement(x);
                }
            }
            for(int x=0; x<maxEnglishSentenceLength; x++){
                hmmParamsOdd.priorProbs.put(x,priorInitVal);
                for(int y=0; y<maxEnglishSentenceLength; y++){
                    int tcode = Utils.getEncodingTransition(x, y);
                    hmmParamsOdd.transitionProbs.put(tcode, transitionInitVal);
                }
            }
            //System.out.println(" -------------------------------- ");

            // init hmm
            System.out.println("maxEnglishSentenceLength= "+maxEnglishSentenceLength);
            hmm = new HMM(maxEnglishSentenceLength, efKeyset, maxFrenchSentenceLength);

            // training procedure
            train();
		
	}
	
	////////////////////////////////////////////////////////////////////////
	private void train() throws Exception{
		int numOfIterations = this.numOfIterations;
                System.out.println(" ---------\n RUNNING EM ...");
		for(int i=1;i<=numOfIterations;i++){
                        if(i%1==0){
                            System.out.println("\n\niteration = "+i);
                        }
			performEM(i);
		}
                resetData();
	}
        
        private void resetAll(HMMParams hmmParams) throws Exception{
            // reset estimate ...
            hmmParams.clearAll();
            for(int ef:efKeyset){
                hmmParams.emmissionProbs.put(ef, Double.NEGATIVE_INFINITY);
            }
            int numOfStates = hmm.getNumberOfStates();
            for(int x=0; x<numOfStates; x++){
                for(int y=0; y<numOfStates; y++){
                    int tcode = Utils.getEncodingTransition(x, y);
                    hmmParams.transitionProbs.put(tcode, Double.NEGATIVE_INFINITY);
                }
            }
            for(int i=0; i<numOfStates; i++)
                hmmParams.priorProbs.put(i,Double.NEGATIVE_INFINITY);
        }
	
	private void performEM(int iterNum) throws Exception{
            
            // calculate and store ef keyset
            // if iterNum is even,... use even trans and emiss. as known.. change odd to all zeros
            // else ...
            // - compute alpha, compute beta, compute eta and update estimate, compute gamma and update estimates
       
            IntCounter curEmmissionProbs, estimateEmmissionProbs, curTransitionProbs, estimateTransitionProbs, priorProbs, estimatePriorProbs;
            HMMParams currentHmmParams, estimateHmmParams;
            if(iterNum%2==0){
                currentHmmParams = hmmParamsEven;
                estimateHmmParams = hmmParamsOdd;
            }
            else{
                currentHmmParams = hmmParamsOdd;
                estimateHmmParams = hmmParamsEven;
            }
            
            resetAll(estimateHmmParams);
            
            curEmmissionProbs = currentHmmParams.emmissionProbs;
            estimateEmmissionProbs = estimateHmmParams.emmissionProbs;
            curTransitionProbs = currentHmmParams.transitionProbs;
            estimateTransitionProbs = estimateHmmParams.transitionProbs;    
            priorProbs = currentHmmParams.priorProbs;
            estimatePriorProbs = estimateHmmParams.priorProbs;
            
            double logLikelihood=0;
            
            int numOfTrainingDataPoints = dataset.size();
            for(int i=0; i< numOfTrainingDataPoints; i++){
            // for each training data point
                int curEnglish[] = dataset.get(i).getFirst();
                int curFrench[] = dataset.get(i).getSecond();
                
                //E
                double sentProb =  hmm.computForwardScores(curEnglish, curFrench, currentHmmParams, efKeyset);
                logLikelihood += sentProb;
                double sentProbByBack = hmm.computBackwardScores(curEnglish, curFrench, 
                        currentHmmParams, efKeyset);
                if(i%1000==0)
                    System.out.println("data, prob of data, prob by backward computation "+i + " "+sentProb + " "+sentProbByBack);
                if(debug){
                    System.out.println("data, prob of data, prob by backward computation "+i + " "+sentProb + " "+sentProbByBack);
                }
                //M
                hmm.computeAndUpdateTransition(curEnglish, curFrench, currentHmmParams, estimateTransitionProbs, sentProb);
                hmm.computeAndUpdateEmmission(curEnglish, curFrench, currentHmmParams, estimateEmmissionProbs, sentProb);
                hmm.computeAndUpdatePrior(curEnglish, curFrench, currentHmmParams, estimateHmmParams, sentProb);
            }
            double logNumTrain = Math.log(numOfTrainingDataPoints);
            for(int ef:efKeyset){
                estimateEmmissionProbs.incrementCount(ef, -logNumTrain);
            }
            int numOfStates = hmm.getNumberOfStates();
            int timeSteps = hmm.getNumberOfTimeSteps();
            
            //System.out.println(" numOfStates:: "+numOfStates);
            normalize(timeSteps, numOfStates, estimateTransitionProbs, estimateEmmissionProbs, estimatePriorProbs);
            System.out.println("loglikelihood = "+logLikelihood);
            
        }

        
        private void normalize(int T, int numOfStates, IntCounter estimateTransitionProbs, IntCounter estimateEmmissionProbs, 
                IntCounter estimatePriorProbs) throws Exception{
            
            // TRANSITION
            double val;
            IntCounter codeSum = new IntCounter();
            for(int scountCode:transitionKeySet) 
                codeSum.put(scountCode, Double.NEGATIVE_INFINITY);
            for(int i=0; i<numOfStates;i++){
                for(int j=0; j<numOfStates;j++){
                    int scountCode = Utils.getSCountEncoding(i, j, true);
                    double prev = codeSum.get(scountCode);
                    int code = Utils.getEncodingTransition(i, j);
                    double cnt = estimateTransitionProbs.get(code);
                    codeSum.put(scountCode, Utils.logAdd(prev, cnt));
                }
            }
            double transitionDefValue = Math.log(1.0/(numOfStates));
            double transitionSum[] = new double[numOfStates];
            for(int i=0; i<numOfStates;i++){
                transitionSum[i] = Double.NEGATIVE_INFINITY;
                for(int j=0; j<numOfStates;j++){
                    int scountCode = Utils.getSCountEncoding(i, j, true);
                    transitionSum[i] = Utils.logAdd(transitionSum[i],  codeSum.get(scountCode) );
                }
                boolean useDefValue = false;
                if(transitionSum[i]==Double.NEGATIVE_INFINITY){
                    useDefValue=true;
                }
                for(int j=0; j<numOfStates;j++){
                    int code = Utils.getEncodingTransition(i, j);
                    int scountCode = Utils.getSCountEncoding(i, j, true);
                    val = useDefValue? transitionDefValue:(codeSum.get(scountCode)-transitionSum[i]);
                    estimateTransitionProbs.put(code, val);
                    if(false && i<3 && j<3)System.out.println("transition " + i + " " + j + " " + Math.exp(estimateTransitionProbs.get(code)));

                }
            }
            

            // EMMISSION
            double englishSum[] = new double[englishIndexer.size()];
            double defValueEmmission = Math.log(1.0/frenchIndexer.size());
            for(int i=englishIndexer.size()-1;i>=0;i--)
                englishSum[i]=Double.NEGATIVE_INFINITY;
            for(int ef:efKeyset){
                int e = Utils.getReverseEncodingFirstElement(ef);
                englishSum[e] = Utils.logAdd(englishSum[e], estimateEmmissionProbs.get(ef));
            }
            int iter=0;
            for(int ef:efKeyset){
                iter++;
                int e = Utils.getReverseEncodingFirstElement(ef);
                double prev = estimateEmmissionProbs.get(ef);
                if(englishSum[e]==Double.NEGATIVE_INFINITY)
                    estimateEmmissionProbs.put(ef, defValueEmmission);                    
                else
                    estimateEmmissionProbs.put(ef, prev-englishSum[e]);
                if(false && Utils.getReverseEncodingSecondElement(ef)==2) 
                    System.out.printf(" ef, = %d, e=%s, f=%s,  engSum= %f, prev= %f, updated=%f \n",ef,englishIndexer.get(e),
                            frenchIndexer.get(Utils.getReverseEncodingSecondElement(ef)), englishSum[e], Math.exp(prev), 
                            Math.exp(estimateEmmissionProbs.get(ef)) );
            }
            
            //PRIOR
            double priorSum=Double.NEGATIVE_INFINITY;
            for(int i=0;i<numOfStates;i++){
                priorSum = Utils.logAdd(priorSum, estimatePriorProbs.get(i));
            }
            for(int i=0;i<numOfStates;i++){
                estimatePriorProbs.incrementCount(i, -priorSum);
                if(false && i<10) System.out.println("prior " + i + " " + Math.exp(estimatePriorProbs.get(i)));
            }          
            
            //sanityCheck(T, numOfStates, estimateTransitionProbs, estimateEmmissionProbs, estimatePriorProbs);
            
        }
        
        private boolean isOne(double logVal){
            double val = Math.exp(logVal);
            double eps = 0.00001;
            if(Math.abs(val-1)<eps) return true;
            return false;
        }

        private void sanityCheck(int T, int numOfStates, IntCounter estimateTransitionProbs, IntCounter estimateEmmissionProbs, 
                IntCounter estimatePriorProbs) throws Exception{
            
            // TRANSITION
            double val;
            for(int i=0; i<numOfStates;i++){
                val = Double.NEGATIVE_INFINITY;
                for(int j=0; j<numOfStates;j++){
                    int code = Utils.getEncodingTransition(i, j);
                    double cnt = estimateTransitionProbs.get(code);
                    val = Utils.logAdd(val, cnt);
                }
                if(!isOne(val)){
                    System.out.println("*********************************Error in transition ... ");
                }
            }

            

            // EMMISSION
            double englishSum[] = new double[englishIndexer.size()];
            for(int i=englishIndexer.size()-1;i>=0;i--)
                englishSum[i]=Double.NEGATIVE_INFINITY;
            for(int ef:efKeyset){
                int e = Utils.getReverseEncodingFirstElement(ef);
                englishSum[e] = Utils.logAdd(englishSum[e], estimateEmmissionProbs.get(ef));
            }
            for(int e=0;e<englishIndexer.size();e++){
                if(!isOne(englishSum[e])){
                    System.out.println("******************************************Error with emmission... at e= "+e);
                }
            }
            
            //PRIOR
            double priorSum=Double.NEGATIVE_INFINITY;
            for(int i=0;i<numOfStates;i++){
                priorSum = Utils.logAdd(priorSum, estimatePriorProbs.get(i));
            }
            if(!isOne(priorSum)){
                System.out.println("************************************************Error in prior...");
            }      
            
            
        }
        
	public Alignment alignSentencePair(SentencePair sentencePair) {
            Alignment alignment = new Alignment();
            try {
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
                int engWords[] = new int[numEnglishWords+1];
                int frenchWords[] = new int[numFrenchWords];
                int nullWordIndex = englishIndexer.addAndGetIndex(Utils.NULL_WORD);
                int nullState = 0;
                int i=0;
                engWords[i++] = nullWordIndex;
                for(String s: englishWordData){
                    engWords[i++] = englishIndexer.addAndGetIndex(s); // new word at test can create issues
                }
                i=0;
                for(String s: frenchWordData){
                    frenchWords[i++] = frenchIndexer.addAndGetIndex(s);
                }
                int align[] = hmm.computeSequence(engWords,frenchWords, 
                        hmmParamsOdd);
                for (int frenchPosition = 0; frenchPosition < numFrenchWords; frenchPosition++) {
                    
                    //int englishPosition=0; double maxScore=Double.NEGATIVE_INFINITY; 
                    if(align[frenchPosition]!=nullState){
                        --align[frenchPosition];
                        if(this.useFirstAsEnglish){
                            alignment.addAlignment(align[frenchPosition], frenchPosition, true);
                        }
                        else{
                            alignment.addAlignment(frenchPosition, align[frenchPosition], true);                                
                        }                        
                        //alignment.addAlignment(align[frenchPosition], frenchPosition, true);
                        if(false) System.out.println("useFirstAsEnglish, frenchPosition, align[frenchPosition] = "+useFirstAsEnglish+" "+
                                frenchPosition+" "+align[frenchPosition]);

                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(HMMAligner.class.getName()).log(Level.SEVERE, null, ex);
            }
                            return alignment;
	}
}
