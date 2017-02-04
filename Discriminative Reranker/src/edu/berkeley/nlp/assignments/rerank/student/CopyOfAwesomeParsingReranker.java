package edu.berkeley.nlp.assignments.rerank.student;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Pair;

public class CopyOfAwesomeParsingReranker  implements ParsingReranker {
	
private static enum algorithmChoices {
	PERCEPTRON,
	SVM,
	ENTROPY
};
private final algorithmChoices choice = algorithmChoices.SVM;
RerankAlgorithm rerankAlgorithm;
	  
public CopyOfAwesomeParsingReranker(Iterable<Pair<KbestList,Tree<String>>> kbestListsAndGoldTrees){
	
	if(choice==algorithmChoices.PERCEPTRON){
		rerankAlgorithm = new RerankPerceptron();
	}
	else if(choice==algorithmChoices.ENTROPY){
		rerankAlgorithm = new RerankEntropy();
	}
	else if(choice==algorithmChoices.SVM){
		rerankAlgorithm = new RerankSVMF1();
	}
	
	/*Tree<String> test = new Tree<String>("a");
	List<Tree<String>> lst = new ArrayList<Tree<String>>();
	lst.add(new Tree<String>("."));
	test.setChildren(lst);
	List<Tree<String>> lst2 = new ArrayList<Tree<String>>();
		lst2.add(new Tree<String>("."));
		lst.get(0).setChildren(lst2);
	System.out.println(FeatsGenerator.getRightmostTreeDepthHelper(test,1));*/
	
	
	//check(kbestListsAndGoldTrees);
	//rerankAlgorithm.extractFeatures()
	 
	 // Train
	rerankAlgorithm.train(kbestListsAndGoldTrees);
	 

}

  public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
  //System.out.println(sentence);
  //System.out.println(kbestList.getKbestTrees().get(0).toString());
    //return kbestList.getKbestTrees().get(0);
	  return rerankAlgorithm.getBestParse(sentence, kbestList);
  }
}
