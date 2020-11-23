package org.prolog4j.problog;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.prolog4j.base.Executable;

public interface ProblogExecutable extends Executable {
	
	public String execute(String problogProgram);

	// similar copy can be found in SWIPRologCLIRun.java
    static File createPrologFile(String program, String fileName) throws IOException {
        var tmpFilePath = Files.createTempFile(fileName, ".pl");
        var tmpFile = tmpFilePath.toFile();
        tmpFile.deleteOnExit();
        Files.writeString(tmpFilePath, program, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        return tmpFile;
    }
}
