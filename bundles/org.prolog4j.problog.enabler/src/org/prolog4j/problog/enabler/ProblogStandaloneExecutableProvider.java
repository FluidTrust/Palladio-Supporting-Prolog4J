package org.prolog4j.problog.enabler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
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

		if (isPythonInstalled()) {
			return Optional.empty();
		}

		var resourcePath = getFileName();
		var file = new File(resourcePath);

		return Optional.of(new ProblogStandaloneExecutable(file));
	}

	protected String getFileName() {
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
	
	private boolean isPythonInstalled() {
		String pythonCommand = null;
		if (SystemUtils.IS_OS_WINDOWS) {
			pythonCommand = "python";
		}
		if (SystemUtils.IS_OS_LINUX) {
			pythonCommand = "python3";
		}
		
		try {
			ProcessBuilder builder = new ProcessBuilder(pythonCommand, "-V");
			Process process = builder.start();
			process.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line = reader.readLine();
			return (line != null && (line.startsWith("Python 3.6")));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
//	//Copy executable to tmp dir
//	protected File extractExecutable(String resourcePath) {
//		var cl = ProblogStandaloneExecutableProvider.class.getClassLoader();
//		if (cl.getResource(resourcePath) == null) {
//			return Optional.empty();
//		}
//		try (InputStream archiveStream = cl.getResourceAsStream(resourcePath)) {
//			File destinationDirectory = Files
//					.createTempDirectory(ProblogStandaloneExecutableProvider.class.getSimpleName())
//					.toFile();
//			FileUtils.forceDeleteOnExit(destinationDirectory);
//			var dst = new File(destinationDirectory, );
//				if (tarEntry.isDirectory()) {
//					dst.mkdirs();
//				} else {
//					try (FileOutputStream fos = new FileOutputStream(dst)) {
//						IOUtils.copyLarge(tarIs, fos, 0, tarEntry.getSize());
//					}
//					if (isExecutable(tarEntry.getMode())) {
//						dst.setExecutable(true);
//					}
//				}
//			return Optional.of(destinationDirectory);
//		} catch (IOException e) {
//			// error, return fallback value below
//		}
//		return Optional.empty();
//	}

}
