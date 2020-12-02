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
    static File createTempFile(String fileName, String fileSuffix, String fileContent) throws IOException {
        var tmpFilePath = Files.createTempFile(fileName, fileSuffix);
        var tmpFile = tmpFilePath.toFile();
        tmpFile.deleteOnExit();
        if(fileContent != null && !fileContent.isEmpty()) {
        	Files.writeString(tmpFilePath, fileContent, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        }
        
        return tmpFile;
    }
}
