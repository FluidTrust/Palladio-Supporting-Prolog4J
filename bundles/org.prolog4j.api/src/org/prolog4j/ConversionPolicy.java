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
package org.prolog4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An instance of this class represents how terms are converted to regular Java
 * objects (POJOs) and vice versa. The transformation is performed by
 * converters. The implementations of the Prolog4J API should provide a fully
 * functional instance of this class.
 * <p>
 * Prolog terms are converted to regular Java object by <i>term converters</i>.
 * Unbound variables are always converted to <tt>null</tt>. For bound variables
 * their value is converted. Terms representing numbers are always converted to
 * an instance of <tt>java.lang.Number</tt>.
 * <p>
 * For other terms the default way of conversion can be redefined. The default
 * conversion is the following. Atoms are converted to strings, lists to
 * <code>java.util.List</code> objects and other compound terms to
 * {@link Compound} objects.
 * <p>
 * You can redefine this behavior by registering your own term converter to the
 * policy. Term converters can be assigned to a string. If an atom with the same
 * name or a compound term with the same functor has to be converted then this
 * custom converter will be used. Otherwise, the default converter will be used.
 * <p>
 * The conversion of objects to terms is performed in a similar way. <tt>null
 * </tt> is converted to a new, unbound variable. Other objects will be
 * converted to ground terms. <tt>java.lang.Number</tt> objects are converted to
 * Prolog numbers, <tt>java.lang.String</tt> objects to atoms and
 * <tt>java.util.List</tt> objects to Prolog lists. For other objects there is
 * no default way of conversion.
 * <p>
 * You can add your own <i>object converter</i> to the policy assigning it to a
 * class. If an object has to be converted to a term, first a converter assigned
 * to its class is looked for. If not found, then a converter assigned to its
 * superclasses and the implemented interfaces are looked for, recursively. If
 * none found and there is no default way of conversion, then an exception is
 * thrown.
 * 
 * @see Converter
 */
public abstract class ConversionPolicy {

	/**
	 * Stores the converters for transforming terms to regular objects. The keys of
	 * the map are the patterns. If a pattern matches a term then its converter can
	 * be applied.
	 */
	private HashMap<Class<?>, Converter<?>> termConverters = new HashMap<Class<?>, Converter<?>>();

	/**
	 * Represents the reverse insertion order of the keys to
	 * {@link #termConverters}. (Note that LinkedHashMap is not suitable for two
	 * reasons. At first, it cannot be iterated through reversely, at second the
	 * insertion order is not affected if a key is re-inserted into the map.)
	 */
	private Map<String, Converter<Object>> termPatterns = new HashMap<String, Converter<Object>>();

	/**
	 * Stores the converters for transforming regular objects to terms. The keys of
	 * the map are the patterns. If a pattern matches an object then its converter
	 * can be applied.
	 */
	private HashMap<Class<?>, Converter<?>> objectConverters = new HashMap<Class<?>, Converter<?>>();

	/**
	 * Constructs an empty <code>ConversionPolicy</code>.
	 */
	protected ConversionPolicy() {
	}

	/**
	 * Registers a new term converter into the policy. The converter will be used to
	 * convert terms of the specified class to objects. This method is intended to
	 * be used by the implementation of the conversion policy specific to a
	 * particular Prolog implementation.
	 * 
	 * @param <T>       the class of the terms to convert
	 * @param class_    the object that represents the given type
	 * @param converter the converter
	 */
	protected <T> void addTermConverter(Class<T> class_, Converter<T> converter) {
		termConverters.put(class_, converter);
	}

	/**
	 * Registers a new term converter into the policy. The converter will be used
	 * for atoms having the given name and compound terms having the given functor.
	 * If a converter has already been assigned to the functor, the new converter
	 * will override it.
	 * 
	 * @param functor   the functor to select the converter to use later
	 * @param converter the converter
	 */
	public void addTermConverter(String functor, Converter<Object> converter) {
		termPatterns.put(functor, converter);
	}

	/**
	 * Registers a new object converter into the policy. The converter will be used
	 * to convert objects of the specified type to terms (subtypes included). If a
	 * converter has already been assigned to the type, the new converter will
	 * override it.
	 * 
	 * @param <T>       the type of the terms to convert
	 * @param class_    the object that represents the given type
	 * @param converter the converter
	 */
	public <T> void addObjectConverter(Class<T> class_, Converter<T> converter) {
		objectConverters.put(class_, converter);
	}
	
	public <T> void addListConverter(Class<T> class_, Converter<List<?>> converter) {
		objectConverters.put(class_, converter);
	}

