package org.prolog4j.base;

import java.io.Reader;

import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.XtextResource;
import org.palladiosimulator.supporting.prolog.api.PrologAPI;
import org.palladiosimulator.supporting.prolog.api.impl.PrologAPIImpl;
import org.palladiosimulator.supporting.prolog.model.prolog.PrologFactory;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Expression;
import org.palladiosimulator.supporting.prolog.services.PrologGrammarAccess;

public class PrologAPIWrapper {

    private final PrologAPI prologApi;

    public PrologAPIWrapper() {
        if (Activator.getInstance() != null) {
            prologApi = Activator.getInstance().getPrologApi();
        } else {
            prologApi = new PrologAPIImpl();
        }
    }

    public PrologAPI getPrologApi() {
        return prologApi;
    }
    
    public String serializeExpression(Expression expr) {
    	var program = PrologFactory.eINSTANCE.createProgram();
        var rule = PrologFactory.eINSTANCE.createRule();
        program.getClauses().add(rule);
        var head = PrologFactory.eINSTANCE.createCompoundTerm();
        rule.setHead(head);
        head.setValue("test");
        rule.setBody(expr);
        var r = new XtextResource();
        r.getContents().add(program);
        String termString = prologApi.getSerializer()
            .serialize(expr);
        return termString;
    }
    
    public IParseResult parse(ParserRule rule, Reader reader) {
    	return prologApi.getParser().parse(rule, reader);
    }
    
    // Vielleich spezielle funktion für einzelne Rules und Kommentare um zu erklären was dies genau ist.
    // Vielleicht auch spezielle parse Funktionen, welche 
    public PrologGrammarAccess getGrammarAccess() {
    	return prologApi.getParser().getGrammarAccess();
    }
    
}
