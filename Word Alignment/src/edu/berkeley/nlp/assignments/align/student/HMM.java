package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.util.IntCounter;
import java.util.Map;


public class HMM {
	private int maxNumberOfStates;
        private int maxNumberOfTimeSteps;
	private int efKeyset[];
        boolean usingNull = true;

        double alpha[][];
        double beta[][];
        double eta[][]; 
        //double etaSum[];    
        IntCounter gamma; 
        //IntCounter gammaSum;
        
        public HMM(int maxNumberOfStates, int[] efKeyset, int maxNumberOfTimeSteps){
            this.maxNumberOfStates = maxNumberOfStates; 
            System.out.println(" maxNumberOfStates = "+maxNumberOfStates);
            this.maxNumberOfTimeSteps = maxNumberOfTimeSteps;
            this.efKeyset=efKeyset;
            alpha = new double[maxNumberOfStates][maxNumberOfTimeSteps];
            beta = new double[maxNumberOfStates][maxNumberOfTimeSteps];
            eta = new double[maxNumberOfStates][maxNumberOfStates];
            //etaSum = new double[maxNumberOfStates];
            gamma = new IntCounter();
            //gammaSum = new IntCounter();
        }
        
        public int getNumberOfStates(){
            return this.maxNumberOfStates;
        }
        public int getNumberOfTimeSteps(){
            return this.maxNumberOfTimeSteps;
        }

    double computForwardScores(int[] curEnglish, int[] curFrench, HMMParams curhmmparams, int[] efKeyset) throws Exception {
        
        IntCounter curEmmissionProbs = curhmmparams.emmissionProbs;
        IntCounter curTransitionProbs = curhmmparams.transitionProbs;
        IntCounter priorProbabilities = curhmmparams.priorProbs;
            int T=curFrench.length;
            int numOfStates = curEnglish.length;
            
            for(int s=0; s<numOfStates;s++) { alpha[s][0]= priorProbabilities.get(s)+
                            curEmmissionProbs.get(Utils.getEncoding(curEnglish[s], curFrench[0]));
                            //System.out.println("alpha["+s+"]["+0+"] = " + Math.exp(alpha[s][0]) );

            }
            for(int t=1;t<T;t++){
                    for(int s=0; s<numOfStates;s++){
                            alpha[s][t] = Double.NEGATIVE_INFINITY;
                            for(int k=0; k<numOfStates;k++){
                                //System.out.println(alpha[k][t-1]);
                                //System.out.println( curTransitionProbs.get( Utils.getEncodingTransition(k, s) ) );
                                //System.out.println( curEmmissionProbs.get(Utils.getEncoding(curEnglish[s], curFrench[t])) );
                                    alpha[s][t] = Utils.logAdd(alpha[s][t],  alpha[k][t-1]
                                            +curTransitionProbs.get( Utils.getEncodingTransition(k, s,true) )
                                            +curEmmissionProbs.get(Utils.getEncoding(curEnglish[s], curFrench[t])) );
                            
                            if(false){System.out.println(" ..... " + Math.exp(alpha[k][t-1]) + " " 
                                    + Math.exp( curTransitionProbs.get( Utils.getEncodingTransition(k, s, true) ) )
                                +Math.exp(curEmmissionProbs.get(Utils.getEncoding(curEnglish[s], curFrench[t])))  );                                    
                                }
                            }	
                            if(Utils.debug) System.out.println("alpha["+s+"]["+t+"] = " + Math.exp(alpha[s][t]) );

                    }
            }
            double ret=Double.NEGATIVE_INFINITY;
            for(int s=0; s<numOfStates;s++){
                    ret = Utils.logAdd(ret,alpha[s][T-1]);
            }
            if(Utils.debug){
                System.out.println("ret = " + Math.exp(ret));
            }
            return ret;
    }

