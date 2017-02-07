/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.util.IntCounter;

/**
 *
 * @author harsh
 */
public class HMMParams {
    IntCounter transitionProbs;
    IntCounter emmissionProbs;
    IntCounter priorProbs;
    IntCounter lastStateProbs;
        
    public HMMParams(){
        transitionProbs = new IntCounter();
        emmissionProbs = new IntCounter();
        priorProbs = new IntCounter();
        lastStateProbs = new IntCounter();
    }

    public IntCounter getTransitionProbs() {
        return transitionProbs;
    }

    public void setTransitionProbs(IntCounter transitionProbs) {
        this.transitionProbs = transitionProbs;
    }

    public IntCounter getEmmissionProbs() {
        return emmissionProbs;
    }

    public void setEmmissionProbs(IntCounter emmissionProbs) {
        this.emmissionProbs = emmissionProbs;
    }

    public IntCounter getPriorProbs() {
        return priorProbs;
    }

    public void setPriorProbs(IntCounter priorProbs) {
        this.priorProbs = priorProbs;
    }


    void clearAll() {
        emmissionProbs.clear();
        transitionProbs.clear();
        priorProbs.clear();
        lastStateProbs.clear();
    }
}
