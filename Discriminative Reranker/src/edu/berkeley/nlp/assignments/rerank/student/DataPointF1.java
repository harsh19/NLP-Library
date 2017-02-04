package edu.berkeley.nlp.assignments.rerank.student;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

public class DataPointF1 {
	private IntCounter goldFeats;
	//private boolean kbestLoss[];
	private double kbestLoss[];
	private int[][] kbestFeats;
	int goldIdx;
	
	public DataPointF1(IntCounter goldFeatures, double[] kBestLoss, int[][] kBestFeatures)
	{
		this.setGoldFeatures(goldFeatures);
		this.setkBestLoss(kBestLoss);
		this.setkBestFeatures(kBestFeatures);
	}
	public DataPointF1(IntCounter goldFeatures, int[][] kBestFeatures)
	{
		this.setGoldFeatures(goldFeatures);
		this.setkBestFeatures(kBestFeatures);
	}

	public IntCounter getGoldFeatures() {
		return goldFeats;
	}

	public void setGoldFeatures(IntCounter goldFeatures) {
		this.goldFeats = goldFeatures;
	}

	public boolean[] getkBestLossOld() {
		return null; //kbestLoss;
	}

	public void setkBestLossOld(boolean kBestLoss[]) {
		//this.kbestLoss = kBestLoss;
	}
	public double[] getkBestLoss() {
		return kbestLoss;
	}

	public void setkBestLoss(double kBestLoss[]) {
		this.kbestLoss = kBestLoss;
	}
	
	public int[] getkBestFeatures(int idx) {
		return kbestFeats[idx];
	}
	
	public int[][] getkBestFeatures() {
		return kbestFeats;
	}

	public void setkBestFeatures(int[][] kBestFeatures) {
		this.kbestFeats = kBestFeatures;
	}
	public int getLength() {
		return kbestFeats.length;
	}
	
}
