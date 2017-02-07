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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DiscriminativeClassifierAligner implements WordAligner
{
    
    Model1Aligner model1Aligner;
    HMMAligner hmmAligner;
    
    
    double[] featureWeights;
    double cumFeatureWeights[];
    int numOfIterations = 20;
    List< List<Double> > features;
    List<Integer> labels;
    int numOfDataPoints;
    int numOfFeatures;
    int maxNumOfFeatures = 100;
    Map<Integer, Alignment> testAlignments;
            int numPositiveExamples = 0, numNegativeExamples =0 ;
    boolean dotrain=false;

    public DiscriminativeClassifierAligner(Iterable<SentencePair> trainingData) throws Exception{
        hmmAligner = new HMMAligner(trainingData,14);
        model1Aligner = new Model1Aligner(trainingData);
        numOfDataPoints=0;
        for(SentencePair sPair:trainingData){
            numOfDataPoints++;
        }
        if(dotrain){
            features = new ArrayList<>();
            labels = new ArrayList<>();
            String basePath = "../downloads/align_data";
            testAlignments = Alignment.readAlignments(basePath +"PATH"); 
            System.out.println("######### "+testAlignments.size());
            train(trainingData);
        }
        else{
            featureWeights = new double[] {0.42606975942654796 , -1.0 , 0.46695449700819697 , -0.7092214749366913 , -0.11230459312705132};
            cumFeatureWeights = featureWeights;
            double sumW = 0.0;
            featureWeights=cumFeatureWeights;
            for(double w:featureWeights)sumW=Math.max(sumW,Math.abs(w));
            for(int i=0;i<numOfFeatures;i++)featureWeights[i]=featureWeights[i]/sumW; 
            for(int i=0; i<numOfFeatures; i++)
                System.out.print(featureWeights[i] + " , ");
        }

    }
    
    private void getAllFeatures(Iterable<SentencePair> trainingData){
        int i=1;
        numOfFeatures=5;
        for(SentencePair sPair:trainingData){
            getFeats(sPair);
            i++;
        }
        System.out.println("------->> "+features.size());
    }

    private void getFeats(SentencePair sPair ){
        Alignment fe = hmmAligner.getAlignmentFE(sPair);
        Alignment hmmIntersect = hmmAligner.getAlignmentFE(sPair);
        int engPosition, frenchPosition=0;
        Alignment gtAlignments = testAlignments.get(sPair.getSentenceID());
        if(gtAlignments==null) {
            //System.out.println("i=, is null? " + sPair.getSentenceID() + " "+(gtAlignments==null));
            return;
        }
        for(String f:sPair.getFrenchWords()){
            engPosition=0;
            for(String e:sPair.englishWords){
                
                int label = (gtAlignments.containsPossibleAlignment(engPosition, frenchPosition) )?1:-1;
                if(label<0){
                    //System.out.println(" --------NEGGGGG");
                    if(Math.random()<0.02){
                    List<Double> feats = 
                        getFeats(e, f, engPosition, frenchPosition, sPair.englishWords.size(),sPair.frenchWords.size());    
                    labels.add(label);
                    features.add(feats);
                    numNegativeExamples++;
                    }
                }
                else{
                    if(Math.random()<0.18){
                    List<Double> feats = 
                            getFeats(e, f, engPosition, frenchPosition, sPair.englishWords.size(),sPair.frenchWords.size());                        
                    labels.add(label);
                    features.add(feats);
                    numPositiveExamples++;
                    }
                }
                engPosition++;
            }
            frenchPosition++;
        }
            
    }
    
    private List<Double> getFeats(String e, String f, int engPosition, int frenchPosition, int E, int F){
        List<Double> feats = new ArrayList<>();
        feats.add(Math.exp(model1Aligner.getFEProb(e, f)));
        feats.add(Math.exp(model1Aligner.getEFProb(e, f)));
        feats.add( Math.exp(model1Aligner.getFEProb(e, f)) * Math.exp(model1Aligner.getEFProb(e, f)));
        double diff = ( (engPosition*1.0)/(E*1.0) - (frenchPosition*1.0)/(F*1.0) );
        feats.add(4*Math.abs(diff));
        /*feats.add(diff*diff);
        feats.add(Math.sqrt(Math.abs(diff)));
        feats.add(Math.log(Math.abs(diff)));
        */
        feats.add(0.1); //bias
        
        return feats;
    }
    
    public void train(Iterable<SentencePair> trainingData) {
        // 1. Select a subset of data
        // 2. Load ground truth for the subset
        // 3. For every sentence
            // Run hmm to get candidates
            // Extract features for every pair of (f,e)
            // Train perceptron
         
        getAllFeatures(trainingData);
         System.out.println(" pos,neg = " +numPositiveExamples + " " + numNegativeExamples);
        featureWeights = new double[numOfFeatures];
        cumFeatureWeights = new double[numOfFeatures];
        for(int i=0; i<numOfFeatures; i++) featureWeights[i] = Math.random()-0.5;
       
        featureWeights = new double[] {0.42606975942654796 , -1.0 , 0.46695449700819697 , -0.7092214749366913 , -0.11230459312705132};
        cumFeatureWeights = featureWeights;
 
        //trainProcedure();
        //trainProcedure();
        double sumW = 0.0;
        featureWeights=cumFeatureWeights;
        for(double w:featureWeights)sumW=Math.max(sumW,Math.abs(w));
        for(int i=0;i<numOfFeatures;i++)featureWeights[i]=featureWeights[i]/sumW; 
        for(int i=0; i<numOfFeatures; i++)
            System.out.print(featureWeights[i] + " , ");
    }
	

    private void addToCumulative(){
        for(int i=0;i<numOfFeatures;i++){
            cumFeatureWeights[i]+=featureWeights[i];
        }
    }
	
    private void trainProcedure(){
        for(int iter=1; iter<=numOfIterations; iter++){
            int correct=0,incorrect=0,negcorrect=0,negincorrect=0;
               System.out.println("iter= "+iter);
               int ctr=0;
                for(List<Double> feats: features){
                        double dot = getDotProduct(featureWeights, feats);
                        int label = labels.get(ctr);
                        if((dot*label)>0){
                            if(label>0)
                                correct++;
                            else
                                negcorrect++;
                        }
                        else{
                            if(label>0)
                                incorrect++;
                            else
                                negcorrect++;
                            if(label>0)
                                updateWeights(feats, 1);
                           else
                                updateWeights(feats, -1);
                        }
                        ctr++;
                }
                for(int i=0; i<numOfFeatures; i++)
                    System.out.print(featureWeights[i] + " , ");
                System.out.println();
                System.out.println(": correct, incorrect, negcorrect, negincorrect= " +correct+" "
                        +incorrect + " "+negcorrect + " "+negincorrect);
                if(iter>10)
                    addToCumulative();
            }
		 
	}
	
	private void updateWeights(List<Double> feats, double factor){
		int i=0;
                for(double f:feats){
			featureWeights[i] += (factor*f);
                        i++;
		}
                //System.out.println("numof feats = " + feats.size());
	}
	

    private double getDotProduct(double w[], List<Double> feats){
            double ret=0.0;
            int i=0;
            for(double f:feats){
                    ret+=(f*w[i]);
                    i++;
            }
            return ret;
    }
   
    
    @Override
    public Alignment alignSentencePair(SentencePair sPair) {
        
            Alignment alignment1 = hmmAligner.hmmSolverFrenchEnglish.alignSentencePair(sPair);
            Alignment alignment2 = hmmAligner.hmmSolverEnglishFrench.alignSentencePair(sPair);
            
            Alignment alignment = new Alignment();
            Set<Pair<Integer, Integer>> setAlignmentsFgivenE = alignment1.getSureAlignments();
            Iterator<Pair<Integer, Integer>> iterator = setAlignmentsFgivenE.iterator();
            while(iterator.hasNext()){
                Pair<Integer, Integer> val = iterator.next();
                int epos = val.getFirst().intValue();
                int fpos = val.getSecond().intValue();
                if(alignment2.containsSureAlignment(epos,fpos)){
                    alignment.addAlignment(epos, fpos, true);
                }
            }
        
            /*double thresh=0.3;
            iterator = setAlignmentsFgivenE.iterator();
            while(iterator.hasNext()){
                Pair<Integer, Integer> val = iterator.next();
                int epos = val.getFirst().intValue();
                int fpos = val.getSecond().intValue();
                 List<Double> feats = getFeats(sPair.englishWords.get(epos), sPair.frenchWords.get(fpos), 
                         epos, fpos, sPair.englishWords.size(),sPair.frenchWords.size());
                double dot = getDotProduct(featureWeights, feats);
                if(dot>thresh){
                    if(Math.random()<0.001)
                        System.out.println("POS:" + dot);
                    alignment.addAlignment(epos, fpos, true);
                    //System.out.println(" ALIGNMENT = " + frenchPosition+":" + engPosition);
                }
                else{
                  // System.out.println(dot);
                     if(Math.random()<0.00001)
                        System.out.println("NEG:" + dot);
                }
            }
            iterator = alignment2.getSureAlignments().iterator();
            while(iterator.hasNext()){
                Pair<Integer, Integer> val = iterator.next();
                int epos = val.getFirst().intValue();
                int fpos = val.getSecond().intValue();
                 List<Double> feats = getFeats(sPair.englishWords.get(epos), sPair.frenchWords.get(fpos), 
                         epos, fpos, sPair.englishWords.size(),sPair.frenchWords.size());
                double dot = getDotProduct(featureWeights, feats);
                if(dot>thresh){
                    if(Math.random()<0.001)
                        System.out.println("POS:" + dot);
                    alignment.addAlignment(epos, fpos, true);
                    //System.out.println(" ALIGNMENT = " + frenchPosition+":" + engPosition);
                }
                else{
                  // System.out.println(dot);
                     if(Math.random()<0.00001)
                        System.out.println("NEG:" + dot);
                }
            }*/

            
            
        
            int frenchPosition=0;
            for(String f:sPair.getFrenchWords()){
                int engPosition=0;
                for(String e:sPair.englishWords){
                    List<Double> feats = getFeats(e, f, engPosition, frenchPosition, sPair.englishWords.size(),sPair.frenchWords.size());
                    double dot = getDotProduct(featureWeights, feats);
                    if(dot>0.3){
                        //if(Math.random()<0.001)
                        //    System.out.println("POS:" + dot);
                        alignment.addAlignment(engPosition, frenchPosition, true);
                        //System.out.println(" ALIGNMENT = " + frenchPosition+":" + engPosition);
                    }
                    else{
                      // System.out.println(dot);
                         //if(Math.random()<0.00001)
                          //  System.out.println("NEG:" + dot);
                    }
                    engPosition++;
                }
                frenchPosition++;
            }

            return alignment;
    }
}
