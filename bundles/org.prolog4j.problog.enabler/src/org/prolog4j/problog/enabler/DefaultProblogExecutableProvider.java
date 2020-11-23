package org.prolog4j.problog.enabler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;
import org.osgi.service.component.annotations.Component;
import org.prolog4j.problog.ProblogExecutable;
import org.prolog4j.problog.ProblogExecutableProvider;

//TODO: Test auf installiertes Prolog (siehe JProblog... oder einfach JProblog benutzen?!)

@Component(property = ProblogExecutableProvider.PRIORITY_PROPERTY + " = "
        + ProblogExecutableProvider.PRIORITY_LOWEST)
public class DefaultProblogExecutableProvider implements ProblogExecutableProvider {

	@Override
	public Optional<ProblogExecutable> getExecutable() {
		
		String pythonCommand = null;
		if (SystemUtils.IS_OS_WINDOWS) {
			pythonCommand = "python";
		}
		if (SystemUtils.IS_OS_LINUX) {
			pythonCommand = "python3";
		}
		
		if(isPythonInstalled(pythonCommand)) {
			return Optional.of(new DefaultProblogExecutable(pythonCommand));
		} else {
			return Optional.empty();
		}
	}
	
	private boolean isPythonInstalled(String pythonCommand) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(pythonCommand + " -V").getInputStream()));
			String line = reader.readLine();
			return (line != null && (line.startsWith("Python 3.6")));
//            if (Runtime.getRuntime()
//                .exec(pythonCommand + " -V").getErrorStream()
//                .exitValue() == 0) {
//                return true;
//            }
//            return false;
        } catch (Exception e) {
            return false;
        }
		
//		try {
//			ProcessBuilder builder = new ProcessBuilder(pythonCommand, "-V");
//			Process process = builder.start();
//			process.waitFor();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//			String line = reader.readLine();
//			return (line != null && (line.startsWith("Python 3.6")));
//		} catch (IOException e) {
//			throw new UncheckedIOException(e);
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}
	}

}
