package org.prolog4j.swicli.impl;

import java.util.Collection;
import java.util.List;

import org.prolog4j.Solution;
import org.prolog4j.UnknownVariableException;

public class SWIPrologCLIFailedSolution<A> extends Solution<A> {

    public SWIPrologCLIFailedSolution(String resultString, String syntaxErrors) {
        System.err.println("Errors while parsing result string:");
        System.err.println(syntaxErrors);
        System.err.println("Result string:");
        System.err.println(resultString);
    }
    
    public SWIPrologCLIFailedSolution(Exception e) {
        e.printStackTrace();
    }
    
    public SWIPrologCLIFailedSolution() {
        
    }
    
    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    protected boolean fetch() {
        return false;
    }

    @Override
    public void collect(Collection<?>... collections) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<?>[] toLists() {
        return null;
    }

    @SuppressWarnings("hiding")
    @Override
    public <A> A get(String variable) {
        throw new UnknownVariableException(variable);
    }

    @SuppressWarnings("hiding")
    @Override
    public <A> A get(String variable, Class<A> type) {
        throw new UnknownVariableException(variable);
    }

}
