package org.prolog4j.problog.enabler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;
import org.prolog4j.problog.ProblogExecutable;
import org.prolog4j.problog.ProblogExecutableProvider;

public class ProblogStandaloneExecutableProvider implements ProblogExecutableProvider {
	
	private Optional<ProblogExecutable> executable;
	
	public ProblogStandaloneExecutableProvider() {
		executable = createExecutable();
	}
	
	@Override
	public Optional<ProblogExecutable> getExecutable() {
		return this.executable;
	}

	protected Optional<ProblogExecutable> createExecutable() {
		// we do not support other combinations yet
		if (!SystemUtils.OS_ARCH.contains("64") || !(SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX)) {
			return Optional.empty();
		}
		
		var file = extractExecutable();
		
		if(file.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(new ProblogStandaloneExecutable(file.get()));
		}
	}

	private String getFileName() {
		String os = null;
		String ending = null;
		if (SystemUtils.IS_OS_WINDOWS) {
			os = "win";
			ending = ".exe";
		} else if (SystemUtils.IS_OS_LINUX) {
			os = "linux";
			ending = "";
		}
		String version = "2.1";
		String architecture = "x64";
		return String.format("problog-%s-%s-%s%s", version, os, architecture, ending);
	}
	
	private Optional<File> extractExecutable() {
		var cl = ProblogStandaloneExecutableProvider.class.getClassLoader();
		var execFileName = getFileName();
		if (cl.getResource(execFileName) == null) {
			return Optional.empty();
		}
		try (InputStream execInStream = cl.getResourceAsStream(execFileName)) {
			var execFilePath = Files.createTempFile(execFileName, ""); //suffix already in resourcePath
	        var execFile = execFilePath.toFile();
	        execFile.deleteOnExit();
	        
	        FileOutputStream execFileOutStream = new FileOutputStream(execFile);
			while(execInStream.available()>0)
            { 
                execFileOutStream.write(execInStream.read());
            }
			
			execFileOutStream.close();
			execInStream.close();
			
			if (!execFile.canExecute()) {
				execFile.setExecutable(true);
            }
			
			return Optional.of(execFile);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Optional.empty();
	}

}
