package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.util.Pair;
import java.lang.*;
import java.lang.Math;

public class Utils {

        static boolean debug = false;
        public static final String NULL_WORD = "NULL_WORD";
	public static double logAdd(double logX, double logY) {
		// make a the max
		if (logY > logX) {
			double temp = logX;
			logX = logY;
			logY = temp;
		}
		// now a is bigger
		if (logX == Double.NEGATIVE_INFINITY) { return logX; }
		double negDiff = logY - logX;
		if (negDiff < -20) { return logX; }
		return logX + java.lang.Math.log(1.0 + java.lang.Math.exp(negDiff));
	}
        
        //////////////////////////////////////////
	final static int thresh=35000;
	public static int getEncoding(int eng, int frn){
            if(frn>=thresh){
                System.out.println("ERRORROR : to > thresh for emmission encoding...");
            }
		return thresh*eng+frn;
	}
	public static Pair<Integer,Integer> getReverseEncoding(int code){
		return new Pair<>(code/thresh,code%thresh);
	}
        public static int getReverseEncodingFirstElement(int code){
		return code/thresh;
	}
        public static int getReverseEncodingSecondElement(int code){
		return code%thresh;
	}
        // "/test_aligns_big/test.wa");
        
        
        final static int transitionThresh=1000;
	public static int getEncodingTransition(int from, int to) throws Exception{
            if(to>=transitionThresh){
                System.out.println("ERRORROR : to > thresh for transition encoding...at to = "+to);
                throw new Exception();
            }
            return transitionThresh*from+to;
	}
        static int getEncodingTransition(int k, int s, boolean b) throws Exception {
            return getEncodingTransition(k, s);
        }
	public static Pair<Integer,Integer> getReverseEncodingTransition(int code){
		return new Pair<>(code/transitionThresh,code%transitionThresh);
	}
        public static int getReverseEncodingFirstElementTransition(int code){
		return code/transitionThresh;
	}
        public static int getReverseEncodingSecondElementTransition(int code){
		return code%transitionThresh;
	}
        
        public static int getSCountEncoding(int from, int to, boolean differenceEncoding){
            if(to==0 && from==0){
                return Integer.MAX_VALUE-2;
            }
            else if(to==0){
                return Integer.MAX_VALUE-1;
            }
            else if(from==0){
                return Integer.MAX_VALUE;
            }
            
            /*int lim=10;
            if(Math.abs(to-from)<=lim){
                return to-from+transitionThresh;
            }
            else if((to-from)>0)
                return (int)((Math.log(to-from))/Math.log(2)) +lim + transitionThresh;
            else{
                return -lim-(int)(Math.log(-(to-from))/Math.log(2.0)) + transitionThresh;
            }
            */
            
            /*if((to-from)>=14){
                return 14+transitionThresh;
            }
            else if((to-from)<=-14){
                return -14+transitionThresh;
            }*/
           return to-from+transitionThresh;
	}


public static void main(String args[]){
    int x = Integer.MAX_VALUE;
}



}

