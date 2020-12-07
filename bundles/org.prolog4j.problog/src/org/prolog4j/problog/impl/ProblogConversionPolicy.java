package org.prolog4j.problog.impl;

import org.palladiosimulator.supporting.prolog.model.prolog.impl.AtomicQuotedStringImpl;
import org.palladiosimulator.supporting.prolog.parser.antlr.PrologParser;
import org.prolog4j.Converter;
import org.prolog4j.base.MetaModelBasedConversionPolicy;

/**
 * This is the conversion policy for the problog integration
 * 
 * @author Nicolas Boltz
 */
public class ProblogConversionPolicy extends MetaModelBasedConversionPolicy {
	
	public ProblogConversionPolicy(PrologParser parser) {
		super(parser);
		
		//TODO: Strangely under windows this always adds a ' at the beginning of the string
		// so the substring command was used when developing under windows...
        addTermConverter(AtomicQuotedStringImpl.class, new Converter<AtomicQuotedStringImpl>() {
            @Override
            public Object convert(AtomicQuotedStringImpl term) {
                return term.getValue();//.substring(1);
            }
        });
	}
}
