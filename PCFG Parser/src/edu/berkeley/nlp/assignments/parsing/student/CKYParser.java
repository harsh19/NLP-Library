package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.berkeley.nlp.assignments.parsing.BinaryRule;
import edu.berkeley.nlp.assignments.parsing.Grammar;
import edu.berkeley.nlp.assignments.parsing.SimpleLexicon;
import edu.berkeley.nlp.assignments.parsing.UnaryClosure;
import edu.berkeley.nlp.assignments.parsing.UnaryRule;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.Pair;

public class CKYParser {
	int n;
	int stateCount;
	int maxN;
	double scoreBinary[][][];
	double scoreUnary[][][];
	int backPointerBinaryK[][][];
	int backPointerBinaryLeft[][][];
	int backPointerBinaryRight[][][];
	int backPointerUnaryRuleNumber[][][];
	Indexer<String> labelIndexer;
	List<String> words;
	UnaryClosureCustom uc;
	
	public CKYParser(int maxN, int stateCount){
		this.maxN = maxN;
		this.stateCount = stateCount;
		scoreBinary = new double[maxN][maxN][stateCount];
		scoreUnary = new double[maxN][maxN][stateCount];
		backPointerUnaryRuleNumber = new int[maxN][maxN][stateCount];
		backPointerBinaryK = new int[maxN][maxN][stateCount];
		backPointerBinaryLeft = new int[maxN][maxN][stateCount];
		backPointerBinaryRight = new int[maxN][maxN][stateCount];
	}
	
	private void reset(){
		//System.out.println("statecount, indexer size = "+stateCount + " " + labelIndexer.size());
		for(int i=0; i<n; i++){
			for(int j=i; j<n; j++){
				for(int sym=0; sym<stateCount; sym++){
					scoreBinary[i][j][sym] = Double.NEGATIVE_INFINITY;
					scoreUnary[i][j][sym] = Double.NEGATIVE_INFINITY;
				}
			}
		}
	}
	
	private double max(double a, double b){
		if(a>b)return a;
		return b;
	}
	
	private void debug(){
		for(int i=0;i<n;i++){
			System.out.println("i = "+i);
			for(int j=i;j<n;j++){
				System.out.println("j = "+ j);
				for(int curLabel=0; curLabel<stateCount; curLabel++){
					System.out.print(curLabel + ": " +scoreBinary[i][j][curLabel] + "\t");
					System.out.print(curLabel + ": (unary) " +scoreUnary[i][j][curLabel] + "\t");
				}
				System.out.println();
			}
			System.out.println();
		}
	}
	
	// ---------------------------------------------
	
	private Tree<String> reconstructSubTree(int i, int j, boolean unaryOrNot, int curLabel ){
		if(i==j && (!unaryOrNot)){
			List<Tree<String> > children = new ArrayList< Tree<String> >();
			children.add( new Tree<String>( words.get(i) ) );
			return new Tree<String>( labelIndexer.get(curLabel), children );
		}
		if(i>j){ // error.. should not happer
			return null;
		}
		List<Tree<String> > children = new ArrayList<Tree<String> >();
		if(unaryOrNot){ // unary
			int nextLabel = backPointerUnaryRuleNumber[i][j][curLabel];
			//System.out.println(i+" "+j+" "+curLabel + " ..: "+nextLabel);
			Tree<String> child = reconstructSubTree(i, j, false, nextLabel);
			List<Integer> path = uc.getPath(new UnaryRule(curLabel, labelIndexer.indexOf(child.getLabel())));
			if(path.size()==1){
				children.addAll( child.getChildren() );
				return new Tree<String>( labelIndexer.get(curLabel), children );
			}
			else if(path.size()==2){
				children.add( child );
				return new Tree<String>( labelIndexer.get(curLabel), children );
			}
			else{ // >2
				int lim = path.size();
				List<Tree<String> > tmpList; tmpList = new ArrayList<Tree<String> > ();tmpList.add(child);
				Tree<String> first = new Tree<String>( labelIndexer.get(path.get(lim-2)), tmpList );
				Tree<String> tmp,next;
				next = first;
				for(int idx=lim-3;idx>=1;idx--){
					tmpList = new ArrayList<Tree<String> > ();
					tmpList.add( next  );
					tmp = new Tree<String>( labelIndexer.get(path.get(idx)), tmpList );
					next = tmp;
				}
				children.add( next );
				return new Tree<String>( labelIndexer.get(curLabel), children );
			}
		}
		else{
			int leftLabel = backPointerBinaryLeft[i][j][curLabel];
			int rightLabel = backPointerBinaryRight[i][j][curLabel];
			int k = backPointerBinaryK[i][j][curLabel];
			//System.out.println(i+" "+j+" "+curLabel + " ;; "+leftLabel+" "+rightLabel+ " "+k);
			children.add( reconstructSubTree(i, k, true, leftLabel) );
			children.add( reconstructSubTree(k+1, j, true, rightLabel) );
			return new Tree<String>( labelIndexer.get(curLabel), children );
		}
	}
	
