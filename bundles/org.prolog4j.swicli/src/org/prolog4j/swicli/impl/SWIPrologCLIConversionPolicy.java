package org.prolog4j.swicli.impl;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.xtext.parser.IParseResult;
import org.palladiosimulator.supporting.prolog.model.prolog.AtomicDouble;
import org.palladiosimulator.supporting.prolog.model.prolog.AtomicNumber;
import org.palladiosimulator.supporting.prolog.model.prolog.AtomicQuotedString;
import org.palladiosimulator.supporting.prolog.model.prolog.CompoundTerm;
import org.palladiosimulator.supporting.prolog.model.prolog.PrologFactory;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Expression;
import org.palladiosimulator.supporting.prolog.model.prolog.impl.AtomicDoubleImpl;
import org.palladiosimulator.supporting.prolog.model.prolog.impl.AtomicNumberImpl;
import org.palladiosimulator.supporting.prolog.model.prolog.impl.AtomicQuotedStringImpl;
import org.palladiosimulator.supporting.prolog.model.prolog.impl.CompoundTermImpl;
import org.palladiosimulator.supporting.prolog.model.prolog.impl.ListImpl;
import org.palladiosimulator.supporting.prolog.parser.antlr.PrologParser;
import org.prolog4j.Compound;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Converter;

public class SWIPrologCLIConversionPolicy extends ConversionPolicy {

    private static final PrologFactory FACTORY = PrologFactory.eINSTANCE;
    @SuppressWarnings("unchecked")
    private static final Class<Collection<?>> CLZ_COLLECTION = (Class<Collection<?>>) ((Class<?>) Collection.class);
    private final PrologParser parser;

