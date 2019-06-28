/* 
 * Copyright (c) 2010 Miklos Espak
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.prolog4j.projog;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.projog.core.term.Atom;
import org.projog.core.term.DecimalFraction;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.ListFactory;
import org.projog.core.term.ListUtils;
import org.projog.core.term.Numeric;
import org.projog.core.term.Structure;
import org.projog.core.term.Term;
import org.projog.core.term.TermType;
import org.prolog4j.Compound;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Converter;

/**
 * tuProlog implementation of the conversion policy.
 */
public class ProjogConversionPolicy extends ConversionPolicy {

	/** Converts an Integer object to a term. */
	private static final Converter<Integer> INTEGER_CONVERTER = new Converter<Integer>() {
		@Override
		public Object convert(Integer i) {
			return new IntegerNumber(i);
		}
	};
	/** Converts a Long object to a term. */
	private static final Converter<Long> LONG_CONVERTER = new Converter<Long>() {
		@Override
		public Object convert(Long value) {
			return new IntegerNumber(value);
		}
	};
	/** Converts a Float object to a term. */
	private static final Converter<Float> FLOAT_CONVERTER = new Converter<Float>() {
		@Override
		public Object convert(Float value) {
			return new DecimalFraction(value);
		}
	};
	/** Converts a Double object to a term. */
	private static final Converter<Double> DOUBLE_CONVERTER = new Converter<Double>() {
		@Override
		public Object convert(Double value) {
			return new DecimalFraction(value);
		}
	};
	/** Converts a String object to a term. */
	private static final Converter<String> STRING_CONVERTER = new Converter<String>() {
		@Override
		public Object convert(String value) {
			return new Atom(value);
		}
	};
	/** Converts an alice.tuprolog.Int term to an Integer object. */
	private static final Converter<IntegerNumber> LONG_TERM_CONVERTER = new Converter<IntegerNumber>() {
		@Override
		public Object convert(IntegerNumber value) {
			return value.getLong();
		}
	};
	/** Converts an alice.tuprolog.Float term to a Float object. */
	private static final Converter<DecimalFraction> DOUBLE_TERM_CONVERTER = 
		new Converter<DecimalFraction>() {
		@Override
		public Object convert(DecimalFraction value) {
			return value.getDouble();
		}
	};

	/**
	 * Constructs a conversion policy for tuProlog.
	 */
	public ProjogConversionPolicy() {
		super();
		addObjectConverter(Long.class, LONG_CONVERTER);
		addObjectConverter(Float.class, FLOAT_CONVERTER);
		addObjectConverter(Double.class, DOUBLE_CONVERTER);
		addObjectConverter(Integer.class, INTEGER_CONVERTER);
		addObjectConverter(String.class, STRING_CONVERTER);
		addObjectConverter(Object[].class, new Converter<Object[]>() {
			@Override
			public Object convert(Object[] array) {
				List<Term> termList = Arrays.asList(array).stream().map(x -> (Term)convertObject(x)).collect(Collectors.toList());
				return ListFactory.createList(termList);
			}
		});
		addListConverter(List.class, new Converter<List<?>>() {
			@Override
			public Object convert(List<?> list) {
				List<Term> termList = list.stream().map(x -> (Term)convertObject(x)).collect(Collectors.toList());
				return ListFactory.createList(termList);
			}
		});
		addObjectConverter(Compound.class, new Converter<Compound>() {
			@Override
			public Object convert(Compound value) {
				String functor = value.getFunctor();
				Object[] args = value.getArgs();
				Term[] tArgs = new Term[value.getArity()];
				for (int i = 0; i < args.length; ++i) {
					tArgs[i] = (Term) convertObject(args[i]);
				}
				return Structure.createStructure(functor, tArgs);
			}
		});
		addObjectConverter(Term.class, new Converter<Term>() {
			@Override
			public Object convert(Term value) {
				return value;
			}
		});
		addTermConverter(IntegerNumber.class, LONG_TERM_CONVERTER);
		addTermConverter(DecimalFraction.class, DOUBLE_TERM_CONVERTER);
		addTermConverter(Structure.class, new Converter<Structure>() {
			@Override
			public Object convert(Structure value) {
				if (isAtom(value)) {
					return value.getName();
				}
				else if (isList(value)) {
					return ListUtils.toJavaUtilList(value);
				}
				int arity = value.getNumberOfArguments();
				Object[] args = new Object[arity];
				for (int i = 0; i < arity; ++i) {
					args[i] = convertTerm(value.getArgument(i).getTerm());
				}
				return new Compound(value.getName(), args);
			}

			@SuppressWarnings("unchecked")
			@Override
			public <R> R convert(Structure value, java.lang.Class<R> to) {
				if (isList(value) && Object[].class.isAssignableFrom(to)) {
					int length = value.getNumberOfArguments();
					R[] array = (R[]) Array.newInstance(to.getComponentType(), length);
					for (int i = 0; i < length; ++i) {
						Term t = value.getArgument(0);
						array[i] = (R) convertTerm(t.getTerm());
						value = (Structure) value.getArgument(1).getTerm();
					}
					return to.cast(array);
				}
				if (isAtom(value) && to == String.class) {
					return to.cast(value.getName());
				}
				return null;
			}
		});
	}
	
	private boolean isList(Term term) {
		return term.getType() == TermType.LIST || term.getType() == TermType.EMPTY_LIST;
	}

	@Override
	public boolean match(Object term1, Object term2) {
		if (term1 instanceof Term && term2 instanceof Term) {
			return ((Term) term1).strictEquality((Term) term2);
		}
		return false;
	}

	@Override
	public Object term(int value) {
		return new IntegerNumber(value);
	}

	@Override
	public Object term(double value) {
		return new DecimalFraction(value);
	}

	@Override
	public Object term(String name) {
		return null; // TODO
	}

	@Override
	public Object term(String name, Object... args) {
		return null; // TODO
	}

	@Override
	public int intValue(Object term) {
		return (int) ((Numeric) term).getLong();
	}

	@Override
	public double doubleValue(Object term) {
		return ((Numeric) term).getDouble();
	}

	@Override
	protected String getName(Object compound) {
		return ((Structure) compound).getName();
	}

	@Override
	protected int getArity(Object compound) {
		return ((Structure) compound).getNumberOfArguments();
	}


	@Override
	protected Object getArg(Object compound, int index) {
		return convertTerm(((Structure) compound).getArgument(index).getTerm());
	}

	@Override
	public boolean isAtom(Object term) {
		return ((Term) term).getType() == TermType.ATOM;
	}

	@Override
	public boolean isCompound(Object term) {
		return term instanceof Structure;
	}

	@Override
	public boolean isDouble(Object term) {
		return term instanceof DecimalFraction;
	}

	@Override
	public boolean isInteger(Object term) {
		return term instanceof IntegerNumber;
	}

}
