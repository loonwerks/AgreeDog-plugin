/*
Copyright (c) 2021, Collins Aerospace.
Developed with the sponsorship of Defense Advanced Research Projects Agency (DARPA).

Permission is hereby granted, free of charge, to any person obtaining a copy of this data,
including any software or models in source or binary form, as well as any drawings, specifications,
and documentation (collectively "the Data"), to deal in the Data without restriction, including
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Data, and to permit persons to whom the Data is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Data.

THE DATA IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS, SPONSORS, DEVELOPERS, CONTRIBUTORS, OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE DATA OR THE USE OR OTHER DEALINGS IN THE DATA.
*/

package com.rockwellcollins.atc.agree.agreedog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.ComponentImplementation;
import org.osate.ui.dialogs.Dialog;
import org.osgi.framework.Bundle;

import com.rockwellcollins.atc.agree.agreedog.preferences.AgreeDogPreferenceConstants;
import com.rockwellcollins.atc.agree.analysis.extentions.CexExtractor;

import jkind.results.Counterexample;
import jkind.results.Property;

public class AgreeDog implements CexExtractor {

	protected class Runner {
		protected Process process;
		protected BufferedReader fromProcess;
		protected BufferedWriter toProcess;

		Runner() throws Exception {
			ProcessBuilder processBuilder = new ProcessBuilder(getArgs());
			processBuilder.redirectErrorStream(true);
			System.out.println(String.join(" ", processBuilder.command()));
			try {
				process = processBuilder.start();
			} catch (IOException e) {
				Exception generalException = new Exception(
						"Unable to start AgreeDog by executing: " + String.join(" ", processBuilder.command()),
						e);
				throw generalException;
			}
			addShutdownHook();
			toProcess = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			fromProcess = new BufferedReader(new InputStreamReader(process.getInputStream()));
//			String out;
//			while ((out = fromProcess.readLine()) != null) {
//				System.out.println(out);
//				output = out;
//			}
			process.waitFor();
			System.out.println("Finished running AgreeDog.");

		}

		private final Thread shutdownHook = new Thread("shutdown-hook") {
			@Override
			public void run() {
				Runner.this.stop();
			}
		};

		private void addShutdownHook() {
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		}

		private void removeShutdownHook() {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			} catch (IllegalStateException e) {
				// Ignore, we are already shutting down
			}
		}

		public synchronized void stop() {
			/**
			 * This must be synchronized since two threads (an Engine or a shutdown
			 * hook) may try to stop the process at the same time
			 */

			if (process != null) {
				process.destroy();
				process = null;
			}

			removeShutdownHook();
		}

	};

	private final static String AGREE_DOG_EXEC_FOLDER = "exec";
	private final static String AGREE_DOG_EXEC = "AgreeDog";
	private final static String CEX_FOLDER_NAME = "counterexamples";
	private final static String CEX_FILE_EXT = ".txt";

	private String workingDir = "";
	private String openApiKey = "";
	private String counterExampleFile = "";
	private String startFile = "";

	public AgreeDog() {

	}

	private List<String> getArgs() {
		List<String> result = new ArrayList<>();

		result.add(getEntrypoint());
		result.add("--working-dir");
		result.add(workingDir);
		result.add("--user-open-api-key");
		result.add(openApiKey);
		result.add("--counter-example");
		result.add(counterExampleFile);
		result.add("--start-file");
		result.add(startFile);

		return result;
	}

	private String getEntrypoint() {

		final Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		try {
			final URL dirUrl = FileLocator.toFileURL(bundle.getEntry(AGREE_DOG_EXEC_FOLDER));
			final File bin = new File(dirUrl.getPath(), AGREE_DOG_EXEC);
			return bin.getPath();
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to extract " + AGREE_DOG_EXEC + " from plugin", e);
		}
	}

	@Override
	public void receiveCex(ComponentImplementation compImpl, Property property, EObject agreePoperty,
			Counterexample cex, Map<String, EObject> refMap) {

		// Create cex folder (if it doesn't exist) to save cex to
		final IProject project = AgreeDogUtil.getProject(compImpl);
		if (project == null) {
			Dialog.showError("AgreeDog", "Unable to launch AgreeDog.  AADL project could not be determined.");
			return;
		} else {
			workingDir = project.getLocation().toString();
		}

		// Get OpenAI API key
		openApiKey = Activator.getDefault()
				.getPreferenceStore()
				.getString(AgreeDogPreferenceConstants.PREF_OPEN_AI_KEY);
		if (openApiKey.isBlank()) {
			Dialog.showError("AgreeDog",
					"An OpenAI API key is needed to use AgreeDog.  The key can be specified in the AgreeDog preferences.");
			return;
		}

		// Create counterexamples folder if it doesn't exist
		URI uri = URI.createPlatformResourceURI(project.getFullPath().toString(), true);
		uri = uri.appendSegment(CEX_FOLDER_NAME);
		AgreeDogUtil.makeFolder(uri);

		// Refresh directory
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}

		// Create cex file name
		uri = uri.appendSegment(AgreeDogUtil.getUniqueFileName(compImpl, CEX_FILE_EXT));

		// Write cex to file
		final IFile cexFile = AgreeDogUtil.createFile(uri, cex.toString());
		if (cexFile == null) {
			Dialog.showError("AgreeDog", "Unable to launch AgreeDog.  Problem saving counterexample.");
			return;
		} else {
			counterExampleFile = cexFile.getLocation().toString();
		}

		// Get file name containing component implementation
		startFile = AgreeDogUtil.removeProjectName(compImpl.eResource().getURI().toPlatformString(true));

		// Refresh directory
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}

		// Launch AgreeDog
		try {
			new Runner();
		} catch (Exception e) {
			Dialog.showError("AgreeDog", "Error running AgreeDog.");
			e.printStackTrace();
			return;
		}
	}

	@Override
	public String getDisplayText() {
		return "AgreeDog";
	}

}