	private Tree<String> reconstruct(){
		//System.out.println(" ---  max score is "+scoreUnary[0][n-1][0] );
		//long nanos = System.nanoTime();
		if(scoreUnary[0][n-1][0] == Double.NEGATIVE_INFINITY){
			return new Tree<String>("ROOT", Collections.singletonList(new Tree<String>("JUNK")));
		}
		Tree<String> tmp = reconstructSubTree( 0, n-1, true, 0 );	
		//System.out.println( "Reconstruct time: " + (System.nanoTime() - nanos)/1000000.0 );
		return tmp;
	}
	
	// ---------------------------------------------
	
	public Tree<String> solve(Grammar grammar, UnaryClosureCustom unaryClosure, SimpleLexicon lexicon, List<String> sentence){
		// update n
		// POS tagging of sentence
		// Get (w_i,tag,score): Can use empty space for binary[][i][i] for filling this, though it is not binary
		// Have iterations. Do alternate unary and binary rounds
		//long nanos = System.nanoTime();
		n = sentence.size();
		labelIndexer = grammar.getLabelIndexer();
		words = sentence;
		uc = unaryClosure;
		reset();
		int labelSetSize = grammar.getLabelIndexer().size();

		//System.out.println(" ---- CHECKING VALUES AFTER RESET .... ");
		//debug();
		
		boolean debugMark = false;
		
		int i=0,idx;
		for(String word:sentence){
			idx=0;
			while(idx<labelSetSize){
				scoreBinary[i][i][idx] = ParserUtils.getScoreTag(word, labelIndexer.get(idx), lexicon);
				if(scoreBinary[i][i][idx] == Double.NaN)
					scoreBinary[i][i][idx] = Double.NEGATIVE_INFINITY;
				/*if(word.equalsIgnoreCase("and")) {
					System.out.println( ",i,j,idx,val " + i + "  " + i + " " + idx + " " + scoreBinary[idx][i][i] );
					debugMark = true;
				}*/
				idx++;
			}
			i++;
		}
		
		double curLabelBest,candidateScore;
		int j;
		//int tmpl,tmpr,tmpk;
		int tmpk;
		double tmpArrBinary[];
		//double tempArrUnary[][];
		int tmpLeft[], tmpRight[], tmpKarr[];
		double leftChildScore;
		
		// Have a unary run
		for(i=0; i<=(n-1); i++){
			j = i+1-1;
			//System.out.print("i,j = " + i + " "+j);
			
			// Solve unary
			for(int curLabel=0; curLabel<labelSetSize; curLabel++){
				curLabelBest = scoreUnary[i][j][curLabel];
				for(UnaryRule ur: unaryClosure.getClosedUnaryRulesByParent(curLabel)){
					if(scoreBinary[i][j][ur.child] == Double.NEGATIVE_INFINITY)
						continue;
					candidateScore = scoreBinary[i][j][ur.child] + ur.getScore();
					if( candidateScore > curLabelBest){
						curLabelBest = candidateScore;
						scoreUnary[i][j][curLabel] = curLabelBest;
						backPointerUnaryRuleNumber[i][j][curLabel] = ur.child; 
					}
				}
			}
		}
		
		//System.out.println(" ---- CHECKING VALUES .... ");
		//debug();

		for(int diff=2; diff<=n; diff+=1){
			//System.out.println("--------------------------------------------------------");

			for(i=0; i<=(n-diff); i++){
				j = i+diff-1;
				tmpArrBinary = scoreBinary[i][j];
				tmpLeft = backPointerBinaryLeft[i][j];
				tmpRight = backPointerBinaryRight[i][j];
				tmpKarr = backPointerBinaryK[i][j];
				//if(i<=j)continue;
				// Solve Binary
				//System.out.print("i,j = " + i + " "+j);
				for(int k=i;k<j;k++){
					
					for(int curLabel=0; curLabel<labelSetSize; curLabel++){
						leftChildScore = scoreUnary[i][k][curLabel];
						if(leftChildScore == Double.NEGATIVE_INFINITY){
							//System.out.println("1 iteration saved!!!");
							continue;
						}
						for(BinaryRule br: grammar.getBinaryRulesByLeftChild(curLabel) ){
							//if(scoreUnary[k+1][j][br.rightChild] == Double.NEGATIVE_INFINITY)
							//	continue;
							candidateScore = br.getScore() + leftChildScore
									+ scoreUnary[k+1][j][br.rightChild];
							if( tmpArrBinary[br.parent] < candidateScore){
								tmpArrBinary[br.parent] = candidateScore;
								tmpLeft[br.parent] = br.leftChild;
								tmpRight[br.parent] =br.rightChild;
								tmpKarr[br.parent] = k;
							}
						}
					}
				}
				// ..
			}
		
		for(i=0; i<=(n-diff); i++){
			j = i+diff-1;
			//System.out.print("i,j = " + i + " "+j);
			
			// Solve unary
			for(int curLabel=0; curLabel<labelSetSize; curLabel++){
				curLabelBest = scoreUnary[i][j][curLabel]; 
				tmpk=0;
				for(UnaryRule ur: unaryClosure.getClosedUnaryRulesByChild(curLabel)){
					if(scoreBinary[i][j][ur.child] == Double.NEGATIVE_INFINITY)
						continue;
					candidateScore = scoreBinary[i][j][ur.child] + ur.getScore();
					if( candidateScore > scoreUnary[i][j][ur.parent]){
						scoreUnary[i][j][ur.parent] = candidateScore;
						backPointerUnaryRuleNumber[i][j][ur.parent] = ur.child;
					}
				}
			}
		}
			
		}
		
		Tree<String> tmp = reconstruct();
		//System.out.println( "Total time: " + (System.nanoTime() - nanos)/1000000.0 );
		return tmp;
		//return null;
	}
	
}


