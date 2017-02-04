package edu.berkeley.nlp.assignments.rerank.student;

import java.util.List;
import java.util.Random;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.WeightsUtils;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.math.DifferentiableFunction;
import edu.berkeley.nlp.math.LBFGSMinimizer;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

public class RerankEntropy implements RerankAlgorithm{

	int numOfEpochs = 30;
	double tolerance = 0.00001;

	LBFGSMinimizer minimizer;
	DifferentiableFunctionCustom differentiableFunctionCustom;
	DifferentiableFunction differentiableFunction;
	Indexer<String> featureIndexer;
	FeatsGenerator featsGenerator;
	double weights[];
	
	public RerankEntropy(){
		featureIndexer = new Indexer<String>();
		featsGenerator = new FeatsGenerator();
	}
	
	@Override
	public void train(Iterable<Pair<KbestList, Tree<String>>> kbestListsAndGoldTrees) {
		differentiableFunctionCustom = new DifferentiableFunctionCustom(kbestListsAndGoldTrees, 
				featureIndexer,  featsGenerator);
		 minimizer = new LBFGSMinimizer(numOfEpochs);
		 differentiableFunction = differentiableFunctionCustom;
		 int dimension = differentiableFunctionCustom.dimension();
		 Random rand = new Random(99);
		 weights = WeightsUtils.randDouble(dimension, rand);
		 weights = minimizer.minimize(this.differentiableFunction, this.weights, this.tolerance);

	}

	public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
		int bestTreeIdx=-1;
		double bestScore = Double.NEGATIVE_INFINITY;
		double currentTreeScore;
		FeatsGenerator featsGenerator = new FeatsGenerator(); 
		List<Tree<String>> kBestTrees = kbestList.getKbestTrees();
		
		for(int idx = 0; idx < kBestTrees.size(); idx++){
			int[] curTreeFeats = featsGenerator.extractKbestIdxTreeFeats(kbestList, idx, featureIndexer, false);
			currentTreeScore = Utils.convertArrayToIntCounter(curTreeFeats).dotProduct(weights);
			if(currentTreeScore > bestScore){
				bestScore = currentTreeScore;
				bestTreeIdx=idx;
			}
		}
		return kbestList.getKbestTrees().get(bestTreeIdx);
	}
	
	
	
}
