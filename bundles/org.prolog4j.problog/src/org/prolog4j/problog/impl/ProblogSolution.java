package org.prolog4j.problog.impl;

import java.util.Collection;
import java.util.List;

import org.prolog4j.Solution;

public class ProblogSolution<S> extends Solution<S> {
	
	private String rawSolutions;
	
	public ProblogSolution(String solutions) {
		this.rawSolutions = solutions;
	}

	@Override
	public boolean isSuccess() {
		return !rawSolutions.isEmpty();
	}

	@Override
	public <A> A get(String variable) {
		
		return null;
	}

	@Override
	public <A> A get(String variable, Class<A> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean fetch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void collect(Collection<?>... collections) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<?>[] toLists() {
		// TODO Auto-generated method stub
		return null;
	}

}
