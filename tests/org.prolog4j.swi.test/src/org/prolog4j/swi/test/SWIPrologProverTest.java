/* 
 * Copyright (c) 2010 Miklos Espak
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.prolog4j.swi.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prolog4j.swi.SWIPrologProverFactory;
import org.prolog4j.test.ProverTest;

import swiprolog.SwiInstaller;

/**
 * JUnit test for the SWI-Prolog binding. Inherits the common test class.
 */
public class SWIPrologProverTest extends ProverTest {

	protected static File tmpDir;
	
	@Override
	@Test
	public void testInvalidQuery2() {
		// SWI-Prolog does not need a period at the end of queries.
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		initSwiPrologStandalone();
		p = new SWIPrologProverFactory().createProver();
		setup();
	}

	private static void initSwiPrologStandalone() throws IOException {
		tmpDir = Files.createTempDirectory(SWIPrologProverTest.class.getName()).toFile();
		FileUtils.forceDeleteOnExit(tmpDir);
		tmpDir.delete();
		SwiInstaller.overrideDirectory(tmpDir.getAbsolutePath());
		SwiInstaller.init();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws IOException {
		FileUtils.deleteDirectory(tmpDir);
	}

}
