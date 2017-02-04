package edu.berkeley.nlp.assignments.rerank.student;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import edu.berkeley.nlp.ling.AnchoredTree;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.IntCounter;

public class Utils {
	
	
    static EnglishBankCopy.LabeledConstituentEval<String> eval = new EnglishBankCopy.LabeledConstituentEval<String>(
				Collections.singleton("ROOT"), new HashSet<String>(
						Arrays.asList(new String[] { "''", "``", ".", ":", "," })));
	 
    static	Set<String> punctutationList = new HashSet<String>(Arrays.asList
    		("'", ".", ",", "\"", "?", "!", "(",")",":", "'","$","%","`","'","''","``","-","'s",";"));
    
    public static double getF1(Tree<String>guess, Tree<String>gold){
    	return eval.getF1(guess, gold);
    }
    
	public static boolean treesAreEqual(Tree<String> t1, Tree<String> t2){
		if(t1.toString().equalsIgnoreCase(t2.toString())) return true;
		return false;
		/* if(t1==null && t2!=null) return false;
		if(t1!=null && t2==null) return false;
		if(t1==null && t2==null) return true;
		if( !(t1.getLabel().equalsIgnoreCase(t2.getLabel())) ) return false;
		if(t1.getChildren().size()!=t2.getChildren().size()) return false;
		boolean ret=true;
		/*int iter=0;
		for(Tree<String> t1Child:t1.getChildren()){
			ret= ret && treesAreEqual(t1Child, t2.getChildren().get(iter));
			iter++;
			if(!ret){
				break;
			}
		}
		
		//return ret;
		if( (!ret) && t1.hashCode()==t2.hashCode()){
			System.out.println( t1.toString() );
			System.out.println( t1.toString() );
		}
		return ret;*/
		//return  t1.hashCode()==t2.hashCode();
	}
	
  public static void comb(double[] vect1, double x1, double[] vect2, double x2) {
	    for (int i = 0; i < vect1.length; ++i) {
	      vect1[i] = x1 * vect1[i] + x2 * vect2[i];
	    }
	  }
  
  public static void comb(double[] vect1, double x1, int[] vect2, double x2) {
	    for (int i: vect2) {
	      vect1[i] = x1 * vect1[i] + x2;
	    }
	  }
  
  public static void comb(double[] vect1, double x1, IntCounter vect2, double x2) {
	    for (Entry<Integer, Double> it: vect2.entries()) {
	      vect1[it.getKey().intValue()] = x1 * vect1[it.getKey().intValue()] + x2*it.getValue().doubleValue();
	    }
	  }

  public static double[] pointwiseMult(double[] vect1, double[] vect2) {
    double[] vect3 = new double[vect1.length];
    for (int i = 0; i < vect1.length; ++i) {
      vect3[i] = vect1[i] * vect2[i];
    }
    return vect3;
  }
  
  public static void divideArray(double[] vect1, double factor) {
	    for (int i = 0; i < vect1.length; ++i) {
	      vect1[i] = vect1[i]/factor;
	    }
	  }
  

  public static void setAllZeros(double[] vect1) {
    for (int i = 0; i < vect1.length; ++i) {
      vect1[i] = 0.0;
    }
  }
  
	public  static IntCounter convertArrayToIntCounter(int[] featureList)
	{
		IntCounter ret = new IntCounter();
		for(int f : featureList){
			double val = ret.get(f);
			ret.put(f, val+ 1);
		}
		return ret;
	}
	
	  public static int getRightBranchLength(int lengthTillNow, AnchoredTree<String> tree, int idx){
		  if(tree.isPreTerminal() && tree.getStartIdx() == (tree.getEndIdx()-1))
			  return (lengthTillNow+1);
		  List<AnchoredTree<String>> children = tree.getChildren();
		  for(int i = (children.size()-1); i >= 0; i--){
			  if((children.get(i).getEndIdx() > idx) && (children.get(i).getStartIdx() <= idx)) {
				  return getRightBranchLength(lengthTillNow+1, children.get(i), idx);
			  }
		  }
		  return 0;
	  }
	  
	  
  
}
