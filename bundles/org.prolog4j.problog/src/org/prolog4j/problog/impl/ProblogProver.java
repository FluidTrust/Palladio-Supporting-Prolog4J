package org.prolog4j.problog.impl;

import java.util.ArrayList;
import java.util.List;

import org.prolog4j.AbstractProver;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Query;
import org.prolog4j.base.PrologAPIWrapper;
import org.prolog4j.problog.ProblogExecutable;

import de.tudresden.inf.lat.jproblog.JProblog;

public class ProblogProver extends AbstractProver {

	private static final long serialVersionUID = -1L;
	
	private JProblog jproblog;
	
	private List<String> knowledgeBase;
	
	private PrologAPIWrapper prologAPIWrapper;
	private final ProblogExecutable executable;
	
	public ProblogProver(PrologAPIWrapper prologAPIWrapper, ConversionPolicy conversionPolicy, ProblogExecutable executable) {
		super(conversionPolicy);
		this.jproblog = new JProblog();
		this.knowledgeBase = new ArrayList<>();
		this.prologAPIWrapper = prologAPIWrapper;
		this.executable = executable;
		
		this.loadLibrary("lists");
		this.loadLibrary("assert");
	}

	@Override
	public Query query(String goal) {
		return new ProblogQuery(this, prologAPIWrapper, goal, executable);
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
	
	@Override
	public void assertz(String fact, Object... args) {
		knowledgeBase.add("query(assertz(" + fact.substring(0, fact.lastIndexOf('.')) + ")).");
	}

	@Override
	public void retract(String fact) {
		int lastDot = fact.lastIndexOf('.');
		int length = fact.length();
		if (lastDot == -1 || fact.substring(lastDot, length).trim().length() > 1) {
			lastDot = length;
		}
		knowledgeBase.add("query(retract(" + fact.substring(0, lastDot) + ")).");
	}
}
