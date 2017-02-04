package edu.berkeley.nlp.assignments.rerank.student;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.SurfaceHeadFinder;
import edu.berkeley.nlp.ling.AnchoredTree;
import edu.berkeley.nlp.ling.Constituent;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;

/**
 * Baseline feature extractor for k-best lists of parses. Note that this does
 * not implement Featurizer, though you can adapt it to do so.
 * 
 * @author gdurrett
 *
 */
public class FeatsGenerator 
{

	  static final boolean usingIdxFeature =false;
	  static final boolean usingScoreFeature=true;
	  static final boolean usingBinnedScoreFeature=false;
	  static final boolean usingRulesFeatures = true;
	  static final boolean usingNgramSiblingsFeatures = true;
	  
	  static final boolean usingSpanLengthFeatures = true;
	  static final boolean usingSpanEdgePOSFeatures = true;
	  static final boolean usingSpanEdgeLexicalFeatures = true;
	  static final boolean usingSpanContextLexicalFeatures = true;
	  static final boolean usingSpanSplitRule = true;
	  static final boolean usingSpanShapeFeatures = true;
	  
	  static final boolean usingTreeDepth=false;
	  static final boolean usingTreeRightBranch=true;
	  static final boolean usingNgramsOfPOSTags=false;
	  static final boolean usingRightBranchNonTerminals=false;
	  
	  static final boolean usingHeadAnnotationFeature = false;
	  static final boolean usingLexicalHeadsOnPPRulesFeatures = false;
	  static final boolean usingWordsLCAFeatures = false;
	  static final boolean usingHeadDependencyWithWords = false;
	  
public FeatsGenerator(){
	  System.out.println("usingIdxFeature= "+usingIdxFeature);
	  System.out.println("usingScoreFeature= "+usingScoreFeature);
	  System.out.println("usingBinnedScoreFeature= "+usingBinnedScoreFeature);
	  System.out.println("usingRulesFeatures= "+usingRulesFeatures);
	  System.out.println("usingNgramSiblingsFeatures= "+usingNgramSiblingsFeatures);
	  
	  System.out.println("usingSpanLengthFeatures= "+usingSpanLengthFeatures);
	  System.out.println("usingSpanEdgePOSFeatures= "+usingSpanEdgePOSFeatures);
	  System.out.println("usingSpanEdgeLexicalFeatures= "+usingSpanEdgeLexicalFeatures);
	  System.out.println("usingSpanContextLexicalFeatures= "+usingSpanContextLexicalFeatures);
	  System.out.println("usingSpanSplitRule= "+usingSpanSplitRule);
	  System.out.println("usingSpanShapeFeatures= "+usingSpanShapeFeatures);
	  
	  System.out.println("usingTreeDepth= "+usingTreeDepth);
	  System.out.println("usingTreeRightBranch= "+usingTreeRightBranch);
	  System.out.println("usingNgramsOfPOSTags= "+usingNgramsOfPOSTags);
	  System.out.println("usingRightBranchNonTerminals= "+usingRightBranchNonTerminals);
	  System.out.println("usingHeadAnnotationFeature= "+usingHeadAnnotationFeature);
	  
}

/**
   * 
   * @param kbestList
   * @param idx
   *          The index of the tree in the k-best list to extract features for
   * @param featureIndexer
   * @param addFeaturesToIndexer
   *          True if we should add new features to the indexer, false
   *          otherwise. When training, you want to make sure you include all
   *          possible features, but adding features at test time is pointless
   *          (since you won't have learned weights for those features anyway).
   * @return the list of features present in the tree indexed by parameter idx.
   */
  public int[] extractKbestIdxTreeFeats(KbestList kbestList, int idx, Indexer<String> featureIndexer, boolean addFeaturesToIndexer) {
	  Tree<String> tree = kbestList.getKbestTrees().get(idx);
	  return ExtractTreeFeatures(tree, idx, kbestList.getScores()[idx], featureIndexer, addFeaturesToIndexer);  
  }
  
  public int[] extractGoldTreeFeatures(Tree<String> goldTree, KbestList kbestList, Indexer<String> featureIndexer) {
	  int idx = RerankingConstant.DEFAULT_TREE_IDX;
	  double score = RerankingConstant.DEFAULT_TREE_SCORE;
	  for(int i = 0; i < kbestList.getKbestTrees().size(); i++){
		  Tree<String> curTree = kbestList.getKbestTrees().get(i);
		  if(curTree.toString().equals(goldTree.toString())){
			  idx = i;
			  score = kbestList.getScores()[i];
			  break;
		  }
	  }
      return ExtractTreeFeatures(goldTree, idx, score, featureIndexer, true);
  }
  
