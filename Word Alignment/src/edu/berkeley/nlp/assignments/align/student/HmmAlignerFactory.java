package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.assignments.align.AlignmentTester;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.mt.BaselineWordAligner;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.mt.WordAlignerFactory;
import edu.berkeley.nlp.mt.decoder.Decoder;
import edu.berkeley.nlp.mt.decoder.DecoderFactory;
import edu.berkeley.nlp.mt.decoder.DistortionModel;
import edu.berkeley.nlp.mt.phrasetable.PhraseTable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HmmAlignerFactory implements WordAlignerFactory
{

	public WordAligner newAligner(Iterable<SentencePair> trainingData) {



            try {
                return new DiscriminativeClassifierAligner(trainingData);
        //HMMAligner(trainingData);
                
                //return null;
            } catch (Exception ex) {
                Logger.getLogger(HmmAlignerFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
	}
        
}