/*for(i=0; i<=(n-diff); i++){
	j = i+diff-1;
	// Solve Binary
	if(j>i){
		//System.out.print("i,j = " + i + " "+j);
		for(int k=i;k<j;k++){
			for(int curLabel=0; curLabel<labelSetSize; curLabel++){
				curLabelBest = scoreBinary[i][j][curLabel];
				for(BinaryRule br: grammar.getBinaryRulesByParent(curLabel) ){
					candidateScore = br.getScore() + scoreUnary[br.leftChild][i][k] + scoreUnary[br.rightChild][k+1][j];
					if( candidateScore > curLabelBest){
						scoreBinary[i][j][curLabel] = candidateScore;
						curLabelBest = candidateScore;
						backPointerBinaryLeft[i][j][curLabel] = br.leftChild;
						backPointerBinaryRight[i][j][curLabel] = br.rightChild;
						backPointerBinaryK[i][j][curLabel] = k;
					}
				}
				if(debugMark){
			System.out.println("Solved binary i,j,sym " +i+" " +j + " " + curLabel + " with " +
					"score k "+ scoreBinary[i][j][curLabel] + " "+	
					backPointerBinaryK[i][j][curLabel] +" "+	
					backPointerBinaryLeft[i][j][curLabel] + 
					" "+	backPointerBinaryRight[i][j][curLabel] ) ; 
				}
			}
		}
	}
	// ..
}*/


/*
 * 
 * 		for(i=0; i<=(n-diff); i++){
			j = i+diff-1;
			//System.out.print("i,j = " + i + " "+j);
			
			// Solve unary
			for(int curLabel=0; curLabel<labelSetSize; curLabel++){
				curLabelBest = scoreUnary[i][j][curLabel]; 
				tmpk=0;
				for(UnaryRule ur: unaryClosure.getClosedUnaryRulesByParent(curLabel)){
					candidateScore = scoreBinary[i][j][ur.child] + ur.getScore();
					if( candidateScore > curLabelBest){
						curLabelBest = candidateScore;
						tmpk = ur.child;
					}
				}
				if( scoreUnary[i][j][curLabel] < curLabelBest){
					scoreUnary[i][j][curLabel] = curLabelBest;
					backPointerUnaryRuleNumber[i][j][curLabel] = tmpk;
				}
			}
		}
		*/


/*

for(i=0; i<=(n-diff); i++){
				j = i+diff-1;
				tmpArrBinary = scoreBinary[i][j];
				tmpLeft = backPointerBinaryLeft[i][j];
				tmpRight = backPointerBinaryRight[i][j];
				tmpKarr = backPointerBinaryK[i][j];
				//if(i<=j)continue;
				// Solve Binary
				//System.out.print("i,j = " + i + " "+j);
				for(int k=i;k<j;k++){
					
					for(int curLabel=0; curLabel<labelSetSize; curLabel++){
						tmpl=0; tmpr=0; tmpk=0;
						curLabelBest = tmpArrBinary[curLabel];
						for(BinaryRule br: grammar.getBinaryRulesByParent(curLabel) ){
							candidateScore = br.getScore() + scoreUnary[i][k][br.leftChild] 
									+ scoreUnary[k+1][j][br.rightChild];
							if( candidateScore > curLabelBest){
								curLabelBest = candidateScore;
								tmpl = br.leftChild;
								tmpr = br.rightChild;
								tmpk = k;
							}
						}
						if(tmpArrBinary[curLabel] != curLabelBest){
							tmpArrBinary[curLabel] = curLabelBest;
							tmpLeft[curLabel] = tmpl;
							tmpRight[curLabel] =tmpr;
							tmpKarr[curLabel] = tmpk;
						}
						
					}
				}
				// ..
			}
	
*/