  public int[] ExtractTreeFeatures(Tree<String> tree, int idx, double score, Indexer<String> featureIndexer, boolean addFeaturesToIndexer)
  {
	// Converts the tree
    // (see below)
    AnchoredTree<String> anchoredTree = AnchoredTree.fromTree(tree);
    // If you just want to iterate over labeled spans, use the constituent list
    Collection<Constituent<String>> constituents = tree.toConstituentList();
    // You can fire features on parts of speech or words
    List<String> poss = tree.getPreTerminalYield();
    List<String> words = tree.getYield();
    // Allows you to find heads of spans of preterminals. Use this to fire
    // dependency-based features
    // like those discussed in Charniak and Johnson
    SurfaceHeadFinder shf = new SurfaceHeadFinder();
	int sentenceLength = words.size();

    
    List<Integer> feats = new ArrayList<Integer>();

    if(usingIdxFeature){
  	  if(idx>=0){
  		  addFeature("Posn=" + idx, feats,featureIndexer, addFeaturesToIndexer);
  	 }
    }
    
    if(usingScoreFeature){
    if(score != Double.POSITIVE_INFINITY) {
    	int scoreNum = getBinnedParserScore(score);
    	addFeature("Score=" + scoreNum, feats, featureIndexer, addFeaturesToIndexer);
    	}
    }
    	
    

    for (AnchoredTree<String> subtree : anchoredTree.toSubTreeList()) 
    {
      if (!subtree.isPreTerminal() && !subtree.isLeaf()) 
      {
    	String rule = "Rule=" + subtree.getLabel() + " ->";
    	//String curTreeLabel = subtree.getLabel();
    	//boolean isAChildPP = false;
    	List<AnchoredTree<String>> children = subtree.getChildren();
    	
        int subtreeStartIndex = subtree.getStartIdx();
        int subtreeEndIndex = subtree.getEndIdx() - 1; 
        
        
        for (AnchoredTree<String> child : children) {
            	rule += " " + child.getLabel();
        }
        //System.out.println("rule="+rule);
        if(usingRulesFeatures){
            addFeature(rule, feats, featureIndexer, addFeaturesToIndexer);
        }
        /*if(usingLexicalHeadsOnPPRulesFeatures){
            if(curTreeLabel.contains("PP")){
	        	String headWord = words.get(shf.findHead(curTreeLabel, 
	        			poss.subList(subtreeStartIndex, subtreeEndIndex)) );
	        	//System.out.println("headWord= "+headWord);
	        	addFeature(rule+"^head="+headWord, feats, featureIndexer, addFeaturesToIndexer);
            }
        }*/
        /*if(usingHeadDependencyWithWords){
        	if(children.size()>1){
	        	int headIdx = shf.findHead(curTreeLabel, 
	        			poss.subList(subtreeStartIndex, subtreeEndIndex)) ;
	        	String rule2="Rule2=" + subtree.getLabel() + " ->";
	        	int ii=0,jj=0;
	        	String posHeadIdx = poss.get(headIdx), headLabIdx=""; String adj1="adj=1",adj0="adj0"; String adj;
	            for (AnchoredTree<String> child : children) {
	            	if(child.getEndIdx()<headIdx && child.getStartIdx()>=headIdx){
	            		headLabIdx = child.getLabel();
	            		break;
	            	}
	            	ii++;
	            }
	            for (AnchoredTree<String> child : children) {
	            	if(jj==ii){
	            		
	            	}
	            	else{
	            		adj = (jj==(ii-1) || jj==(ii+1))?adj1:adj0;
	                    addFeature(rule2 + headLabIdx + "^" + posHeadIdx + " " + child.getLabel() + " a=" +adj, 
	                    		feats, featureIndexer, addFeaturesToIndexer);
	            	}
	            	jj++;
	            }
        	}
        }*/

        //------------------------------------------------------SPAN FEATURES
        if(usingSpanLengthFeatures){
	        String spanLengthRule = rule+"^"+getSpanLengthFeatureHelper(subtree.getSpanLength());
	        addFeature(spanLengthRule, feats, featureIndexer, addFeaturesToIndexer);
        }
        if(usingSpanEdgeLexicalFeatures){
	        String startingWordRule = rule + "^startWord=" + words.get(subtreeStartIndex);
	        addFeature(startingWordRule, feats, featureIndexer, addFeaturesToIndexer);
	        String endWordRule = rule + "^endWord=" + words.get(subtreeEndIndex);
	        addFeature(endWordRule, feats, featureIndexer, addFeaturesToIndexer);
	    }
        if(usingSpanEdgePOSFeatures){
	        String startingWordPOSRule = rule + "^posStartW=" + poss.get(subtreeStartIndex);
	        addFeature(startingWordPOSRule, feats, featureIndexer, addFeaturesToIndexer);
	        String endWordPOSRule = rule + "^posEndW=" + poss.get(subtreeEndIndex);
	        addFeature(endWordPOSRule, feats, featureIndexer, addFeaturesToIndexer); 
        }
        if(usingSpanContextLexicalFeatures){
	        if(subtreeStartIndex > 0){
	        	String firstWordContextRule = rule + "^prevW= " + words.get(subtreeStartIndex-1);
	        	addFeature(firstWordContextRule, feats, featureIndexer, addFeaturesToIndexer);
	        }
	        if(subtreeEndIndex < (sentenceLength-1)){
	        	String lastWordContextRule = rule + "^nextW= " + words.get(subtreeEndIndex+1);
	        	addFeature(lastWordContextRule, feats, featureIndexer, addFeaturesToIndexer);
	        }
        }
        if(usingSpanSplitRule){
        	if(children.size() == 2){
	        	String splitWord = words.get(children.get(0).getEndIdx()-1);
	        	String binaryRuleSplitPointRule = "Rule=" + subtree.getLabel() + " ->" +
	        			children.get(0).getLabel() + "^" + splitWord + "^" + children.get(1).getLabel();
	        	addFeature(binaryRuleSplitPointRule, feats, featureIndexer, addFeaturesToIndexer);
	        }
        }
        if(usingSpanShapeFeatures){
        	/**
        	 * For each word in the span, 2
				we indicate whether that word begins with a cap-
				ital letter, lowercase letter, digit, or punctuation
				mark. If it begins with punctuation, we indicate
				the punctuation mark explicitly.
        	 */
        	String shapeFeature=rule+"^";
        	for(int i=subtreeStartIndex; i<=subtreeEndIndex; i++){
        		shapeFeature+=getShapeFeaturesHelper(words.get(i));
        	}
        	addFeature(shapeFeature, feats, featureIndexer, addFeaturesToIndexer);
        }
        if(usingNgramSiblingsFeatures){
        	addNgramsRules(subtree.getLabel(),subtree.getChildren(),feats,featureIndexer,addFeaturesToIndexer);
        }
        
      }//if ends here
    } // for loop ends here for anchored trees
    
    // --------------------- Right branch length features
    if(usingTreeRightBranch){
    	int lastNonPunctuationIndex = sentenceLength-1;
    	while(lastNonPunctuationIndex>=0){
    		if(!(Utils.punctutationList.contains(words.get(lastNonPunctuationIndex)))){
    			break;
    		}
    		lastNonPunctuationIndex--;
    	}
        int rightBranchLength = Utils.getRightBranchLength( 0, anchoredTree, lastNonPunctuationIndex);
        String rightBranchNonPunctuationRule = "rbLength=" + rightBranchLength;
        addFeature(rightBranchNonPunctuationRule, feats, featureIndexer, addFeaturesToIndexer);
    }
    if(usingRightBranchNonTerminals){
    	int lastNonPunctuationIndex = sentenceLength-1;
    	while(lastNonPunctuationIndex>=0){
    		if(!(Utils.punctutationList.contains(words.get(lastNonPunctuationIndex)))){
    			break;
    		}
    		lastNonPunctuationIndex--;
    	}
        addRightBranchNonTerminals(anchoredTree, idx, feats, featureIndexer, addFeaturesToIndexer);
    }
   
    
    //---------------------------- Surface Head Features

    
    
    int[] featsArr = new int[feats.size()];
    for (int i = 0; i < feats.size(); i++) {
      featsArr[i] = feats.get(i).intValue();
    }
    return featsArr;
  }

