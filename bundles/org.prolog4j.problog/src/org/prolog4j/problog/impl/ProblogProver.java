package org.prolog4j.problog.impl;

import java.util.ArrayList;
import java.util.List;

import org.prolog4j.AbstractProver;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Query;

import de.tudresden.inf.lat.jproblog.JProblog;

public class ProblogProver extends AbstractProver {

	private static final long serialVersionUID = -1L;
	
	private JProblog jproblog;
	
	private List<String> knowledgeBase;
	
	public ProblogProver(ConversionPolicy conversionPolicy) {
		super(conversionPolicy);
		this.jproblog = new JProblog();
		this.knowledgeBase = new ArrayList<>();
		this.loadLibrary("lists");
	}

	@Override
	public Query query(String goal) {
		return new ProblogQuery(this, goal);
	}

	@Override
	public void loadLibrary(String library) {
		StringBuilder libraryBuilder = new StringBuilder();
		libraryBuilder.append(":- use_module(library(").append(library).append(")).");
		this.knowledgeBase.add(libraryBuilder.toString());
	}

	@Override
	public void addTheory(String theory) {
		this.knowledgeBase.add(theory);
	}

	@Override
	public void addTheory(String... theory) {
		for (String factOrRule : theory) {
			addTheory(factOrRule);
		}
	}
	
	public String combineKnowledgeBase() {
		StringBuilder sb = new StringBuilder();
		for(String factOrRule : this.knowledgeBase) {
			sb.append(factOrRule).append('\n');
		}
		
		return sb.toString();
	}

	public JProblog getJProblog() {
		return jproblog;
	}

}
