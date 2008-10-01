/*
 * Copyright (c) 2008, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package net.sf.graphiti.ui.commands;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.graphiti.grammar.GrammarTransformer;
import net.sf.graphiti.grammar.XsltTransformer;
import net.sf.graphiti.model.Graph;
import net.sf.graphiti.model.Vertex;
import net.sf.graphiti.ontology.FileFormat;
import net.sf.graphiti.parsers.GenericGraphFileParser;
import net.sf.graphiti.parsers.IncompatibleConfigurationFile;
import net.sf.graphiti.ui.GraphitiPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.osgi.framework.Bundle;
import org.w3c.dom.Element;

/**
 * @author Matthieu Wipliez
 * 
 */
public class PortChooser {

	private class PortContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return ((List<?>) inputElement).toArray();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private RefinementManager manager;

	public PortChooser(RefinementManager manager) {
		this.manager = manager;
	}

	private String choosePort(List<String> ports, String edgePort) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		Shell shell = window.getShell();

		ListDialog dialog = new ListDialog(shell);
		dialog.setContentProvider(new PortContentProvider());
		dialog.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				return (String) element;
			}

		});
		dialog.setAddCancelButton(false);
		dialog.setInput(ports);
		dialog.setMessage("Please choose a " + edgePort + ":");
		dialog.setTitle("Choose " + edgePort);
		do {
			dialog.open();
		} while (dialog.getResult().length == 0);

		return (String) dialog.getResult()[0];
	}

	/**
	 * Returns a port name from the current vertex (set by getSourcePort or
	 * getTargetPort).
	 * 
	 * @param edgePort
	 *            The label to use when prompting the user to choose
	 *            ("source port" or "target port").
	 * @param portType
	 *            The port type to check in the refinement: "Input port" or
	 *            "Output port".
	 * @return A port name if found, <code>null</code> otherwise.
	 */
	private String getPort(String edgePort, String portType) {
		IFile sourceFile = null;
		try {
			sourceFile = manager.getIFileFromSelection();
		} catch (FileNotFoundException e) {
		}

		// open the refinement
		if (sourceFile != null) {
			List<String> ports = getPorts(sourceFile, portType);
			if (!ports.isEmpty()) {
				return choosePort(ports, edgePort);
			}
		}

		return null;
	}

	private String getPortName(String portName) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		Shell shell = window.getShell();

		InputDialog dialog = new InputDialog(shell, "Enter port name",
				"Please enter a " + portName + " name:", "", null);
		dialog.open();
		String value = dialog.getValue();
		if (value.isEmpty()) {
			return null;
		} else {
			return value;
		}
	}

	/**
	 * Returns the list of ports read from the given file that have the given
	 * port type.
	 * 
	 * @param sourceFile
	 * @param portType
	 * @return
	 */
	private List<String> getPorts(IFile sourceFile, String portType) {
		// refinement graph
		Graph graph = null;

		// get file format
		FileFormat format = null;
		try {
			format = (FileFormat) sourceFile
					.getSessionProperty(new QualifiedName("net.sf.graphiti",
							"format"));
		} catch (CoreException e) {
			e.printStackTrace();
		}

		// parse according to format
		if (format != null) {
			graph = parseRefinement(format, sourceFile);
		}

		// get ports from graph
		if (graph == null) {
			return new ArrayList<String>();
		} else {
			Set<Vertex> vertices = graph.vertexSet();
			List<String> ports = new ArrayList<String>();
			for (Vertex vertex : vertices) {
				if (vertex.getType().equals(portType)) {
					String id = (String) vertex.getValue(Vertex.PARAMETER_ID);
					ports.add(id);
				}
			}
			return ports;
		}
	}

	/**
	 * Returns a port from the given vertex.
	 * 
	 * @param source
	 *            The edge's source vertex.
	 * @return A port name.
	 */
	public String getSourcePort(Vertex source) {
		manager.setVertex(source);
		String port = getPort("source port", "Output port");
		if (port == null) {
			return getPortName("source port");
		} else {
			return port;
		}
	}

	/**
	 * Returns a port from the given vertex.
	 * 
	 * @param target
	 *            The edge's target vertex.
	 * @return A port name.
	 */
	public String getTargetPort(Vertex target) {
		manager.setVertex(target);
		String port = getPort("target port", "Input port");
		if (port == null) {
			return getPortName("target port");
		} else {
			return port;
		}
	}

	private Graph parseRefinement(FileFormat format, IFile sourceFile) {
		Graph graph = null;
		String grammar = format.hasGrammar();
		if (grammar.isEmpty()) {
			// parse with generic parser.
			GenericGraphFileParser parser = new GenericGraphFileParser(
					GraphitiPlugin.getDefault().getConfiguration());
			try {
				graph = parser.parse(sourceFile);
			} catch (IncompatibleConfigurationFile e) {
				// nothing we can do
			}
		} else {
			Bundle bundle = GraphitiPlugin.getDefault().getBundle();
			URL url = bundle.getEntry("src/owl/" + grammar);
			try {
				// parse and transform
				InputStream is = sourceFile.getContents();
				Element source = new GrammarTransformer(url)
						.parse(new InputStreamReader(is));
				url = bundle.getEntry("src/owl/" + format.hasXslt());
				Element target = new XsltTransformer(url).transformDomToDom(
						source, "dummy");

				// parse the result with generic parser.
				GenericGraphFileParser parser = new GenericGraphFileParser(
						GraphitiPlugin.getDefault().getConfiguration());
				try {
					graph = parser.parse(target);
				} catch (IncompatibleConfigurationFile e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return graph;
	}
}