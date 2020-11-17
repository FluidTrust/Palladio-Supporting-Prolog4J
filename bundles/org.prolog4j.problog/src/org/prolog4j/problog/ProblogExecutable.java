package org.prolog4j.problog;

import org.prolog4j.base.Executable;

public interface ProblogExecutable extends Executable {
	
	public String execute(String problogProgram);

}
