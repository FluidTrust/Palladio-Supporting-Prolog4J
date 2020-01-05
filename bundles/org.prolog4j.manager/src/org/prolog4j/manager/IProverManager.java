package org.prolog4j.manager;

import java.util.Map;

import org.prolog4j.IProverFactory;
import org.prolog4j.ProverInformation;

public interface IProverManager {
	
	Map<ProverInformation, IProverFactory> getProvers();
	
}