	/**
	 * Converts a term to a regular Java object. If the term is <code>null
	 * </code> (that represents an unbound Prolog variable) then <code>null</code>
	 * is returned.
	 * <p>
	 * The method starts to match the term with the patterns of the term converters
	 * in their reverse insertion order. If the term matches one then it will be
	 * converted by the converter assigned to the pattern. If the converter returns
	 * <code>null</code> then another applicable converter will be looked for.
	 * <p>
	 * An exception is thrown if there is no applicable converter. (The
	 * implementations of the Prolog4J API should prevent this situation.)
	 * 
	 * @param term the term to convert
	 * @return the result of the conversion
	 */
	public <T> Object convertTerm(T term) {
		if (term == null) {
			return null;
		}
		if (isCompound(term)) {
			String compoundName = getName(term);
			if (termPatterns.containsKey(compoundName)) {
				Object result = termPatterns.get(compoundName).convert(term);
				if (result != null) {
					return result;
				}
			}
		}
		Class<?> termClass = term.getClass();
		do {
			if (termConverters.containsKey(termClass)) {
				@SuppressWarnings("unchecked")
				Object result = ((Converter<T>)termConverters.get(termClass)).convert(term);
				if (result != null) {
					return result;
				}
			}
			termClass = termClass.getSuperclass();
		} while (termClass != null);
		throw new RuntimeException("No suitable converter found for " + term);
	}

	/**
	 * Converts a term to a regular Java object. If the term is <code>null
	 * </code> (that represents an unbound Prolog variable) then <code>null</code>
	 * is returned.
	 * <p>
	 * The method starts to match the term with the patterns of the term converters
	 * in their reverse insertion order. If the term matches one then it will be
	 * converted by the converter assigned to the pattern. If the converter returns
	 * <code>null</code> then another applicable converter will be looked for.
	 * <p>
	 * An exception is thrown if there is no applicable converter. (The
	 * implementations of the Prolog4J API should prevent this situation.)
	 * 
	 * @param <T>  the type to convert to
	 * @param term the term to convert
	 * @param type the type to convert to
	 * @return the result of the conversion
	 */
	public <U,T> T convertTerm(U term, Class<T> type) {
		if (term == null) {
			return null;
		}
		Class<?> termClass = term.getClass();
		do {
			@SuppressWarnings("unchecked")
			Converter<U> converter = (Converter<U>) termConverters.get(termClass);
			if (converter != null) {
				return (T) converter.convert(term, type);
			}
			termClass = termClass.getSuperclass();
		} while (termClass != null);
		throw new RuntimeException("No suitable converter found for " + term);
	}

	/**
	 * Converts a regular Java object to a term. The <code>null</code> value will be
	 * converted to <code>null</code> (that represents an unbound Prolog variable).
	 * <p>
	 * The method starts to match the class of the object with the patterns of the
	 * object converters in their reverse insertion order. If the class is the
	 * subclass of a pattern (equality is allowed) then the object will be converted
	 * by the converter assigned to the pattern. If the converter returns
	 * <code>null</code> then another applicable converter will be looked for.
	 * <p>
	 * An exception is thrown if there is no applicable converter. (The
	 * implementations of the Prolog4J API should prevent this situation.)
	 * 
	 * @param object the object to convert
	 * @return the result of the conversion
	 */
	public Object convertObject(Object object) {
		if (object == null) {
			return null;
		}
		Class<?> objectClass = object.getClass();
		if (objectClass.isArray()) {
			@SuppressWarnings("unchecked")
			Converter<Object> converter = (Converter<Object>) objectConverters.get(Object[].class);
			return converter.convert(object);
		} else {
			do {
				@SuppressWarnings("unchecked")
				Converter<Object> converter = (Converter<Object>) objectConverters.get(objectClass);
				if (converter != null) {
					return converter.convert(object);
				}
				if (!objectClass.isArray()) {
					for (Class<?> interf : objectClass.getInterfaces()) {
						@SuppressWarnings("unchecked")
						Converter<Object> converter1 = (Converter<Object>) objectConverters.get(interf);
						if (converter1 != null) {
							return converter1.convert(object);
						}
					}
					objectClass = objectClass.getSuperclass();
				} else {
					// TODO
				}
			} while (objectClass != null);
		}
		throw new RuntimeException("No suitable converter found for " + object);
	}

	/**
	 * Decides whether two terms match or not. The comparison is similar to
	 * {@link Object#equals(Object)} apart of that <code>null</code> matches any
	 * object.
	 * 
	 * @param term1 the first term
	 * @param term2 the second object
	 * @return <code>true</code> if the terms match, otherwise <code>false</code>
	 */
	public abstract boolean match(Object term1, Object term2);

