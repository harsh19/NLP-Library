package edu.berkeley.nlp.assignments.rerank.student;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.PrimalSubgradientSVMLearner;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

public class RerankSVM implements RerankAlgorithm{

	IntCounter weights;
	Indexer<String> featureIndexer;
	FeatsGenerator featsGenerator;

	
	public RerankSVM(){
		weights = new IntCounter();
		featureIndexer = new Indexer<String>();
		featsGenerator = new FeatsGenerator(); 

	}
	
	public void train(Iterable<Pair<KbestList,Tree<String>>> kbestListsAndGoldTrees){
		PrimalSubgradientSVMLearner<DataPoint> svmLearner;
		LossAugmentLinearModelCustom lossAugmentedModel = new LossAugmentLinearModelCustom();
		
		List<DataPoint> trainingDataList = new ArrayList<DataPoint>();
		KbestList kBestTrees; int numOfTrees;
		int ctr =0 ;
		
		for(Pair<KbestList,Tree<String>> iter : kbestListsAndGoldTrees)
		{
			kBestTrees = iter.getFirst();
			numOfTrees = kBestTrees.getKbestTrees().size();
			
			boolean[] kBestLoss = new boolean[numOfTrees];
			int[] goldFeats = featsGenerator.extractGoldTreeFeatures(iter.getSecond(), kBestTrees, featureIndexer);
			IntCounter goldFeatures = Utils.convertArrayToIntCounter(goldFeats);
			int[][] featureList = new int[numOfTrees][];
			for(int i = 0; i <numOfTrees; i++){
				int[] curFeatureList = featsGenerator.extractKbestIdxTreeFeats(iter.getFirst(), i, featureIndexer, true);
				featureList[i] = curFeatureList;
				kBestLoss[i] = !Utils.treesAreEqual(kBestTrees.getKbestTrees().get(i), iter.getSecond()); 
			}
			DataPoint d = new DataPoint(goldFeatures, kBestLoss, featureList);
			trainingDataList.add(d);
			ctr++;
			if(ctr%1000==0){
				System.out.println("Training: scanned till " + ctr);
				System.out.println("Training: feature size= " + featureIndexer.size());
			}
		}
		System.out.println(" featureIndexer.size() = " + featureIndexer.size());
		
		int batchSize = RerankingParams.SVM_BATCH_SIZE;
		int numEpochs = RerankingParams.SVM_EPOCHS;
		double stepSize  = RerankingParams.SVM_STEP_SIZE;
		double regConstant = RerankingParams.SVM_REG_FACTOR;
		svmLearner = new PrimalSubgradientSVMLearner<DataPoint>(stepSize, regConstant, 
				featureIndexer.size(), batchSize);		
		weights = svmLearner.train(weights, lossAugmentedModel, trainingDataList, numEpochs);	
	}
	
	
	public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
		int bestTreeIdx=-1;
		double bestScore = Double.NEGATIVE_INFINITY;
		double currentTreeScore;
		//FeatsGenerator featsGenerator = new FeatsGenerator(); 
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