  private String getShapeFeaturesHelper(String word){
	  if(Utils.punctutationList.contains(word)){
		  return word;
	  }
	  if(Utils.punctutationList.contains(""+word.charAt(0))){
		  return ""+word.charAt(0);
	  }
	  if(Character.isDigit(word.charAt(0) )){
		  //System.out.println("--xxxxxx w="+word);
		  return "D";
	  }
	  if(Character.isUpperCase(word.charAt(0) )){
		  return "X";
	  }
	  if(Character.isLowerCase(word.charAt(0) )){
		  return "x";
	  }
	  //System.out.println("---------------------xxxxxx w="+word.charAt(0));
	  return "w";
  }
  
  private void addNgramsRules(String subTreeLabel, List<AnchoredTree<String>> children, List<Integer> feats, 
		  Indexer<String> featureIndexer, boolean addNew){
	  String trigramRule = "trigram=" + subTreeLabel + "^";
	  String current="";
	  int i=0; int sz=children.size();
	  if(sz<=2){
	     return;
	  }
	  String rule;
	  for(i=0;i<sz;i++){
		  rule = trigramRule;
		  rule = rule + (i==0?("START"):(children.get(i-1).getLabel())) + "^";
		  rule+=(children.get(i).getLabel()+"^");
		  rule = rule + (i==(sz-1)?("STOP"):(children.get(i+1).getLabel()));
	      addFeature(rule, feats, featureIndexer, addNew);
	  }
  }
  
  
  private void addFeature(String feat, List<Integer> feats, Indexer<String> featureIndexer, boolean addNew) {
    if (addNew || featureIndexer.contains(feat)) {
      feats.add(featureIndexer.addAndGetIndex(feat));
    }
  }
  
