package org.prolog4j.swicli.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.supporting.prolog.api.PrologAPI;
import org.palladiosimulator.supporting.prolog.model.prolog.CompoundTerm;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Expression;
import org.palladiosimulator.supporting.prolog.parser.antlr.PrologParser;
import org.prolog4j.swicli.SWIPrologExecutable;

public class SWIPrologCLIRun {

    private final SWIPrologExecutable executable;
    private final PrologParser prologParser;

    public SWIPrologCLIRun(SWIPrologExecutable executable, PrologAPI prologApi) {
        this.executable = executable;
        prologParser = prologApi.getParser();
    }

    public String execute(String theory, String goal) throws IOException, InterruptedException, ExecutionException {
        var variables = determineVariables(goal);
        var actualGoal = buildGoal(goal, variables);
        var programFileContent = theory + System.lineSeparator() + ":- " + actualGoal;
        var programFile = createPrologFile(programFileContent);
        var pb = new ProcessBuilder(Arrays.asList(executable.getPath(), "-q", "-f", programFile.getAbsolutePath(), "-g", "halt"));
        pb.environment().putAll(executable.getEnvironment());
        pb.redirectErrorStream(true);
        var process = pb.start();
        
        String processOutput = null;
        try (var s = new Scanner(process.getInputStream()).useDelimiter("\\A")) {
            processOutput = s.hasNext() ? s.next() : "";            
        }
        process.waitFor();
        var processExitValue = process.exitValue();
        if (processExitValue == 0) {
            // everything is ok
            return processOutput;
        } else {
            // error
            throw new IllegalStateException();
        }
    }

    protected static String buildGoal(String goal, Collection<String> variables) {
        var goalParameter = goal.replaceAll("[\\r\\n]", "")
            .trim();
        if (goalParameter.endsWith(".")) {
            goalParameter = goalParameter.substring(0, goalParameter.length() - 1);
        }
        var writeGoals = variables.stream()
            .map(SWIPrologCLIRun::getVariablePrintGoals)
            .collect(Collectors.joining(", writeln(','), "));
        if (variables.isEmpty()) {
            writeGoals = "writeln(true)";
        }
        var actualGoal = "forall((Goal = (" + goalParameter + "), call(Goal)), (" + writeGoals
                + ", writeln(';'))), write(false).";
        return actualGoal;
    }

    protected static String getVariablePrintGoals(String variableName) {
        return String.format("write('%1$s = '), writeq(%1$s)", variableName);
    }

    protected Collection<String> determineVariables(String goal) {
        var initialRule = prologParser.getGrammarAccess()
            .getExpression_1100_xfyRule();
        var parsingResult = prologParser.parse(initialRule, new StringReader(goal));
        var parsedQuery = (Expression) parsingResult.getRootASTElement();

        var variables = new LinkedHashSet<String>();
        var queue = new LinkedList<EObject>();
        queue.add(parsedQuery);
        while (!queue.isEmpty()) {
            EObject currentElement = queue.pop();
            queue.addAll(currentElement.eContents());
            if (currentElement instanceof CompoundTerm) {
                var term = (CompoundTerm) currentElement;
                if (term.getArguments()
                    .isEmpty()) {
                    variables.add(term.getValue());
                }
            }
        }
        return variables;
    }

    protected static File createPrologFile(String program) throws IOException {
        var tmpFilePath = Files.createTempFile(SWIPrologCLIRun.class.getSimpleName(), ".pl");
        var tmpFile = tmpFilePath.toFile();
        tmpFile.deleteOnExit();
        Files.writeString(tmpFilePath, program, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        return tmpFile;
    }

}