    public SWIPrologCLIConversionPolicy(PrologParser parser) {
        this.parser = parser;

        addObjectConverter(String.class, new Converter<String>() {
            @Override
            public Object convert(String value) {
                AtomicQuotedString term = FACTORY.createAtomicQuotedString();
                term.setValue(value.replaceFirst("'([^']+)'", "$1"));
                return term;
            }
        });

        addObjectConverter(Integer.class, new Converter<Integer>() {
            @Override
            public Object convert(Integer value) {
                return term(value);
            }
        });

        addObjectConverter(Long.class, new Converter<Long>() {
            @Override
            public Object convert(Long value) {
                return term(value.intValue());
            }
        });

        addObjectConverter(Double.class, new Converter<Double>() {
            @Override
            public Object convert(Double value) {
                return term(value);
            }
        });

        addObjectConverter(CLZ_COLLECTION, new Converter<Collection<?>>() {
            @Override
            public Object convert(Collection<?> value) {
                var listTerm = FACTORY.createList();
                for (var item : value) {
                    listTerm.getHeads()
                        .add((Expression) convertObject(item));
                }
                return listTerm;
            }
        });

        addObjectConverter(Compound.class, new Converter<Compound>() {
            @Override
            public Object convert(Compound value) {
                var term = FACTORY.createCompoundTerm();
                term.setValue(value.getFunctor());
                for (var arg : value.getArgs()) {
                    term.getArguments()
                        .add((Expression) convertObject(arg));
                }
                return term;
            }
        });

        addTermConverter(AtomicNumberImpl.class, new Converter<AtomicNumberImpl>() {
            @Override
            public Object convert(AtomicNumberImpl term) {
                return (long) term.getValue();
            }
        });

        addTermConverter(AtomicDoubleImpl.class, new Converter<AtomicDoubleImpl>() {
            @Override
            public Object convert(AtomicDoubleImpl term) {
                return term.getValue();
            }
        });

        addTermConverter(AtomicQuotedStringImpl.class, new Converter<AtomicQuotedStringImpl>() {
            @Override
            public Object convert(AtomicQuotedStringImpl term) {
                return term.getValue();
            }
        });

        addTermConverter(ListImpl.class, new Converter<ListImpl>() {
            @Override
            public Object convert(ListImpl term) {
                var result = new ArrayList<>();
                for (Expression headTerm : term.getHeads()) {
                    result.add(convertTerm(headTerm));
                }
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <R> R convert(ListImpl object, Class<R> to) {
                if (to.isArray()) {
                    var arrayType = to.getComponentType();
                    var destinationArray = (Object[]) Array.newInstance(arrayType, object.getHeads()
                        .size());
                    var array = ((List<?>) convert(object)).toArray(destinationArray);
                    return (R) array;
                }
                return super.convert(object, to);
            }

        });

        addTermConverter(CompoundTermImpl.class, new Converter<CompoundTermImpl>() {
            @Override
            public Object convert(CompoundTermImpl term) {
                if (!term.getArguments()
                    .isEmpty()) {
                    var arguments = term.getArguments()
                        .stream()
                        .map(SWIPrologCLIConversionPolicy.this::convertTerm)
                        .collect(Collectors.toList());
                    return new Compound(term.getValue(), arguments.toArray());
                }
                return term.getValue();
            }
        });
    }

    @Override
    public boolean match(Object term1, Object term2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInteger(Object term) {
        return term instanceof AtomicNumber;
    }

    @Override
    public boolean isDouble(Object term) {
        return term instanceof AtomicDouble;
    }

    @Override
    public boolean isAtom(Object term) {
        if (term instanceof AtomicQuotedString || term instanceof AtomicNumber) {
            return true;
        }
        if (term instanceof CompoundTerm) {
            return ((CompoundTerm) term).getArguments()
                .isEmpty();
        }
        return false;
    }

    @Override
    public boolean isCompound(Object term) {
        if (term instanceof CompoundTerm) {
            return !((CompoundTerm) term).getArguments()
                .isEmpty();
        }
        return false;
    }

    @Override
    public Object term(int value) {
        AtomicNumber term = FACTORY.createAtomicNumber();
        term.setValue(value);
        return term;
    }

    @Override
    public Object term(double value) {
        AtomicDouble term = FACTORY.createAtomicDouble();
        term.setValue(value);
        return term;
    }

    @Override
    public Object term(String name) {
        // this should parse the text as far as the tests specify it...
        IParseResult result = parser.parse(parser.getGrammarAccess()
            .getTermRule(), new StringReader(name));
        if (result.hasSyntaxErrors()) {
            return null;
        }
        return result.getRootASTElement();
//        
//        AtomicQuotedString term = FACTORY.createAtomicQuotedString();
//        term.setValue(name);
//        return term;
    }

    @Override
    public Object term(String pattern, Object... args) {
        var newPattern = pattern;
        var variables = new ArrayList<String>();
        for (int i = newPattern.indexOf("?"); i >= 0 && i < newPattern.length(); i = newPattern.indexOf("?", i)) {
            String variableName = "PROLOG1561_" + variables.size();
            variables.add(variableName);
            newPattern = newPattern.substring(0, i) + variableName + newPattern.substring(i + 1);
            i = i + variableName.length() - 1;
        }

//        IParseResult parseResult = parser.doParse(newPattern);
//        CompoundTerm patternTerm = (CompoundTerm)parseResult.getRootASTElement();
//        for (TreeIterator<EObject> iter = patternTerm.eAllContents(); iter.hasNext();) {
//            var part = iter.next();
//            if (part instanceof CompoundTerm) {
//                CompoundTerm term = (CompoundTerm) part;
//                if (variables.contains(term.getValue())) {
//                    var replacement = args[variables.indexOf(term.getValue())];
//                    var replacementTerm = term(replacement);
//                }
//            }
//        }

        for (int i = 0; i < args.length; ++i) {
            Object arg = args[i];
            String replacement = null;
            if (arg instanceof String) {
                replacement = String.format("'%s'", arg);
            } else if (arg instanceof Number) {
                replacement = String.format("%d", arg);
            }
            newPattern = newPattern.replace(variables.get(i), replacement);
        }

        IParseResult parseResult = parser.parse(parser.getGrammarAccess()
            .getTermRule(), new StringReader(newPattern));
        if (parseResult.hasSyntaxErrors()) {
            throw new IllegalArgumentException();
        }

        return parseResult.getRootASTElement();
    }

    @Override
    public int intValue(Object term) {
        if (term instanceof AtomicNumber) {
            return ((AtomicNumber) term).getValue();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public double doubleValue(Object term) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getName(Object compound) {
        if (compound instanceof CompoundTerm) {
            return ((CompoundTerm) compound).getValue();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int getArity(Object compound) {
        if (compound instanceof CompoundTerm) {
            return ((CompoundTerm) compound).getArguments()
                .size();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Object getArg(Object compound, int index) {
        if (compound instanceof CompoundTerm) {
            List<Expression> arguments = ((CompoundTerm) compound).getArguments();
            if (arguments.size() > index) {
                return convertTerm(arguments.get(index));
            }
        }
        throw new IllegalArgumentException();
    }

}
