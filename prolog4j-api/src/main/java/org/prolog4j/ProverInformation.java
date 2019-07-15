package org.prolog4j;

public class ProverInformation {
	private final String name;
	private final String id;

	public ProverInformation(String id, String name) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public String getId() {
		return this.id;
	}
}
