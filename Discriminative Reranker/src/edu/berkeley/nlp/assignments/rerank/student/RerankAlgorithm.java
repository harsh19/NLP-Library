package edu.berkeley.nlp.assignments.rerank.student;

import java.util.List;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Pair;

public interface RerankAlgorithm {
	
	public void train(Iterable<Pair<KbestList,Tree<String>>> kbestListsAndGoldTrees);
	
	public Tree<String> getBestParse(List<String> sentence, KbestList kbestList);

}
