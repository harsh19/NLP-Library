package edu.berkeley.nlp.assignments.rerank.student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.math.DifferentiableFunction;
import edu.berkeley.nlp.math.SloppyMath;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

public class DifferentiableFunctionCustom implements DifferentiableFunction {

	int dimension;
	Iterable< Pair<KbestList, Tree<String>> > iteratorOverData;
	FeatsGenerator featsGenerator;
	Indexer<String> featureIndexer;
	List<DataPoint> dataPoints;

	
	double regularizationFactor = 1.0;

	public DifferentiableFunctionCustom(Iterable< Pair<KbestList, Tree<String>> > iteratorOverData, 
			Indexer<String> featureIndexer, FeatsGenerator featsGenerator){
		this.featureIndexer = featureIndexer;
		this.featsGenerator = featsGenerator;
		this.iteratorOverData = iteratorOverData;
		dataPoints = new ArrayList<DataPoint>();
		DataPoint d; 
		for(Pair<KbestList, Tree<String>> kbestListsAndGoldTree:iteratorOverData){
			d = getAllFeats(kbestListsAndGoldTree, featureIndexer, true);
			dataPoints.add(d);
		}
		dimension=featureIndexer.size();
		System.out.println("featureIndexer.size() = "+featureIndexer.size());
	}
	
	
	public DataPoint getAllFeats(Pair< KbestList, Tree<String> > kbestGoldPair ,
			Indexer<String> featureIndexer, 
			boolean addFeaturesToIndexer) {
		//System.out.println("-- called...");	
		
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
			//kBestLoss[i] = !Utils.treesAreEqual(kBestTrees.getKbestTrees().get(i), iter.getSecond()); 
		}
		int[] goldFeats = featureList[bestTreeIdx];
		IntCounter goldFeatures = Utils.convertArrayToIntCounter(goldFeats);
		DataPoint d = new DataPoint(goldFeatures, featureList);
		return d;
	}		
	
	
	@Override
	public int dimension() {
		return dimension;
	}

	@Override
	public double valueAt(double[] x) {
		int idx;
		double regularizationLoss=0.0, logEntropyLoss=0.0, totalLoss;
		double currentVal;
		for(double w:x){
			regularizationLoss+=(w*w);
		}
		regularizationLoss*=(regularizationFactor/dimension);
		int counter=0;
		double correctLoss = 0.0;
		double tmp;
		Pair<KbestList, Tree<String>> iterator;
		for( DataPoint data: dataPoints){
			tmp=Double.NEGATIVE_INFINITY;
			idx=0;
			counter++;
			int sz=data.getLength(); //length;
			for(idx=0; idx<sz;){
				int[] feats = data.getkBestFeatures(idx);
				currentVal = getDotProduct(x, feats);
				//currentVal = feats.dotProduct(x);
				//System.out.println(currentVal);
				tmp = SloppyMath.logAdd(tmp, currentVal);
				idx++;
			}
			correctLoss = data.getGoldFeatures().dotProduct(x);
			
			tmp -= correctLoss;
			logEntropyLoss+=tmp;

		}
		totalLoss = regularizationLoss + logEntropyLoss/counter;
		System.out.println("[DiffFunctionCustom.value] regularizationLoss, EntropyLoss, totalLoss, counter "
		            +regularizationLoss+" "+logEntropyLoss/counter+" "+totalLoss+" "
				    +counter);
		return totalLoss;
	}

	private double getDotProduct(double w[], int feats[]){
		double ret=0.0;
		for(int f:feats){
			ret+=w[f];
		}
		return ret;
	}
	
@Override
public double[] derivativeAt(double[] x) {
	System.out.println("[DiffFunctionCustom.derivativeAt] ");
	int idx; 
	double currentVal;
	double denominator;
	double[] ret = new double[x.length];
	double[] tmp = new double[x.length];
	// System.out.println("[DiffFunctionCustom.derivativeAt][tmp.length=] "+x.length);
	int counter = 0;
	for( DataPoint data: dataPoints){
		Utils.setAllZeros(tmp);
		idx=0;
		denominator = Double.NEGATIVE_INFINITY;
		int sz=data.getLength();
		for(idx=0; idx<sz;){
			int[] feats = data.getkBestFeatures(idx);
			currentVal = getDotProduct(x, feats); //feats.indicatorFeatures.dotProduct(x);
			denominator=SloppyMath.logAdd(denominator, currentVal);
			Utils.comb(tmp, 1.0, feats, Math.exp(currentVal));
			idx++;
		}
		IntCounter feats = data.getGoldFeatures();
		Utils.divideArray(tmp, Math.exp(denominator));
		Utils.comb(ret, 1.0, tmp, 1.0);
		Utils.comb(ret, 1.0, feats, -1.0);
		counter++;
	}
	Utils.comb(ret,1.0/counter,x,2*regularizationFactor/dimension);
	return ret;
}

}

