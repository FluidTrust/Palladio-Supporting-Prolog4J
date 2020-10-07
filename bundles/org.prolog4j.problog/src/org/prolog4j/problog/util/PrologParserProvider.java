package org.prolog4j.problog.util;

import java.util.function.Supplier;

import org.palladiosimulator.supporting.prolog.parser.antlr.PrologParser;

import com.google.inject.Inject;

/**
 * Not needed? Delete?
 *
 */
public class PrologParserProvider implements Supplier<PrologParser> {

    @Inject
    private PrologParser parser;

    @Override
    public PrologParser get() {
        return parser;
    }
}