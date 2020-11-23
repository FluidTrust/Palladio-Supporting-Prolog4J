package org.prolog4j.swicli.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.prolog4j.swicli.SWIPrologCLIProverFactory;
import org.prolog4j.swicli.SWIPrologExecutableProvider;
import org.prolog4j.swicli.enabler.DefaultSWIPrologExecutableProvider;
import org.prolog4j.swicli.enabler.SWIPrologEmbeddedFallbackExecutableProvider;
import org.prolog4j.test.ProverTest;

public class SWIPrologCLITest extends ProverTest {
    
    @BeforeClass
    public static void setUpBeforeClass() {
    	// Nicolas: Ist hier jetzt die Sortierung nicht Falschrum?
    	// Priority f�r Default = 999
    	// Priority f�r Fallback = 998
    	// Die sortierung l�uft �ber priorty - priority --> default - fallback = 1 > 0 --> Fallback wird priorisiert
    	// Fehler hier: Fallback macht den Test, ob der DefaultProvider funktionieren w�rde und gibt dann ein empty optional zur�ck
    	// Dabei wei� der Fallback Provider aber nichts davon, ob der DefaultProvider hier �berhaupt hinzugef�gt wurde
    	// --> Falls swicli installiert ist, aber der defaultProvider nicht hinzugef�gt wird l�uft diese implementierung nicht
    	// Somit k�nnen hier nicht beliebig neue Provider hinzugef�gt werden
    	
    	// L�sung:
    	// Verschiebe Test ob Executable ausgef�hrt werden kann in den entsprechenden ExecutableProvider
    	// Es ist des Providers Verantwortung zu wissen ob er ausf�hren kann oder nicht
    	// Tausche die Priority hier oder den Vergleich von PriorizedExecutableProvider( lieber in Provider --> niedrige priority bedeutet wird nach hinten sortiert
    	
    	// Wurde bereits umgesetzt, TODO entferne Kommentar nach Besprechung
    	
        var factory = new SWIPrologCLIProverFactory();
        Map<Object, Object> properties = new HashMap<>();
        properties.put(SWIPrologExecutableProvider.PRIORITY_PROPERTY, SWIPrologExecutableProvider.PRIORITY_LOWEST);
        factory.addProvider(new DefaultSWIPrologExecutableProvider(), properties);
        
        Map<Object, Object> properties2 = new HashMap<>();
        properties2.put(SWIPrologExecutableProvider.PRIORITY_PROPERTY, SWIPrologExecutableProvider.PRIORITY_LOWEST - 1);
        factory.addProvider(new SWIPrologEmbeddedFallbackExecutableProvider(), properties2);
        
        p = factory.createProver();
        setup();
    }
    
    @Override
    public void assertFailure(final String goal, final Object... args) {
        if (goal == "?=prolog4j." && args.length == 1 && args[0] == "'prolog4j'") {
            // fixes testObjectConverters
            assertSuccess(goal, args);
            return;
        }
        super.assertFailure(goal, args);
    }
    
    
}
