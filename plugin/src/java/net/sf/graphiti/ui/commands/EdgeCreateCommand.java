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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.graphiti.model.Edge;
import net.sf.graphiti.model.Graph;
import net.sf.graphiti.model.Vertex;
import net.sf.graphiti.parsers.GenericGraphFileParser;
import net.sf.graphiti.parsers.IncompatibleConfigurationFile;
import net.sf.graphiti.ui.GraphitiPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * This class provides a Command that creates a dependency. ComplexSource and
 * target are set when they are connected.
 * 
 * @author Samuel Beaussier
 * @author Nicolas Isch
 * @author Matthieu Wipliez
 */
public class EdgeCreateCommand extends OpenRefinementNewTabCommand {

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

	/**
	 * The edge is stored as an attribute so it can be used both in the
	 * <code>execute</code> and <code>undo</code> methods.
	 */
	private Edge edge;

	/**
	 * The parentGraph is stored as an attribute so it can be used both in the
	 * <code>execute</code> and <code>undo</code> methods.
	 */
	private Graph parentGraph;

	private Vertex source;

	private Vertex target;

	/**
	 * Creates a new command using the given newly created edge.
	 * 
	 * @param edge
	 *            The newly created edge.
	 */
	public EdgeCreateCommand(Edge edge) {
		this.edge = edge;
	}

	@Override
	public boolean canExecute() {
		return (source.equals(target) == false);
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
		dialog.open();

		return (String) dialog.getResult()[0];
	}

	@Override
	public void execute() {
		setEditedFile();

		// parent graph
		parentGraph = source.getParent();

		// edge has been set in the constructor.
		edge.setSource(source);
		edge.setTarget(target);

		if (edge.getParameter(Edge.PARAMETER_SOURCE_PORT) != null) {
			edge.setValue(Edge.PARAMETER_SOURCE_PORT, getSourcePort(source));
		}

		if (edge.getParameter(Edge.PARAMETER_TARGET_PORT) != null) {
			edge.setValue(Edge.PARAMETER_TARGET_PORT, getTargetPort(target));
		}

		parentGraph.addEdge(edge);
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
			sourceFile = getIFileFromSelection();
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

	/**
	 * Returns the list of ports read from the given file that have the given
	 * port type.
	 * 
	 * @param sourceFile
	 * @param portType
	 * @return
	 */
	private List<String> getPorts(IFile sourceFile, String portType) {
		// first with the generic parser.
		GenericGraphFileParser parser = new GenericGraphFileParser(
				GraphitiPlugin.getDefault().getConfiguration());
		try {
			Graph graph = parser.parse(sourceFile);
			Set<Vertex> vertices = graph.vertexSet();
			List<String> ports = new ArrayList<String>();
			for (Vertex vertex : vertices) {
				if (vertex.getType().equals(portType)) {
					String id = (String) vertex.getValue(Vertex.PARAMETER_ID);
					ports.add(id);
				}
			}

			return ports;
		} catch (IncompatibleConfigurationFile e) {
			// then with the refinement parser.
		}

		return new ArrayList<String>();
	}

	/**
	 * Returns a port from the given vertex.
	 * 
	 * @param source
	 *            The edge's source vertex.
	 * @return A port name.
	 */
	private String getSourcePort(Vertex source) {
		vertex = source;
		String port = getPort("source port", "Output port");
		if (port == null) {
			return "source port";
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
	private String getTargetPort(Vertex target) {
		vertex = target;
		String port = getPort("target port", "Input port");
		if (port == null) {
			return "target port";
		} else {
			return port;
		}
	}

	/**
	 * Sets the source of the dependency to create/reconnect.
	 * 
	 * @param source
	 *            The dependency source as a Port.
	 */
	public void setSource(Vertex source) {
		this.source = source;
	}

	/**
	 * Sets the target of the dependency to create/reconnect.
	 * 
	 * @param target
	 *            The dependency target as a Port.
	 */
	public void setTarget(Vertex target) {
		this.target = target;
	}

	@Override
	public void undo() {
		parentGraph.removeEdge(edge);
	}
}
