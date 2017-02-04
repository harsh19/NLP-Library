package edu.berkeley.nlp.assignments.rerank.student;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.WeightsUtils;
import edu.berkeley.nlp.assignments.rerank.student.RerankAlgorithm;
import edu.berkeley.nlp.classify.FeatureExtractor;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

public class RerankPerceptron implements RerankAlgorithm{

	double[] featureWeights;
	double[] SumLastKFeatureWeights;
	double K;
	final int updateGap=7919; //10000;
	 int numOfIterations = 30;
	final boolean useAveraging=true;
	FeatsGenerator featsGenerator;
	List<DataPoint> dataPoints;
	Indexer<String> featureIndexer;
	List<Integer> bestTreeIndices;
	
	public RerankPerceptron(){
		 featureIndexer = new Indexer<String>();
		 featsGenerator = new FeatsGenerator();
		 dataPoints = new ArrayList<DataPoint>();
		 bestTreeIndices = new ArrayList<Integer>();
		 System.out.println("PERCEPTRON ALGORITHM ");
		 System.out.println("updateGap=  "+updateGap);
		 System.out.println("numOfIterations = "+numOfIterations);

	}
	
	@Override
	public void train(Iterable<Pair<KbestList, Tree<String>>> kbestListsAndGoldTrees) {
		DataPoint d; 
		for(Pair<KbestList, Tree<String>> kbestListsAndGoldTree:kbestListsAndGoldTrees){
			d = getAllFeats(kbestListsAndGoldTree, featureIndexer, true);
			dataPoints.add(d);
		}
		System.out.println("featureIndexer.size() = "+featureIndexer.size());
		featureWeights = new double[featureIndexer.size()];
		SumLastKFeatureWeights = new double[featureIndexer.size()];
		K=0;
		trainProcedure();
	}
	
private void addToSum(){
	int i=0;
	for(double w:featureWeights){
		SumLastKFeatureWeights[i]+=w;
		i++;
	}
	K++;
}


public DataPoint getAllFeats(Pair< KbestList, Tree<String> > kbestGoldPair ,
		Indexer<String> featureIndexer, boolean addFeaturesToIndexer) {
	
	KbestList kBestTrees = kbestGoldPair.getFirst();
	int numOfTrees = kBestTrees.getKbestTrees().size();
	int bestTreeIdx=-1;
	double bestF1tillNow = -999.0,tmp;
	int[][] featureList = new int[numOfTrees][];
	for(int i = 0; i <numOfTrees; i++){
		int[] curFeatureList = featsGenerator.extractKbestIdxTreeFeats(kbestGoldPair.getFirst(), i, 
				featureIndexer, true);
		featureList[i] = curFeatureList;
		tmp = Utils.getF1(kBestTrees.getKbestTrees().get(i), kbestGoldPair.getSecond());
		if(tmp > bestF1tillNow){
			bestF1tillNow = tmp;
			bestTreeIdx = i;
		}
		//if(Utils.treesAreEqual(kBestTrees.getKbestTrees().get(i), kbestGoldPair.getSecond()))
		//	bestTreeIdx=i;
		//kBestLoss[i] = !Utils.treesAreEqual(kBestTrees.getKbestTrees().get(i), iter.getSecond()); 
	}
	bestTreeIndices.add(bestTreeIdx);
	int[] goldFeats;
	if(bestTreeIdx<0)
		goldFeats =featsGenerator.extractGoldTreeFeatures(kbestGoldPair.getSecond(), kBestTrees, featureIndexer);
	else
		goldFeats = featureList[bestTreeIdx];
	IntCounter goldFeatures = Utils.convertArrayToIntCounter(goldFeats);
	DataPoint d = new DataPoint(goldFeatures, featureList);
	return d;
}		
	
	private void trainProcedure(){
		 int idx; int ctr;
		 int globalCounter=0;
		 for(int iter=1; iter<=numOfIterations; iter++){
			System.out.println("iter= "+iter);
			int[] feats; ctr=0; int bestTreeIdx;
			double maxScore=-Double.NEGATIVE_INFINITY,curScore;
			int maxScorFeats[] = null;
			 for(DataPoint d: dataPoints){
				 maxScore=Double.NEGATIVE_INFINITY;
				 bestTreeIdx = bestTreeIndices.get(ctr);
				 for(idx=0;idx<d.getLength();idx++){
					 if(bestTreeIdx==idx) continue;
					 feats = d.getkBestFeatures(idx);
					 curScore = getDotProduct(featureWeights,feats);
					 if(curScore>maxScore){
						 maxScore=curScore;
						 maxScorFeats=feats;
					 }
				 }
				 globalCounter++;
				 updateWeights(maxScorFeats, -1.0);
				 updateWeights(d.getGoldFeatures(), 1.0);
				 ctr++;
				 if(iter>=20){
					 if(globalCounter%updateGap==0){
						 addToSum();
					 }
				 }
			 }
		 }
		 
	}
	
	private void updateWeights(IntCounter feats, double factor){
		for(int f:feats.keySet()){
			featureWeights[f] += (factor*feats.get(f));
		}
	}
	
	private void updateWeights(int[] feats, double factor){
		for(int f:feats){
			featureWeights[f] += factor;
		}
	}
	
	@Override
	public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
		int bestTreeIdx=-1;
		double bestScore = Double.NEGATIVE_INFINITY;
		double currentTreeScore;
		//FeatsGenerator featsGenerator = new FeatsGenerator(); 
		List<Tree<String>> kBestTrees = kbestList.getKbestTrees();
		
		for(int idx = 0; idx < kBestTrees.size(); idx++){
			int[] curTreeFeats = featsGenerator.extractKbestIdxTreeFeats(kbestList, idx, featureIndexer, false);
			if(useAveraging)
				currentTreeScore = getDotProduct(SumLastKFeatureWeights,curTreeFeats);
			else
				currentTreeScore = getDotProduct(featureWeights,curTreeFeats);
			if(currentTreeScore > bestScore){
				bestScore = currentTreeScore;
				bestTreeIdx=idx;
			}
		}
		return kbestList.getKbestTrees().get(bestTreeIdx);
	}
	
	private double getDotProduct(double w[], int feats[]){
		double ret=0.0;
		for(int f:feats){
			ret+=w[f];
		}
		return ret;
	}

	
	
}
