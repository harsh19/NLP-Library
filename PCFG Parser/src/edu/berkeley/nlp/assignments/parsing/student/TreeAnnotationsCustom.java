package edu.berkeley.nlp.assignments.parsing.student;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.util.Filter;

/**
 * Class which contains code for annotating and binarizing trees for the
 * parser's use, and debinarizing and unannotating them for scoring.
 */
public class TreeAnnotationsCustom
{
	/**
	 * This performs lossless binarization. You'll need to define your own
	 * function to do more intelligent markovization.
	 * 
	 * @param unAnnotatedTree
	 * @return
	 */
	static Map<String,String> symMapper = new HashMap<String,String>();
	static Map<String,Integer> counter = new HashMap<String,Integer>();
	static Map<String,Boolean> removed ;
	public static Tree<String> annotateTreeLosslessBinarization(Tree<String> unAnnotatedTree) {
		return binarizeTree(unAnnotatedTree);
	}
	
	public static void performVerticalMarkovization(List<Tree<String> > unAnnotatedTrees){
		symMapper = new HashMap<String,String>();
		counter = new HashMap<String,Integer>();
		removed = new HashMap<String, Boolean>();

		for(Tree<String>unAnnotatedTree:unAnnotatedTrees){
			if(ParserParameters.DO_VERTICAL_MARKOVIZATION){
				doVerticalMarkovization(unAnnotatedTree,0,null);
			}
			if(ParserParameters.DO_VARIABLE_VERTICAL_MARKOVIZATION){
				doVariableVerticalMarkovizationHelper(unAnnotatedTree,symMapper,counter);
			}
		}
		if(ParserParameters.DO_VARIABLE_VERTICAL_MARKOVIZATION){
			doVariableVerticalMarkovization();
			//printSymMapper();
			for(Tree<String>tree:unAnnotatedTrees){
				doVariableVerticalMarkovizationUpdateHelper(tree,symMapper);
			}
		}
	}
	
	private static void doVariableVerticalMarkovization(){

		
		for(String s:symMapper.keySet()){
			removed.put(s, false);
		}
		//System.out.println("\n Sizes of all three -- " + symMapper.size() + " " + counter.size() + " " + removed.size());
		//printSymMapper(symMapper);
		System.out.println("\n");
		String tmpSym; int ctr =0 ;
		for(int v=2*ParserParameters.VERTICAL_MARKOVIZATION_DEGREE; v>=1; v--){
			Set<String> keySet = new HashSet<String>(counter.keySet());
			ctr = 0;
			for(String sym:keySet){
				symMapper.put( sym, symMapper.get( symMapper.get(sym) ) );  // Y-> X, but X->Z...So Y should map to X. To achieve this, have an iteration of X -> (Y->Z)
				if(symMapper.get(sym)!=sym)
					ctr++;
			}
			System.out.println("ctr, tot = "+ctr+" "+symMapper.size());
			for(String sym:keySet){
				//System.out.println(sym);
				if(removed.get(sym)){
					continue;
				}
				if(counter.get(sym)<ParserParameters.VARIABLE_VERTICAL_MARKOVIZATION_COUNT_THRESH){
					tmpSym = stripOffLastAncestorSymbol(sym);
					if(tmpSym!=sym){
						if(!symMapper.containsKey(tmpSym)){
							symMapper.put(tmpSym,tmpSym);
							counter.put(tmpSym, counter.get(sym));
							removed.put(tmpSym, false);
						}
						else{
							counter.put(tmpSym, counter.get(tmpSym) + counter.get(sym));
						}
						symMapper.put(sym, tmpSym);
					}
					removed.put(sym, true);
				}
			}
		}
		//printSymMapper(symMapper);
	}
	
	private static void printSymMapper(){
		System.out.println("  -- Printing symbmapper .... ");
		for(String s:symMapper.keySet()){
			System.out.println(s + "-> " + symMapper.get(s));
		}
	}
	
	private static void doVariableVerticalMarkovizationHelper(Tree<String> tree, Map<String,String>symMapper, Map<String,Integer> counter){
		if(tree.isLeaf())
			return;
		if(!symMapper.containsKey(tree.getLabel())){
			symMapper.put(tree.getLabel(), tree.getLabel());
			counter.put(tree.getLabel(), 1);
		}
		else{
			counter.put(tree.getLabel(), counter.get(tree.getLabel())+1 );
		}
		for(Tree<String> child: tree.getChildren())
			doVariableVerticalMarkovizationHelper(child,symMapper,counter);
	}
	
	private static void doVariableVerticalMarkovizationUpdateHelper(Tree<String> tree, Map<String,String>symMapper){
		if(tree.isLeaf())
			return;
		tree.setLabel(symMapper.get(tree.getLabel()));
		for(Tree<String> child: tree.getChildren())
			doVariableVerticalMarkovizationUpdateHelper(child,symMapper);
	}

	
	private static String stripOffLastAncestorSymbol(String label){
		int idx = label.lastIndexOf(ParserConstants.ANCESTOR_SYMBOL);
		if(idx!=-1)
			label = label.substring(0, idx);
		return label;
	}
	
