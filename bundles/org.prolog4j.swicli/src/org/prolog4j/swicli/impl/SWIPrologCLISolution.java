package org.prolog4j.swicli.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.palladiosimulator.supporting.prolog.model.prolog.CompoundTerm;
import org.palladiosimulator.supporting.prolog.model.prolog.False;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.BinaryExpression;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Expression;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.LogicalAnd;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.LogicalOr;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Unification;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Solution;
import org.prolog4j.UnknownVariableException;

public class SWIPrologCLISolution<S> extends Solution<S> {

    private final ConversionPolicy cp;
    private final List<Map<String, Object>> results;
    private int resultIndex = 0;
    private boolean success;
    
    public SWIPrologCLISolution(ConversionPolicy cp) {
        this.cp = cp;
        this.results = Collections.emptyList();
        success = true;
    }
    
    public SWIPrologCLISolution(ConversionPolicy cp, Expression results) {
        this.cp = cp;
        this.results = parseResults(results);
        this.success = true;
        setDefaultVariable();
    }
    
    protected void setDefaultVariable() {
        if (results.isEmpty()) {
            return;
        }
        
        String lastKey = null;
        for (String key : results.get(0).keySet()) {
            lastKey = key;
        }
        
        if (lastKey == null) {
            return;
        }
        
        on(lastKey);
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    @SuppressWarnings("unchecked") // this is actually an issue of the API
    public <A> A get(String variable) {
        Object result = getVariableValue(variable);
        if (clazz != null) {
            return (A) get(variable, clazz);
        }
        return (A) cp.convertTerm(result);
    }

    @Override
    public <A> A get(String variable, Class<A> type) {
        Object result = getVariableValue(variable);
        return (A)cp.convertTerm(result, type);
    }

    @Override
    protected boolean fetch() {
        if (results.size() > resultIndex + 1) {
            resultIndex++;
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void collect(Collection<?>... collections) {
        for (int iVariable = 0; iVariable < collections.length; ++iVariable) {
            for (int iResult = 0; iResult < results.size(); ++iResult) {
                Iterator<Entry<String, Object>> variableIter = results.get(iResult).entrySet().iterator();
                Entry<String, Object> entry = variableIter.next();
                for (int i = 0; i < iVariable; ++i) {
                    entry = variableIter.next();
                }
                ((Collection<Object>)collections[iVariable]).add(entry.getValue());
            }
        }
    }

    @Override
    public List<?>[] toLists() {
        List<?>[] tmp = new List<?>[results.size()];
        for (int i = 0; i < results.size(); ++i) {
            Map<String, Object> result = results.get(i);
            tmp[i] = new ArrayList<>(result.values());
        }
        return tmp;
    }
    
    protected Object getVariableValue(String variable) {
        return Optional.ofNullable(results)
                .filter(c -> !c.isEmpty())
                .map(c -> c.get(resultIndex))
                .map(m -> m.get(variable))
                .orElseThrow(() -> new UnknownVariableException(variable));
    }
    
    protected static List<Map<String, Object>> parseResults(Expression resultExpression) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // ensure that at least one result is available
        if (resultExpression instanceof False) {
            return results;
        }

        // collect solution groups
        List<Expression> solutionGroups = findNestedExpressions((LogicalOr) resultExpression, LogicalOr.class);
        solutionGroups.remove(solutionGroups.size() - 1);

        // extract variables from groups
        for (Expression solutionGroup : solutionGroups) {
            
            List<Unification> variableBindings = Collections.emptyList();
            if (solutionGroup instanceof LogicalAnd) {
                variableBindings = findNestedExpressions((LogicalAnd) solutionGroup, LogicalAnd.class,
                        Unification.class);
            } else if (solutionGroup instanceof Unification) {
                variableBindings = Arrays.asList((Unification) solutionGroup);
            } else {
                // error
            }
            
            Map<String, Object> result = new LinkedHashMap<>();
            for (Unification variableBinding : variableBindings) {
                CompoundTerm variable = (CompoundTerm)(variableBinding).getLeft();
                Expression content = (variableBinding).getRight();
                result.put(variable.getValue(), content);
            }
            results.add(result);
        }

        return results;
    }

    protected static <T extends BinaryExpression, E extends Expression> List<E> findNestedExpressions(T expr, Class<T> exprClass, Class<E> nestedExprClass) {
        return findNestedExpressions(expr, exprClass).stream().filter(nestedExprClass::isInstance).map(nestedExprClass::cast).collect(Collectors.toList());
    }
    
    @SuppressWarnings("unchecked")
    protected static <T extends BinaryExpression> List<Expression> findNestedExpressions(T expr, Class<T> exprClass) {
        List<Expression> nestedExpressions = new ArrayList<>();
        T currentResult = expr;
        while (currentResult != null) {
            nestedExpressions.add(currentResult.getRight());
            if (exprClass.isInstance(currentResult.getLeft())) {
                currentResult = (T) currentResult.getLeft();
            } else {
                nestedExpressions.add(currentResult.getLeft());
                currentResult = null;
            }
        }
        Collections.reverse(nestedExpressions);
        return nestedExpressions;
    }

}
