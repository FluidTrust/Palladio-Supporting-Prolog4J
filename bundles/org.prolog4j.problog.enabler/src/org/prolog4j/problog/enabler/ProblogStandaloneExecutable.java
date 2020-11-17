package org.prolog4j.problog.enabler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Scanner;

import org.prolog4j.problog.ProblogExecutable;

public class ProblogStandaloneExecutable implements ProblogExecutable {
	
	private File executable;
	
	public ProblogStandaloneExecutable(File executable) {
		this.executable = executable;
	}
	
	@Override
	public String execute(String problogProgram) {
		try {
			//create tmp file for input
			var inputFile = createPrologFile(problogProgram);
			return runProblog(inputFile);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	private String runProblog(File inputFile) throws IOException, InterruptedException {
        var pb = new ProcessBuilder(Arrays.asList(executable.getPath(), inputFile.getAbsolutePath()));
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
            System.err.println("Failed to execute ProbLog.");
            throw new IllegalStateException();
        }
	}
	
	// similar copy can be found in SWIPRologCLIRun.java
    private static File createPrologFile(String program) throws IOException {
        var tmpFilePath = Files.createTempFile(ProblogStandaloneExecutable.class.getSimpleName(), ".pl");
        var tmpFile = tmpFilePath.toFile();
        tmpFile.deleteOnExit();
        Files.writeString(tmpFilePath, program, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        return tmpFile;
    }
}