	/**
	 * Determines whether the Prolog term is an integer value.
	 * 
	 * @param term the term
	 * @return <code>true</code> if the value is integer, otherwise
	 *         <code>false</code>
	 */
	public abstract boolean isInteger(Object term);

	/**
	 * Determines whether the Prolog term is a double value.
	 * 
	 * @param term the term
	 * @return <code>true</code> if the value is double, otherwise
	 *         <code>false</code>
	 */
	public abstract boolean isDouble(Object term);

	/**
	 * Determines whether the Prolog term is an atom.
	 * 
	 * @param term the term
	 * @return <code>true</code> if the value is integer, otherwise
	 *         <code>false</code>
	 */
	public abstract boolean isAtom(Object term);

	/**
	 * Determines whether the Prolog term is compound. Atoms are regarded as
	 * compound values with 0 arity.
	 * 
	 * @param term the term
	 * @return <code>true</code> if the value is compound, otherwise
	 *         <code>false</code>
	 */
	public abstract boolean isCompound(Object term);

	/**
	 * Creates a integer term according to the actual implementation.
	 * 
	 * @param value the integer value
	 * @return the created Prolog integer term
	 */
	public abstract Object term(int value);

	/**
	 * Creates a real term according to the actual implementation.
	 * 
	 * @param value the double value
	 * @return the created Prolog real term
	 */
	public abstract Object term(double value);

	/**
	 * Creates an atom according to the actual implementation.
	 * 
	 * @param name the name of the atom
	 * @return the created atom
	 */
	public abstract Object term(String name);

	/**
	 * Creates a compound term according to the actual implementation.
	 * 
	 * @param pattern the pattern
	 * @param args    the arguments of the pattern
	 * @return the created term
	 */
	public abstract Object term(String pattern, Object... args);

	protected class TermPattern {
		public String pattern;
		public List<String> placeholderNames;

		public TermPattern(String pattern, List<String> placeholderNames) {
			this.pattern = pattern;
			this.placeholderNames = placeholderNames;
		}
	}

	protected TermPattern tp(String pattern, Object... args) {
		ArrayList<String> placeholderNames = new ArrayList<String>();
		StringBuilder goalB = new StringBuilder(pattern);
		String newVarPrefix = null;
		for (int i = 0, start = 0; true; ++i) {
			start = goalB.indexOf("?", start);
			if (start == -1) {
				break;
			}
			if (start < goalB.length() - 1 && goalB.charAt(start + 1) == '?') {
				goalB.deleteCharAt(start);
				++start;
				continue;
			}
			int end = start + 1;
			while (end < goalB.length() && Character.isLetterOrDigit(goalB.charAt(end))) {
				++end;
			}
			if (end == start + 1) {
				if (newVarPrefix == null) {
					newVarPrefix = findNewVarPrefix(pattern);
				}
				String variable = newVarPrefix + i;
				placeholderNames.add(variable);
				goalB.replace(start, start + 1, variable);
			} else {
				placeholderNames.add(goalB.substring(start + 1, end));
				goalB.delete(start, start + 1);
			}
			start = end;
		}
		placeholderNames.trimToSize();
		return new TermPattern(goalB.toString(), placeholderNames);
	}

	/**
	 * Creates a new variable name that does not occurs in the pattern.
	 * 
	 * @param pattern the pattern
	 * @return a new, not conflicting variable name
	 */
	private String findNewVarPrefix(final String pattern) {
		if (!pattern.contains("P4J_")) {
			return "P4J_";
		}
		for (int i = 0; true; ++i) {
			String s = String.valueOf(i);
			if (!pattern.contains(s)) {
				return "P4J_" + s;
			}
		}
	}

	/**
	 * Converts an integer term to an int value.
	 * 
	 * @param term a term representing an integer value
	 * @return the int value of the term
	 */
	public abstract int intValue(Object term);

	/**
	 * Converts a floating point term to a double value.
	 * 
	 * @param term a term representing a floating point value
	 * @return the double value of the term
	 */
	public abstract double doubleValue(Object term);

	/**
	 * Returns the functor of a compound term or the name of an atom.
	 * 
	 * @param compound the compound term or atom
	 * @return the functor of the term
	 */
	protected abstract String getName(Object compound);

	/**
	 * Returns the arity of a compound term or atom. For atoms it returns zero.
	 * 
	 * @param compound the compound term or atom
	 * @return the arity of the term
	 */
	public abstract int getArity(Object compound);

	/**
	 * Returns an argument of a compound term. The numbering starts from zero.
	 * 
	 * @param compound the compound
	 * @param index    the index of the argument (>= 0)
	 * @return the <tt>arg</tt>th argument of the compound
	 */
	public abstract Object getArg(Object compound, int index);

}
