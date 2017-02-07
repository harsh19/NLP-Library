package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.berkeley.nlp.assignments.parsing.BinaryRule;
import edu.berkeley.nlp.assignments.parsing.Grammar;
import edu.berkeley.nlp.assignments.parsing.Parser;
import edu.berkeley.nlp.assignments.parsing.SimpleLexicon;
import edu.berkeley.nlp.assignments.parsing.UnaryRule;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.util.CounterMap;


public class GenerativeParser implements Parser{

	  CounterMap<List<String>, Tree<String>> knownParses;

	  CounterMap<Integer, String> spanToCategories;

	  SimpleLexicon lexicon;
	  
	  CKYParser ckyParser;
	  Grammar grammar;
	  UnaryClosureCustom uc;

	  public Tree<String> getBestParse(List<String> sentence) {
	    System.out.println(" [SENTENCE LENGTH = ]\t"+sentence.size());

	    Tree<String> ansUsingCKY = ckyParser.solve(grammar, uc, lexicon, sentence);
	    return TreeAnnotationsCustom.unAnnotateTree(ansUsingCKY);
	    //System.out.println("Tree:\n" + Trees.PennTreeRenderer.render( TreeAnnotationsCustom.unAnnotateTree(ansUsingCKY)  ));
	    //System.out.println(" --->>>>>> ");
	    
	    /*if (knownParses.keySet().contains(tags)) {
	      annotatedBestParse = getBestKnownParse(tags);
	    } else {
	      annotatedBestParse = buildRightBranchParse(sentence, tags);
	    }
	    return TreeAnnotationsCustom.unAnnotateTree(annotatedBestParse);*/
	  }

	  private Tree<String> buildRightBranchParse(List<String> words, List<String> tags) {
	    int currentPosition = words.size() - 1;
	    Tree<String> rightBranchTree = buildTagTree(words, tags, currentPosition);
	    while (currentPosition > 0) {
	      currentPosition--;
	      rightBranchTree = merge(buildTagTree(words, tags, currentPosition), rightBranchTree);
	    }
	    rightBranchTree = addRoot(rightBranchTree);
	    return rightBranchTree;
	  }

	  private Tree<String> merge(Tree<String> leftTree, Tree<String> rightTree) {
	    int span = leftTree.getYield().size() + rightTree.getYield().size();
	    String mostFrequentLabel = spanToCategories.getCounter(span).argMax();
	    if (mostFrequentLabel == null) mostFrequentLabel = "NP";
	    List<Tree<String>> children = new ArrayList<Tree<String>>();
	    children.add(leftTree);
	    children.add(rightTree);
	    return new Tree<String>(mostFrequentLabel, children);
	  }
	  
	  private Tree<String> addRoot(Tree<String> tree) {
		    return new Tree<String>("ROOT", Collections.singletonList(tree));
		  }

		  private Tree<String> buildTagTree(List<String> words, List<String> tags, int currentPosition) {
		    Tree<String> leafTree = new Tree<String>(words.get(currentPosition));
		    Tree<String> tagTree = new Tree<String>(tags.get(currentPosition), Collections.singletonList(leafTree));
		    return tagTree;
		  }

		  private Tree<String> getBestKnownParse(List<String> tags) {
		    return knownParses.getCounter(tags).argMax();
		  }

		  private List<String> getBaselineTagging(List<String> sentence) {
		    List<String> tags = new ArrayList<String>();
		    for (String word : sentence) {
		      String tag = getBestTag(word);
		      tags.add(tag);
		    }
		    return tags;
		  }

		  private String getBestTag(String word) {
		    double bestScore = Double.NEGATIVE_INFINITY;
		    String bestTag = null;
		    for (String tag : lexicon.getAllTags()) {
		      double score = lexicon.scoreTagging(word, tag);
		      if (bestTag == null || score > bestScore) {
		        bestScore = score;
		        bestTag = tag;
		      }
		    }
		    return bestTag;
		  }


