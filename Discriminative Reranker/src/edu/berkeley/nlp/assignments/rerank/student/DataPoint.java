package edu.berkeley.nlp.assignments.rerank.student;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

public class DataPoint {
	private IntCounter goldFeats;
	private boolean kbestLoss[];
	private int[][] kbestFeats;
	
	public DataPoint(IntCounter goldFeatures, boolean[] kBestLoss, int[][] kBestFeatures)
	{
		this.setGoldFeatures(goldFeatures);
		this.setkBestLoss(kBestLoss);
		this.setkBestFeatures(kBestFeatures);
	}
	public DataPoint(IntCounter goldFeatures, int[][] kBestFeatures)
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

	public boolean[] getkBestLoss() {
		return kbestLoss;
	}

	public void setkBestLoss(boolean kBestLoss[]) {
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
