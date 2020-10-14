package org.prolog4j.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.palladiosimulator.supporting.prolog.model.prolog.Term;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Query;
import org.prolog4j.Solution;

/**
 * Class for replacing the placeholders in a query with the given arguments.
 * 
 * @author Stephan Seifermann
 * @author Nicolas Boltz
 */
public class QueryReplacer {

    protected static class NestedQuery extends Query {

        private final Map<String, String> replacements = new HashMap<>();
        private final ConversionPolicy conversionPolicy;
        private final PrologAPIWrapper prologAPIWrapper;

        public NestedQuery(String goalPattern, ConversionPolicy conversionPolicy, PrologAPIWrapper prologAPIWrapper) {
            super(goalPattern);
            this.conversionPolicy = conversionPolicy;
            this.prologAPIWrapper = prologAPIWrapper;
        }

        @Override
        public <A> Solution<A> solve(Object... actualArgs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Query bind(int argument, Object value) {
            var placeholderName = getPlaceholderNames().get(argument);
            registerReplacement(value, placeholderName);
            return this;
        }

        @Override
        public Query bind(String variable, Object value) {
            if (!getPlaceholderNames().contains(variable)) {
                throw new IllegalArgumentException();
            }
            registerReplacement(value, variable);
            return this;
        }

        public String buildGoalString(Object... actualArgs) {
            if (getPlaceholderNames().size() - replacements.size() != actualArgs.length) {
                throw new IllegalArgumentException();
            }

            HashMap<String, String> temporaryReplacements = new HashMap<String, String>(replacements);

            for (int i = 0; i < actualArgs.length; ++i) {
                String placeholderName = getNextUnboundPlaceholder(temporaryReplacements);
                registerReplacement(actualArgs[i], placeholderName, temporaryReplacements);
            }

            String newGoal = getGoal();
            for (Entry<String, String> replacement : temporaryReplacements.entrySet()) {
                newGoal = newGoal.replaceFirst(replacement.getKey(), replacement.getValue());
            }
            return newGoal;
        }

        protected void registerReplacement(Object value, String placeholderName) {
            registerReplacement(value, placeholderName, replacements);
        }

        protected void registerReplacement(Object value, String placeholderName, Map<String, String> replacements) {
            var term = Optional.of(value)
                .filter(Term.class::isInstance)
                .map(Term.class::cast)
                .orElseGet(() -> (Term) conversionPolicy.convertObject(value));
            var termString = prologAPIWrapper.serializeExpression(term);
            replacements.put(placeholderName, termString);
        }

        protected String getNextUnboundPlaceholder() {
            return getNextUnboundPlaceholder(replacements);
        }

        protected String getNextUnboundPlaceholder(Map<String, String> replacements) {
            for (String name : getPlaceholderNames()) {
                if (!replacements.containsKey(name)) {
                    return name;
                }
            }
            throw new IllegalStateException();
        }
    }

    private final NestedQuery nestedQuery;

    public QueryReplacer(ConversionPolicy conversionPolicy, PrologAPIWrapper prologAPIWrapper, String pattern) {
        this.nestedQuery = new NestedQuery(pattern, conversionPolicy, prologAPIWrapper);
    }

    /**
     * Binds the given value to the placeholder with the index 'argument'.
     */
    public Query bind(int argument, Object value) {
        return nestedQuery.bind(argument, value);
    }

    /**
     * Binds the given value to the placeholder variable.
     */
    public Query bind(String variable, Object value) {
        return nestedQuery.bind(variable, value);
    }

    /**
     * Replaces the placeholders in the pattern string with the actualArgs.
     * If not bound to anything, placeholders are replaced in the order they occur in the pattern string.
     * 
     * @param actualArgs Actual values for the placeholders.
     * @return Query string with replaced placeholders
     */
    public String getQueryString(Object... actualArgs) {
        return nestedQuery.buildGoalString(actualArgs);
    }
}