	private int getSpanLengthFeatureHelper(int len){
		if(len<=5){
			return len;
		}
		if(len<=10){
			return 10;
		}
		if(len<=20){
			return 20;
		}
		return 21;
	}
  
  /*private int GetScoreBucketNumber(double score)
  {
	  int num;
	  if(score >= -8)
		  return 1;
	  else if(score<=-30)
		  return (int)(( (-score)-8));
	  return 99;
  }*/
  

   private int getBinnedParserScore(double score){
	  int bucketNumber;
	  if(score >= -9)
		  bucketNumber = 9;
	  else if(score >= -11.5)
		  bucketNumber = 11;
	  else if(score >= -13)
		  bucketNumber = 13;
	  else if(score >= -14.4)
		  bucketNumber = 14;
	  else if(score >= -15.5)
		  bucketNumber = 15;
	  else if(score >= -16.8)
		  bucketNumber = 16;
	  else if(score >= -18.2)
		  bucketNumber = 18;
	  else if(score >= -20.1)
		  bucketNumber = 20;
	  else if(score >= -23.5)
		  bucketNumber = 23;
	  else
		  bucketNumber = 120;
	  return bucketNumber;
  }
   
  public void addRightBranchNonTerminals( AnchoredTree<String> tree, int idx, List<Integer> feats, 
		  Indexer<String> featureIndexer, boolean addNew){
	  if(tree.isLeaf())
		  return;
	  addFeature("rt_nt^"+tree.getLabel(), feats, featureIndexer, addNew);
	  List<AnchoredTree<String>> children = tree.getChildren();
	  for(int i = (children.size()-1); i >= 0; i--){
		  if((children.get(i).getEndIdx() > idx) && (children.get(i).getStartIdx() <= idx)) {
			  addRightBranchNonTerminals(children.get(i),idx,feats,featureIndexer,addNew);
			  return;
		  }
	  }
  }
  
  

}



/*
static final boolean usingIdxFeature =false;
static final boolean usingScoreFeature=true;
static final boolean usingBinnedScoreFeature=false;
static final boolean usingRulesFeatures = true;
static final boolean usingNgramSiblingsFeatures = true;

static final boolean usingSpanLengthFeatures = true;
static final boolean usingSpanEdgePOSFeatures = true;
static final boolean usingSpanEdgeLexicalFeatures = true;
static final boolean usingSpanContextLexicalFeatures = true;
static final boolean usingSpanSplitRule = true;
static final boolean usingSpanShapeFeatures = true;

static final boolean usingTreeDepth=false;
static final boolean usingTreeRightBranch=true;
static final boolean usingNgramsOfPOSTags=false;
static final boolean usingRightBranchNonTerminals=false;

static final boolean usingHeadAnnotationFeature = false;
*/
