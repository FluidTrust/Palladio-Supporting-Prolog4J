package org.prolog4j.problog.enabler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.osgi.service.component.annotations.Component;
import org.prolog4j.problog.ProblogExecutable;
import org.prolog4j.problog.ProblogExecutableProvider;

@Component(property = ProblogExecutableProvider.PRIORITY_PROPERTY + " = "
        + ProblogExecutableProvider.PRIORITY_LOWEST)
public class DefaultProblogExecutableProvider implements ProblogExecutableProvider {
	
	private String tempDirPath;
	private String fileSeperator;
	
	private String problogFolderId = "problog-2.1-SNAPSHOT";
	private String problogArchive = problogFolderId + ".tar.gz";
	
	public DefaultProblogExecutableProvider() {
		this.fileSeperator = System.getProperty("file.separator");
		this.tempDirPath = System.getProperty("java.io.tmpdir") + fileSeperator + DefaultProblogExecutableProvider.class.getSimpleName();
	}

	@Override
	public Optional<ProblogExecutable> getExecutable() {
		
		String pythonCommand = "";
		if (SystemUtils.IS_OS_WINDOWS) {
			pythonCommand = "python";
		}
		if (SystemUtils.IS_OS_LINUX) {
			pythonCommand = "python3";
		}
		
		// check if python 3.6 is installed
		if(isPythonInstalled(pythonCommand)) {
			// try to extract or get path to a problog distribution
			Optional<File> optProblog = getOrExtractProblog();
			if(optProblog.isPresent()) {
				// if problog dist could be extracted/found create executable
				String problogCommand = pythonCommand + " " + optProblog.get().getAbsolutePath() + fileSeperator + "problog-cli.py";
				return Optional.of(new DefaultProblogExecutable(problogCommand));
			}
		}

		// either the system is not supported, required python version is not installed, or problog distr. could not be extracted
		return Optional.empty();
	}
	
	private boolean isPythonInstalled(String pythonCommand) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(pythonCommand + " -V").getInputStream()));
			String line = reader.readLine();
			return (line != null && (line.startsWith("Python 3.6")));
        } catch (Exception e) {
            return false;
        }
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