package org.prolog4j.swicli.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.SyntaxErrorMessage;
import org.eclipse.xtext.parser.IParseResult;
import org.palladiosimulator.supporting.prolog.api.PrologAPI;
import org.palladiosimulator.supporting.prolog.model.prolog.False;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Expression;
import org.palladiosimulator.supporting.prolog.parser.antlr.PrologParser;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.InvalidQueryException;
import org.prolog4j.Query;
import org.prolog4j.Solution;
import org.prolog4j.swicli.SWIPrologExecutable;

public class SWIPrologCLIQuery extends Query {

//    private final Map<String, String> replacements = new HashMap<>();
    private final ConversionPolicy cp;
    private final PrologAPI prologAPI;
    private final SWIPrologExecutable executable;
    private final String theory;
    private final PrologParser prologParser;
    private final QueryReplacer queryReplacer;
    
    public SWIPrologCLIQuery(ConversionPolicy cp, PrologAPI prologAPI, SWIPrologExecutable executable, String theory, String goalPattern) {
        super(goalPattern);
        this.cp = cp;
        this.prologAPI = prologAPI;
        this.executable = executable;
        this.theory = theory;
        this.prologParser = prologAPI.getParser();
        this.queryReplacer = new QueryReplacer(cp, prologAPI, goalPattern);
    }

    @Override
    public <A> Solution<A> solve(Object... actualArgs) {
        String newGoal = queryReplacer.getQueryString(actualArgs);
//        String newGoal = buildGoalString(actualArgs);
        
        String resultString = null;
        try {
            resultString = executeQuery(newGoal);
        } catch (IOException | InterruptedException | ExecutionException e) {
            return new SWIPrologCLIFailedSolution<A>(e);
        }

        String result = extractResultString(resultString, newGoal);
        
        // success without variables
        if (result.isBlank()) {
            return new SWIPrologCLISolution<>(cp);
        }
        
        // parse solution
        IParseResult parseResult = prologParser.parse(prologParser.getGrammarAccess().getExpression_1100_xfyRule(), new StringReader(result));

        // solution has syntax error
        if (parseResult.hasSyntaxErrors()) {
            String syntaxErrors = StreamSupport.stream(parseResult.getSyntaxErrors().spliterator(), false).map(INode::getSyntaxErrorMessage).map(SyntaxErrorMessage::toString).collect(Collectors.joining(System.lineSeparator()));
            return new SWIPrologCLIFailedSolution<A>(result, syntaxErrors);
        }
        
        // result is false, i.e. failed query
        if (parseResult.getRootASTElement() instanceof False) {
            return new SWIPrologCLIFailedSolution<>();
        }
        
        // result is sucess and contains variables
        return new SWIPrologCLISolution<A>(cp, (Expression)parseResult.getRootASTElement());
    }

    private String extractResultString(String resultString, String queryString) {
        String[] resultStringLines = resultString.replace("\r", "").split("\n");
        List<String> errorLines = new ArrayList<>();
        List<String> warningLines = new ArrayList<>();
        List<String> regularLines = new ArrayList<>();
        for (String line : resultStringLines) {
            if (line.startsWith("WARNING")) {
                warningLines.add(line);
            } else if (line.startsWith("ERROR")) {
                errorLines.add(line);
            }
            else {
                regularLines.add(line);
            }
        }
        
        if (!errorLines.isEmpty()) {
            throw new InvalidQueryException(queryString, new Throwable(mergeLines(errorLines)));
        }
        
        if (!warningLines.isEmpty()) {
            System.err.println(mergeLines(warningLines));            
        }
        
        String result = mergeLines(regularLines);
        return result;
    }

    protected static String mergeLines(Collection<String> lines) {
        return lines.stream().collect(Collectors.joining(System.lineSeparator()));
    }
    
    private String executeQuery(String newGoal) throws IOException, InterruptedException, ExecutionException {
        SWIPrologCLIRun cliRun = new SWIPrologCLIRun(executable, prologAPI);
        String resultString = cliRun.execute(theory, newGoal);
        return resultString;
    }

//    private String buildGoalString(Object... actualArgs) {
//        if (getPlaceholderNames().size() - replacements.size() != actualArgs.length) {
//            throw new IllegalArgumentException();
//        }
//        
//        HashMap<String, String> temporaryReplacements = new HashMap<String, String>(replacements);
//        
//        for (int i = 0; i < actualArgs.length; ++i) {
//            String placeholderName = getNextUnboundPlaceholder(temporaryReplacements);
//            registerReplacement(actualArgs[i], placeholderName, temporaryReplacements);
//        }
//
//        String newGoal = getGoal();
//        for (Entry<String, String> replacement : temporaryReplacements.entrySet()) {
//            newGoal = newGoal.replaceFirst(replacement.getKey(), replacement.getValue());
//        }
//        return newGoal;
//    }

    @Override
    public Query bind(int argument, Object value) {
//        var placeholderName = getPlaceholderNames().get(argument);
//        registerReplacement(value, placeholderName);
        queryReplacer.bind(argument, value);
        return this;
    }

    @Override
    public Query bind(String variable, Object value) {
//        if (!getPlaceholderNames().contains(variable)) {
//            throw new IllegalArgumentException();
//        }
//        registerReplacement(value, variable);
        queryReplacer.bind(variable, value);
        return this;
    }
    
//    protected void registerReplacement(Object value, String placeholderName) {
//        registerReplacement(value, placeholderName, replacements);
//    }
//    
//    protected void registerReplacement(Object value, String placeholderName, Map<String, String> replacements) {
//        var term = (Term)cp.convertObject(value);
//        String termString = prologAPI.getSerializer().serialize(term);
//        replacements.put(placeholderName, termString);
//    }
//    
//    protected String getNextUnboundPlaceholder() {
//        return getNextUnboundPlaceholder(replacements);
//    }
//    
//    protected String getNextUnboundPlaceholder(Map<String, String> replacements) {
//        for (String name : getPlaceholderNames()) {
//            if (!replacements.containsKey(name)) {
//                return name;
//            }
//        }
//        throw new IllegalStateException();
//    }

}