  private void printGrammar(Grammar grammar){
	  System.out.println();
	  System.out.println("Printing Grammer ---- ");
	  System.out.println("[BINARY SIZE = ] \t" + grammar.getBinaryRules().size());
	  /*for(BinaryRule br:grammar.getBinaryRules()){
		  System.out.println(br.toString() + "score= "+br.getScore());
	  }*/
	  System.out.println("............Printing Unary Rules; size = " + grammar.getUnaryRules().size());
	  /*for(UnaryRule ur:grammar.getUnaryRules()){
		  System.out.println(ur.toString());
	  }
	  System.out.println("----------------------------------------------------");
	  */
  }

  private void printIndexer(Grammar grammar){
	  System.out.println();
	  System.out.println("[INDEXER SIZE = ] \t" + grammar.getLabelIndexer().size());
	  /*int i =0 ;
	  for(String s:grammar.getLabelIndexer()){
		  System.out.println(i+":"+s );
		  i++;
	  }*/
  }
  private void printUnaryClosure(Grammar grammar, UnaryClosureCustom uc){
	  System.out.println();
	  System.out.println("[UNARY SIZE = ] \t"+grammar.getLabelIndexer().size());
	  /*int i =0 ;
	  for(String s:grammar.getLabelIndexer()){
		  System.out.println(i+":"+s);
		  for(UnaryRule ur:uc.getClosedUnaryRulesByParent(i)){
			  System.out.println("\t "+ur.toString()+" : "+ur.getScore());
		  }
		  i++;
	  }*/
  }
  
  
  public GenerativeParser(List<Tree<String>> trainTrees) {
	  
	ParserParameters.readParams();

    //System.out.print("Annotating / binarizing training trees ... ");
    List<Tree<String>> annotatedTrainTrees = annotateTrees(trainTrees);
    //System.out.println("done.");
    
   // System.out.print("Building grammar ... ");
    this.grammar = Grammar.generativeGrammarFromTrees(annotatedTrainTrees);
    //System.out.println("done. (" + grammar.getLabelIndexer().size() + " states)");
    //printIndexer(grammar);
    //printGrammar(grammar);
    
    this.uc = new UnaryClosureCustom(grammar.getLabelIndexer(),grammar.getUnaryRules());
    //System.out.println("Done with UC" + uc);
    //printUnaryClosure(grammar, uc);
    
    // so we will be using binary rules from grammar, and unary rules from uc

    //System.out.print("Discarding grammar and setting up a baseline parser ... ");
    // For FeaturizedLexiconDiscriminativeParserFactory, you should construct an instance of your own 
    // of LexiconFeaturizer here.
    lexicon = new SimpleLexicon(annotatedTrainTrees);

    knownParses = new CounterMap<List<String>, Tree<String>>();
    spanToCategories = new CounterMap<Integer, String>();
    for (Tree<String> trainTree : annotatedTrainTrees) {
      List<String> tags = trainTree.getPreTerminalYield();
      knownParses.incrementCount(tags, trainTree, 1.0);
      tallySpans(trainTree, 0);
    }
    
    ckyParser = new CKYParser(40, grammar.getLabelIndexer().size());
    
    //System.out.println("done.");
    
  }
  
  private List<Tree<String>> annotateTrees(List<Tree<String>> trees) {
	  
	TreeAnnotationsCustom.performVerticalMarkovization(trees);
	  
    List<Tree<String>> annotatedTrees = new ArrayList<Tree<String>>();
    Tree<String> tmp;
    //int iter = 0;
    for (Tree<String> tree : trees) {
    tmp = TreeAnnotationsCustom.annotateTreeLosslessBinarization(tree);
    	annotatedTrees.add(tmp);
    	/*if(iter<16){
    		System.out.println("ITER= "+iter);
    		System.out.println("Tree:\n" + Trees.PennTreeRenderer.render(tmp));
    	}
		iter++;
		*/
    }
    return annotatedTrees;
  }
  

  private int tallySpans(Tree<String> tree, int start) {
    if (tree.isLeaf() || tree.isPreTerminal()) return 1;
    int end = start;
    for (Tree<String> child : tree.getChildren()) {
      int childSpan = tallySpans(child, end);
      end += childSpan;
    }
    String category = tree.getLabel();
    if (!category.equals("ROOT")) spanToCategories.incrementCount(end - start, category, 1.0);
    return end - start;
  }

}
