package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.mt.Alignment;
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

public class HeuristicAlignerFactory implements WordAlignerFactory
{
	public WordAligner newAligner(Iterable<SentencePair> trainingData) {
            try {
                //return new BaselineWordAligner();
                //return new HeuristicAligner(trainingData);
                return new HMMAligner(trainingData);
            } catch (Exception ex) {
                Logger.getLogger(HeuristicAlignerFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
	}

}
