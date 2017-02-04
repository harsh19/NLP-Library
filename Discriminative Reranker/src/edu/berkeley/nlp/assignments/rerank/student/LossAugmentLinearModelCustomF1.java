package edu.berkeley.nlp.assignments.rerank.student;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import edu.berkeley.nlp.assignments.rerank.LossAugmentedLinearModel;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import edu.berkeley.nlp.util.IntCounter;

public class LossAugmentLinearModelCustomF1 implements LossAugmentedLinearModel<DataPointF1>
{
	  public UpdateBundle getLossAugmentedUpdateBundle(DataPointF1 datum, IntCounter weights){
		  double maxLossAugmentedValue = Double.NEGATIVE_INFINITY;
		  double lossOfGuess = 0, currentTreeLoss;
		  double curLoss = 0;
		  double[] kBestLoss = datum.getkBestLoss();
		  IntCounter guessFeats = null;
		  for(int idx = 0; idx < kBestLoss.length; idx++){
			  IntCounter curFeature = Utils.convertArrayToIntCounter(datum.getkBestFeatures(idx));
			  currentTreeLoss = curFeature.dotProduct(weights);
			  curLoss = kBestLoss[idx]; //)?1:0;
			  if((currentTreeLoss+curLoss) > maxLossAugmentedValue){
				  maxLossAugmentedValue = currentTreeLoss+curLoss;
				  guessFeats = curFeature;
				  lossOfGuess = curLoss;
			  }
		  }
		  IntCounter goldFeats = datum.getGoldFeatures();
		  UpdateBundle bundle = new UpdateBundle(goldFeats, guessFeats, lossOfGuess);
		  return bundle;
	  }

}