    double computBackwardScores(int[] curEnglish, int[] curFrench, HMMParams curhmmparams, int[] efKeyset) throws Exception {
        IntCounter curEmmissionProbs = curhmmparams.emmissionProbs;
        IntCounter curTransitionProbs = curhmmparams.transitionProbs;
        IntCounter priorProbabilities = curhmmparams.priorProbs;
        int T=curFrench.length;
        int numOfStates = curEnglish.length;
        for(int s=0; s<numOfStates;s++) beta[s][T-1]= 0.0; // log(1) = 0
        for(int t=T-2;t>=0;t--){
                for(int s=0; s<numOfStates;s++){
                        beta[s][t] = Double.NEGATIVE_INFINITY;
                        for(int k=0; k<numOfStates;k++){
                                beta[s][t] = Utils.logAdd(beta[s][t],  beta[k][t+1] 
                                    + curTransitionProbs.get( Utils.getEncodingTransition(s, k,true) )
                                    + curEmmissionProbs.get(Utils.getEncoding(curEnglish[k], curFrench[t+1])) );
                        }
                        if(Utils.debug) System.out.println("beta["+s+"]["+t+"] = " + Math.exp(beta[s][t]) );
                }
        }
        double ret=Double.NEGATIVE_INFINITY;
        for(int s=0; s<numOfStates;s++){
                ret = Utils.logAdd(ret,beta[s][0]
                        +curEmmissionProbs.get(Utils.getEncoding(curEnglish[s], curFrench[0]))
                        +priorProbabilities.get(s));
        }
        return ret;
    }

    void computeAndUpdateTransition(int[] curEnglish, int[] curFrench, 
            HMMParams curhmmparams, IntCounter estimateTransitionProbs,double sentProb) throws Exception {
        IntCounter curEmmissionProbd = curhmmparams.emmissionProbs;
        IntCounter curTransitionProbs = curhmmparams.transitionProbs;
        IntCounter priorProbabilities = curhmmparams.priorProbs;
        int T = curFrench.length;
        int numOfStates = curEnglish.length;
        //System.out.println("num of states = " + numOfStates);
        double tmp;
        //double tmpSum =0.0;
        for(int i=0; i<numOfStates;i++){
            for(int j=0; j<numOfStates;j++){
                eta[i][j] = Double.NEGATIVE_INFINITY;
                int code = Utils.getEncodingTransition(i, j, true);
                for(int t=0;t<T-1;t++){
                    tmp = alpha[i][t] + curTransitionProbs.get(code) + beta[j][t+1] 
                            +curEmmissionProbd.get(Utils.getEncoding(curEnglish[j], curFrench[t+1]));
                    eta[i][j] = Utils.logAdd(eta[i][j], tmp);
                if(Utils.debug) System.out.println("--> eta[i][j] = " + i + "," + j + " " + Math.exp(alpha[i][j]) + " " + Math.exp(curTransitionProbs.get(code)) + " " + 
                            Math.exp(beta[j][t+1]) + " " +  Math.exp( curEmmissionProbd.get(Utils.getEncoding(curEnglish[j], curFrench[t+1])) )
                            + " " +  Math.exp(eta[i][j]) );
                //if(t==0)tmpSum += (Math.exp(eta[i][j]));

                }
                double prev = estimateTransitionProbs.get(code);
                estimateTransitionProbs.put(code, Utils.logAdd(prev, eta[i][j]-sentProb) );
                double val = estimateTransitionProbs.get(code);
                if(Utils.debug) System.out.println(" eta[i][j] = " + i + "," + j + " " + Math.exp(eta[i][j]) + " " + Math.exp(prev) + " " + 
                        Math.exp(sentProb) + " updated= "+Math.exp(val));
            }
        }
        
        if(false){
            for(int t=0;t<T;t++){
                double allStateSum = Double.NEGATIVE_INFINITY;
                for(int s=0; s<numOfStates;s++){
                    allStateSum = Utils.logAdd(allStateSum, alpha[s][t]+beta[s][t]);
                }
                System.out.printf("t=%d, sum=%f",t,allStateSum);
            }
            System.out.println();
        }
        
    }

