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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HMMAligner implements WordAligner
{
        HMMSolverLastState hmmSolverFrenchEnglish;
        HMMSolverLastState hmmSolverEnglishFrench;
    
        public HMMAligner(Iterable<SentencePair> trainingData) throws Exception{
            hmmSolverFrenchEnglish = new HMMSolverLastState(trainingData, true);
            hmmSolverEnglishFrench = new HMMSolverLastState(trainingData, false);
            
        }
        
        public HMMAligner(Iterable<SentencePair> trainingData, int iter) throws Exception{
            hmmSolverFrenchEnglish = new HMMSolverLastState(trainingData, true, iter);
            hmmSolverEnglishFrench = new HMMSolverLastState(trainingData, false, iter);
            
        }
        
       
    
	@Override
	public Alignment alignSentencePair(SentencePair sentencePair) {
            Alignment alignment1 = hmmSolverFrenchEnglish.alignSentencePair(sentencePair);
            Alignment alignment2 = hmmSolverEnglishFrench.alignSentencePair(sentencePair);
            
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
	}
        
        public Alignment getAlignmentFE(SentencePair sentencePair) {
            Alignment alignment1 = hmmSolverFrenchEnglish.alignSentencePair(sentencePair);
            return alignment1;
	}
}
