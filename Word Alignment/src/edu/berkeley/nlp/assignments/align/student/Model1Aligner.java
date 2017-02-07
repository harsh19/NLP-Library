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
import java.util.Iterator;
import java.util.Set;

public class Model1Aligner implements WordAligner
{
	
    Model1Solver model1solverFgivenE;
    Model1Solver model1solverEgivenF;
    
    public Model1Aligner(Iterable<SentencePair> trainingData){
        model1solverFgivenE = new Model1Solver(trainingData, true);
        model1solverEgivenF = new Model1Solver(trainingData, false);
    }
    
    public double getFEProb(String e, String f){
        return model1solverFgivenE.getScore(e, f);
    }
    public double getEFProb(String e, String f){
        return model1solverFgivenE.getScore(f, e);
    }
    
    @Override
    public Alignment alignSentencePair(SentencePair sentencePair) {
            Alignment alignment1 = model1solverFgivenE.alignSentencePair(sentencePair);
            Alignment alignment2 = model1solverEgivenF.alignSentencePair(sentencePair);
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
            return alignment;
            //return alignment;
    }
}
