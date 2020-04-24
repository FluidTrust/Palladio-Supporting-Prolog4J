package org.prolog4j.tuprolog.impl.libraries;

import java.io.IOException;

import alice.tuprolog.Theory;
import alice.tuprolog.lib.TheoryLibrary;

public class ListsLibrary extends TheoryLibrary {

    private static final long serialVersionUID = -999830997395506838L;

    public ListsLibrary() throws IOException {
        super("lists", createTheory());
    }

    static protected Theory createTheory() throws IOException {
        return new Theory(ListsLibrary.class.getResourceAsStream("lists.pl"));
    }
}