    void computeAndUpdatePrior(int[] curEnglish, int[] curFrench, 
            HMMParams curhmmparams, HMMParams estimateParams, double sentProb) {
        IntCounter curEmmissionProbd = curhmmparams.emmissionProbs;
        IntCounter curTransitionProbs = curhmmparams.transitionProbs;
        IntCounter priorProbabilities = curhmmparams.priorProbs;
        IntCounter estimatePriorProbs = estimateParams.priorProbs;
        int T = curFrench.length;
        int numOfStates = curEnglish.length;
        double tmp;
        double prior[] = new double[numOfStates];
        for(int i=0; i<numOfStates;i++){
            int code = Utils.getEncoding(curEnglish[i] , curFrench[0]);
            tmp = priorProbabilities.get(i) + curEmmissionProbd.get( code ) + beta[i][0] - sentProb;
            prior[i] =  tmp;
            double prev = estimatePriorProbs.get(i);
            estimatePriorProbs.put(i, Utils.logAdd(prev, prior[i]) );
        }
    }

    
    void computeAndUpdateEmmission(int[] curEnglish, int[] curFrench, 
            HMMParams curhmmparams, 
            IntCounter estimateEmmission, double sentProb) {
        
        IntCounter curEmmissionProbd = curhmmparams.emmissionProbs;
        IntCounter curTransitionProbs = curhmmparams.transitionProbs;
        IntCounter priorProbabilities = curhmmparams.priorProbs;
        int T=curFrench.length;
        int numOfStates = curEnglish.length;
        double tmp;

        gamma.clear();
        for(int j=0; j<numOfStates;j++){
            for(int t=0;t<T;t++){
                int efCode = Utils.getEncoding(curEnglish[j], curFrench[t]);
                gamma.put(efCode, Double.NEGATIVE_INFINITY);
            }
        }
        for(int j=0; j<numOfStates;j++){
            for(int t=0;t<T;t++){
                int efCode = Utils.getEncoding(curEnglish[j], curFrench[t]);
                tmp = alpha[j][t] + beta[j][t] - sentProb;
                gamma.put(efCode, Utils.logAdd(gamma.get(efCode), tmp));
                //estimateEmmission.put(efCode, Double.NEGATIVE_INFINITY);
            }
        }
        for(int j=0; j<numOfStates;j++){
            for(int t=0;t<T;t++){
                int efCode = Utils.getEncoding(curEnglish[j], curFrench[t]);
                double prev = estimateEmmission.get(efCode);
                estimateEmmission.put(efCode, Utils.logAdd(prev, gamma.get(efCode) ) );

            }
        }
        
    }
    
        int[] computeSequence(int[] curEnglish, int[] curFrench, HMMParams curhmmparams) throws Exception {
            
            IntCounter curEmmissionProbs = curhmmparams.emmissionProbs;
            IntCounter curTransitionProbs = curhmmparams.transitionProbs;
            IntCounter priorProbabilities = curhmmparams.priorProbs;
            int T=curFrench.length;
            int numOfStates = curEnglish.length;
            //System.out.println("numof states= "+numOfStates);
            double dp[][] = new double[numOfStates][T];
            int backPointers[][] = new int[numOfStates][T];
            for(int s=0; s<numOfStates;s++){
                    dp[s][0]=  priorProbabilities.get(s)+
                            curEmmissionProbs.get(Utils.getEncoding(curEnglish[s], curFrench[0]));
                    backPointers[s][0] = -1;
            }
            
            for(int t=1;t<T;t++){
                    for(int s=0; s<numOfStates;s++){
                            dp[s][t] = Double.NEGATIVE_INFINITY;
                            backPointers[s][t]=-1;
                            for(int k=0; k<numOfStates;k++){
                                    double val =  dp[k][t-1]
                                +curTransitionProbs.get( Utils.getEncodingTransition(k, s, true) )
                                +curEmmissionProbs.get(Utils.getEncoding(curEnglish[s], curFrench[t])) ;
                                    if(val>dp[s][t]){
                                            dp[s][t]=val;
                                            backPointers[s][t]=k;
                                    }
                            }				
                    }
            }
		int ret[] = new int[T];
		int maxValIdx=-1; double maxVal=Double.NEGATIVE_INFINITY;
		for(int s=0; s<numOfStates; s++){
			if(dp[s][T-1]>maxVal){
				maxVal=dp[s][T-1];
				maxValIdx=s;
			}
		}
		int ctr=T-1,state=maxValIdx;
		while(ctr>=0){
			ret[ctr]=state;
			state=backPointers[state][ctr];
			ctr--;
		}
                //System.out.println(ret.toString());
		return ret;
            
        }
	
}
