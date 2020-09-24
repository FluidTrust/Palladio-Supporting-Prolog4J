package org.prolog4j.problog.impl;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.IParseResult;
import org.palladiosimulator.supporting.prolog.model.prolog.CompoundTerm;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Expression;
import org.palladiosimulator.supporting.prolog.parser.antlr.PrologParser;
import org.palladiosimulator.supporting.prolog.services.PrologGrammarAccess;
import org.prolog4j.Compound;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Converter;
import org.prolog4j.problog.util.PrologToolProvider;

public class ProblogConversionPolicy extends ConversionPolicy {
	
	private Map<String, Converter<Object>> customTermConverters = new HashMap<String, Converter<Object>>();
	
	/** Converts an Integer object to a term. */
	private static final Converter<Integer> INTEGER_CONVERTER = new Converter<Integer>() {
		@Override
		public Object convert(Integer i) {
			return Integer.toString(i);
		}
	};
	/** Converts a Long object to a term. */
	private static final Converter<Long> LONG_CONVERTER = new Converter<Long>() {
		@Override
		public Object convert(Long value) {
			return Long.toString(value);
		}
	};
	/** Converts a Float object to a term. */
	private static final Converter<Float> FLOAT_CONVERTER = new Converter<Float>() {
		@Override
		public Object convert(Float value) {
			return Float.toString(value);
		}
	};
	/** Converts a Double object to a term. */
	private static final Converter<Double> DOUBLE_CONVERTER = new Converter<Double>() {
		@Override
		public Object convert(Double value) {
			return Double.toString(value);
		}
	};
	/** Converts a String object to a term. */
	private static final Converter<String> STRING_CONVERTER = new Converter<String>() {
		@Override
		public Object convert(String value) {
			return "'" + value + "'";
		}
	};
	
	private static final Converter<List<?>> LIST_CONVERTER = new Converter<List<?>>() {
		@Override
		public Object convert(List<?> list) {
			StringBuilder builder = new StringBuilder();
			builder.append("[");
			Iterator<?> it = list.iterator();
			while(it.hasNext()) {
				builder.append(it.next().toString());
				if(it.hasNext()) {
					builder.append(",");
				}
			}
			return builder.append("]").toString();
		}
	};
	
	public ProblogConversionPolicy() {
		super();
		addObjectConverter(Long.class, LONG_CONVERTER);
		addObjectConverter(Float.class, FLOAT_CONVERTER);
		addObjectConverter(Double.class, DOUBLE_CONVERTER);
		addObjectConverter(Integer.class, INTEGER_CONVERTER);
		addObjectConverter(String.class, STRING_CONVERTER);
		addListConverter(List.class, LIST_CONVERTER);
		addObjectConverter(Compound.class, new Converter<Compound>() {
			@Override
			public Object convert(Compound value) {
				return value.toString();
			}
		});
		
		// Terms are only represented as Strings, so no real conversion as to be done.
		addTermConverter(String.class, new Converter<String>() {
			@Override
			public Object convert(String value) {
				EObject root = getRootObject(value);
				if(root != null && root instanceof CompoundTerm) {
					CompoundTerm elements = (CompoundTerm) root;
					String functor = elements.getValue();
					Converter<Object> customConverter = ProblogConversionPolicy.this.customTermConverters.get(functor);
					if(customConverter != null) {
						return customConverter.convert(value);
					}
				}
				
				//list
				if(value.startsWith("[") && value.endsWith("]")) {
					value = value.substring(1, value.length()-1);
					String[] content = value.split(",\\s*");
					List<Object> list = new ArrayList<Object>();
					for(String str : content) {
						list.add(this.convert(str));
					}
					return list;
				} else { //number or string
					try {
						long longValue = Long.parseLong(value);
				        return longValue;
				    } catch (NumberFormatException nfe) {
				    	try {
				    		double doubleValue = Double.parseDouble(value);
					        return doubleValue;
					    } catch (NumberFormatException nfe2) {
					    	return value;
					    }
				    }
				}
			}
		});
	}
	
	@Override
	public void addTermConverter(String functor, Converter<Object> converter) {
		customTermConverters.put(functor, converter);
		super.addTermConverter(functor, converter);
	}

	@Override
	public boolean match(Object term1, Object term2) {
		
		return false;
	}

	@Override
	public boolean isInteger(Object term) {
		return term instanceof Integer;
	}

	@Override
	public boolean isDouble(Object term) {
		return term instanceof Double;
	}

	@Override
	public boolean isAtom(Object term) {
		return false;
	}

	@Override
	public boolean isCompound(Object term) {
		return false;
	}

	@Override
	public Object term(int value) {
		return INTEGER_CONVERTER.convert(value);
	}

	@Override
	public Object term(double value) {
		return DOUBLE_CONVERTER.convert(value);
	}

	@Override
	public Object term(String name) {
		return STRING_CONVERTER.convert(name);
	}

	@Override
	public Object term(String pattern, Object... args) {
		for(int i = 0; i < args.length && pattern.contains("?"); ++i) {
			String argument = (String) this.convertObject(args[i]);
			pattern = pattern.replaceFirst("\\?", argument);
		}

		return STRING_CONVERTER.convert(pattern);
	}

	@Override
	public int intValue(Object term) {
		return ((int) term);
	}

	@Override
	public double doubleValue(Object term) {
		return ((double) term);
	}

	@Override
	protected String getName(Object compound) {
		return compound.toString();
	}

	@Override
	public int getArity(Object compound) {
		EObject root = getRootObject((String) compound);
		if(root != null && root instanceof CompoundTerm) {
			CompoundTerm elements = (CompoundTerm) root;
			return elements.getArguments().size();
		}

		return 0;
	}

	@Override
	public Object getArg(Object compound, int index) {
		EObject root = getRootObject((String) compound);
		if(root != null && root instanceof CompoundTerm) {
			CompoundTerm elements = (CompoundTerm) root;
			if(index < elements.getArguments().size()) {
				Expression expr = elements.getArguments().get(index);
				if(expr instanceof CompoundTerm) {
					CompoundTerm argument = (CompoundTerm) expr;
					return argument.getValue();
				}
				
			}
		}

		return null;
	}
	
	//todo: zwei mal vorhanden (in problogquery) gemeinsame klasse für zugriff auf den parser!!!
	private EObject getRootObject(String query) {
		PrologToolProvider toolProvider = new PrologToolProvider();
		PrologParser parser = toolProvider.getParser();
		PrologGrammarAccess grammar = toolProvider.getGrammarAccess();
		Reader targetReader = new StringReader(query);
		IParseResult result = parser.parse(grammar.getExpression_1100_xfyRule(), targetReader);

		return result.getRootASTElement();
	}

}
