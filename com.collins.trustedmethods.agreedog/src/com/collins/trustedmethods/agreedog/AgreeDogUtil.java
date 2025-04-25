package com.collins.trustedmethods.agreedog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.osate.aadl2.Element;
import org.osate.aadl2.NamedElement;

public class AgreeDogUtil {

	public static IProject getProject(Element element) {
		try {
			return getFile(element.eResource().getURI()).getProject();
		} catch (Exception e) {

		}

		return null;
	}

	public static String getProjectRelativePath(Element element) {
		try {
			IFile file = getFile(element.eResource().getURI());
			return "/" + file.getProjectRelativePath().removeLastSegments(1).toString();
		} catch (Exception e) {

		}

		return null;
	}

	public static IFile getFile(URI f) {
		final String pathString = f.isPlatform() ? f.toPlatformString(true) : f.toString();
		return getRoot().getFile(new Path(pathString));
	}

	public static IFolder makeFolder(URI f) {

		final IFolder folder = getRoot().getFolder(new Path(f.toPlatformString(true)));
		try {
			if (!folder.exists()) {
				folder.create(true, true, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			System.err.println("Error: trouble creating folder.");
			e.printStackTrace();
		}
		return folder;
	}

	public static String getUniqueFileName(NamedElement namedElement, String fileExtension) {
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		final String timestamp = simpleDateFormat.format(new Date());
		return timestamp + "_" + namedElement.getQualifiedName().replace("::", "_").replace(".", "_") + fileExtension;
	}

	public static String getCexFileName(NamedElement namedElement, String fileExtension) {
		return namedElement.getName().replace("::", "_").replace(".", "_") + "_cex" + fileExtension;
	}

	public static IFile createFile(URI fileName, String contents) {
		final IFile file = getRoot().getFile(new Path(fileName.toPlatformString(true)));
		if (!writeFile(file, contents)) {
			return null;
		}
		return file;
	}

	public static String removeProjectName(String filePath) {

		if (filePath == null || filePath.isBlank() || "/".equals(filePath)) {
			return "";
		}

		List<String> segments = Arrays.asList(filePath.split("/"));

		while (!segments.isEmpty() && segments.get(0).isBlank()) {
			segments = segments.subList(1, segments.size());
		}

		if (segments.isEmpty()) {
			return "";
		} else {
			return "/" + String.join("/", segments.subList(1, segments.size()));
		}
	}

	public static boolean writeFile(IFile res, String contents) {
		final NullProgressMonitor monitor = new NullProgressMonitor();
		final InputStream stream = new ByteArrayInputStream(contents.getBytes());

		try {
			if (res.exists()) {
				res.delete(true, monitor);
			}
			res.create(stream, true, monitor);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static IWorkspaceRoot getRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

}