	private static void doVerticalMarkovization(Tree<String> tree, int depth, String parentLabel ) {
		if(tree.isLeaf()) return;
		if( (!ParserParameters.DO_VERTICAL_MARKOVIZATION_OF_PRETERMINALS) && tree.isPreTerminal()) return;
		String label = tree.getLabel();
		if(depth>0){
			if(depth>ParserParameters.VERTICAL_MARKOVIZATION_DEGREE){
				parentLabel = stripOffLastAncestorSymbol(parentLabel);
			}
			label = label + ParserConstants.ANCESTOR_SYMBOL + parentLabel;
		}
		if( (ParserParameters.DO_UNARY_DT) && (tree.getChildren().size()==1) && 
				(tree.getChildren().get(0).getLabel().contains(ParserConstants.DT_SYMBOL)) ){
			tree.getChildren().get(0).setLabel( tree.getChildren().get(0).getLabel() + 
					ParserConstants.INTERNAL_UNARY_MARK  );
		}
		else if( (ParserParameters.DO_UNARY_RB) && (tree.getChildren().size()==1) && 
				(tree.getChildren().get(0).getLabel().contains(ParserConstants.RB_SYMBOL) ) ){
			tree.getChildren().get(0).setLabel( tree.getChildren().get(0).getLabel() + 
					ParserConstants.INTERNAL_UNARY_MARK  );
		}
		for(Tree<String> tmp:tree.getChildren()){
			doVerticalMarkovization(tmp, 1+depth, label);
		}
		if(ParserParameters.DO_INTERNAL_UNARY_MARK && (tree.getChildren().size()==1)){
			label = label + ParserConstants.INTERNAL_UNARY_MARK;
		}
		if(ParserParameters.DO_SPLIT_IN && tree.isPreTerminal() && tree.getLabel().equalsIgnoreCase("IN")){
			switch(tree.getChildren().get(0).getLabel().toLowerCase()){
			case "of": label = label + "<OF";
				break;
			case "in":
			case "at":
			case "on":  label = label + "<TIMELOC";
				break;
			case "for":
			case "since":
			case "after":
			case "before":  label = label + "<TIME";
				break;
			}
		}
		if(ParserParameters.DO_SPLIT_AUX && tree.isPreTerminal() && tree.getLabel().contains("VB")){
			switch(tree.getChildren().get(0).getLabel().toLowerCase()){
			case "is":
			case "was":
			case "be":  label = label + "<BE";
				break;
			case "have":
			case "has":
			case "had": label = label + "<HAVE";
				break;
			}
		}
		if(ParserParameters.DO_SPLIT_CC && tree.isPreTerminal() && tree.getLabel().contains("CC")){
			switch(tree.getChildren().get(0).getLabel().toLowerCase()){
			case "but": label = label + "<BUT";
				break;
			case "&": label = label + "<AND";
				break;
			}
		}
		tree.setLabel(label);
	}
	
	private static Tree<String> binarizeTree(Tree<String> tree) {
		String label = tree.getLabel();
		tree.setLabel(label);
		if (tree.isLeaf()) return new Tree<String>(label);
		if (tree.getChildren().size() == 1) { return new Tree<String>(label, Collections.singletonList(binarizeTree(tree.getChildren().get(0)))); }
		// otherwise, it's a binary-or-more local tree, so decompose it into a sequence of binary and unary trees.
		String intermediateLabel = "@" + label + "->";
		/*if (tree.getChildren().size() == 2) { 
			return new Tree<String>(label, Collections.singletonList(binarizeTree(tree.getChildren().get(0)))); 
		}*/

		Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, intermediateLabel);
		return new Tree<String>(label, intermediateTree.getChildren());
	}

	private static String stripOffFirstSibling(String label){
		//System.out.println( " -- checking: "+label);
		int idx = label.indexOf(ParserConstants.SIBLING_SYMBOL);
		int end_idx = label.indexOf(ParserConstants.SIBLING_SYMBOL,idx+1);
		if(end_idx==-1) 
			return label.substring(0,idx);
		else 
			return label.substring(0,idx)+label.substring(end_idx);
	}
	
	private static Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated, String intermediateLabel) {
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(binarizeTree(leftTree));
		String rightTreeLabel = intermediateLabel;
		if(ParserParameters.DO_HORIZONTAL_MARKOVIZATION){
			if(numChildrenGenerated >= ParserParameters.HORIZONTAL_MARKOVIZATION_DEGREE ){
				rightTreeLabel = stripOffFirstSibling(rightTreeLabel);
			}
			rightTreeLabel = rightTreeLabel + ParserConstants.SIBLING_SYMBOL + leftTree.getLabel();
		}
		if (numChildrenGenerated < (tree.getChildren().size() - 2)) {
			Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, rightTreeLabel );
			children.add(rightTree);
		}
		else  if (numChildrenGenerated == tree.getChildren().size() - 2) {
			Tree<String> rightTree = tree.getChildren().get(numChildrenGenerated+1);
			children.add( binarizeTree(rightTree) );
		}
		return new Tree<String>(intermediateLabel, children);
		/*
		 * 		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(binarizeTree(leftTree));
		if (numChildrenGenerated < tree.getChildren().size() - 1) {
			Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, intermediateLabel + "_" + leftTree.getLabel());
			children.add(rightTree);
		}
		 */
	}

	public static Tree<String> unAnnotateTree(Tree<String> annotatedTree) {
		// Remove intermediate nodes (labels beginning with "@"
		// Remove all material on node labels which follow their base symbol (cuts anything after <,>,^,=,_ or ->)
		// Examples: a node with label @NP->DT_JJ will be spliced out, and a node with label NP^S will be reduced to NP
		Tree<String> debinarizedTree = Trees.spliceNodes(annotatedTree, new Filter<String>()
		{
			public boolean accept(String s) {
				return s.startsWith("@");
			}
		});
		Tree<String> unAnnotatedTree = (new Trees.LabelNormalizer()).transformTree(debinarizedTree);
		return unAnnotatedTree;
	}
}