package org.prolog4j.problog.enabler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.prolog4j.problog.ProblogExecutable;

public class DefaultProblogExecutable implements ProblogExecutable{
	
	// command by default is different between windows and linux
	private String pythonCommand;
	
	private String tempDirPath;
	
	private String fileSeperator = System.getProperty("file.separator");
	
	private String problogFolderId = "problog-2.1-SNAPSHOT";
	
	private String problogArchive = problogFolderId + ".tar.gz";
	
	public DefaultProblogExecutable(String pythonCommand) {
		this.pythonCommand = pythonCommand;

		this.tempDirPath = System.getProperty("java.io.tmpdir") + fileSeperator + DefaultProblogExecutableProvider.class.getSimpleName();
	}
	
	@Override
	public String execute(String problogProgram) {
		Optional<File> problogFolder = getOrExtractProblog();
		if(!problogFolder.isEmpty()) {
			File problog = problogFolder.get();
			
			String problogCliPath = problog.getAbsolutePath() + fileSeperator + "problog-cli.py";
			
			// install problog
			runPython(new String[]{problogCliPath, "install"});
			
			// run problog
			try {
				// create and write input and output file
				var inputFile = ProblogExecutable.createPrologFile(problogProgram, "problog_input");
				var outputFile = createOutputFile();
				 
				// execute problog with python, arguments are: input file path '-o' output file path
				runPython(new String[]{problogCliPath, inputFile.getAbsolutePath(), "-o", outputFile.getAbsolutePath()});
				
				// read output file
				String output = Files.readString(outputFile.toPath());
				
				// Delete temp files, as deleteOnExit and similar do not work properly in eclipse (maybe standalone too)
				inputFile.delete();
				outputFile.delete();
				
				return output;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// error
		return "";
	}
	
	private int runPython(String[] arguments) {
		try {
			String commandLine = flattenCommand(pythonCommand, arguments);
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(commandLine);
			return process.waitFor();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String flattenCommand(String command, String[] arguments) {
		StringBuilder argBuilder = new StringBuilder();
		argBuilder.append(command);
		for(int i = 0; i < arguments.length; ++i) {
			argBuilder.append(" ");
			argBuilder.append(arguments[i]);
		}
		
		return argBuilder.toString();
	}
	
	private File createOutputFile() throws IOException {
		var tmpFilePath = Files.createTempFile("problog_output", ".txt");
        var tmpFile = tmpFilePath.toFile();
        tmpFile.deleteOnExit();
        
        return tmpFile;
	}
	
	private Optional<File> getOrExtractProblog() {
		var problogDirPath = Paths.get(this.tempDirPath + this.fileSeperator + this.problogFolderId);
		File problogDirectory = problogDirPath.toFile();
		if(problogDirectory.exists()) {
			return Optional.of(problogDirectory);
		} else {
			var optParentDir = extractArchive(problogArchive);
			if(optParentDir.isPresent()) {
				var parentDir = optParentDir.get();
				// change path to the child folder of the actual extracted problog
				return Optional.of(new File(parentDir.getAbsolutePath() + this.fileSeperator + this.problogFolderId));
			} else {
				return Optional.empty();
			}
			
		}
	}
	
	private Optional<File> extractArchive(String resourcePath) {
        var cl = DefaultProblogExecutableProvider.class.getClassLoader();
        if (cl.getResource(resourcePath) == null) {
            return Optional.empty();
        }
        try (InputStream archiveStream = cl.getResourceAsStream(resourcePath)) {
            try (GzipCompressorInputStream gzipIs = new GzipCompressorInputStream(archiveStream)) {
                try (TarArchiveInputStream tarIs = new TarArchiveInputStream(gzipIs)) {
                	var destinationDirectory = Files.createDirectory(Paths.get(this.tempDirPath)).toFile();
                    for (var tarEntry = tarIs.getNextTarEntry(); tarEntry != null; tarEntry = tarIs.getNextTarEntry()) {
                        var dst = new File(destinationDirectory, tarEntry.getName());
                        if (tarEntry.isDirectory()) {
                            dst.mkdirs();
                        } else {
                            try (FileOutputStream fos = new FileOutputStream(dst)) {
                                IOUtils.copyLarge(tarIs, fos, 0, tarEntry.getSize());
                            }
                            if (isExecutable(tarEntry.getMode())) {
                                dst.setExecutable(true);
                            }
                        }
                    }
                    
                    return Optional.of(destinationDirectory);
                }
            }
        } catch (IOException e) {
            // error, return fallback value below
        }
        return Optional.empty();
    }
    
    private boolean isExecutable(int fileMode) {
        // rwx rwx rwx
        // 001 001 001 => 73
        return (fileMode & 73) != 0;
    }
}
