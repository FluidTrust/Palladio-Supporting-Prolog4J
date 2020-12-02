package org.prolog4j.problog.enabler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import org.prolog4j.problog.ProblogExecutable;

public class DefaultProblogExecutable implements ProblogExecutable{
	
	// command by default is different between windows and linux
	private String problogCommand;
	
	/**
	 * Constructor.
	 * @param problogCommand
	 * 		Is made up of system specific 'python' command and path to the 'problog-cli.py' file in a problog distribution
	 */
	public DefaultProblogExecutable(String problogCommand) {
		this.problogCommand = problogCommand;
	}
	
	@Override
	public String execute(String problogProgram) {
			// install problog
			runProblog(new String[]{"install"});
			
			// run problog
			try {
				// create and write input and output file
				var inputFile = ProblogExecutable.createTempFile("problog_input", ".pl", problogProgram);
				var outputFile = ProblogExecutable.createTempFile("problog_output", ".txt", null);
				 
				// execute problog with python, arguments are: input file path '-o' output file path
				runProblog(new String[]{inputFile.getAbsolutePath(), "-o", outputFile.getAbsolutePath()});
				
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
		
		// error
		return "";
	}
	
	private int runProblog(String[] arguments) {
		try {
			String commandLine = flattenCommand(this.problogCommand, arguments);
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
}
