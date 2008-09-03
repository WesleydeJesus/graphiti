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
package net.sf.graphiti.ui.views;

import java.util.List;
import java.util.Map;

import net.sf.graphiti.model.Edge;
import net.sf.graphiti.model.Graph;
import net.sf.graphiti.model.Parameter;
import net.sf.graphiti.model.Vertex;
import net.sf.graphiti.ui.editparts.EdgeEditPart;
import net.sf.graphiti.ui.editparts.GraphEditPart;
import net.sf.graphiti.ui.editparts.VertexEditPart;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * This class exposes the Graphiti version of property view. It is a derivative
 * work from the SampleView provided by Eclipse, with the addition of proper
 * table viewer support for graph variable declarations, vertex parameters, and
 * edge properties.
 * 
 * @author Matthieu Wipliez
 */
public class PropertyView extends AbstractPropertyView {

	/**
	 * Sorts parameters by name.
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	private class NameSorter extends ViewerComparator {

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Parameter p1 = (Parameter) e1;
			Parameter p2 = (Parameter) e2;

			return p1.getName().compareTo(p2.getName());
		}
	}

	public static final String ID = "net.sf.graphiti.ui.views.PropertyView";

	/**
	 * Creates the table viewer using the given table.
	 * 
	 * @param table
	 *            A {@link Table}.
	 */
	protected void createTableViewer(Table table) {
		// Creates the table viewer on the table.
		tableViewer = new TableViewer(table);

		// content provider
		PropertiesContentProvider contentProvider = new PropertiesContentProvider();
		tableViewer.setContentProvider(contentProvider);

		// Sort by parameter name
		tableViewer.setComparator(new NameSorter());

		// cell label provider
		PropertiesCellLabelProvider provider = new PropertiesCellLabelProvider();
		contentProvider.addPropertyChangeListener(provider);

		// first column
		TableColumn column = table.getColumn(0);
		TableViewerColumn tvc = new TableViewerColumn(tableViewer, column);
		tvc.setLabelProvider(provider);

		// second column
		column = table.getColumn(1);
		tvc = new TableViewerColumn(tableViewer, column);
		tvc.setLabelProvider(provider);

		// editing support for second column
		PropertiesEditingSupport editing = new PropertiesEditingSupport(tvc
				.getViewer(), table);
		contentProvider.addPropertyChangeListener(editing);
		tvc.setEditingSupport(editing);
	}

	@Override
	public void selectionChanged(Object object) {
		List<Parameter> parameters;
		if (object instanceof GraphEditPart) {
			Graph graph = (Graph) ((GraphEditPart) object).getModel();
			parameters = graph.getParameters();
		} else if (object instanceof VertexEditPart) {
			Vertex vertex = (Vertex) ((VertexEditPart) object).getModel();
			parameters = vertex.getParameters();
		} else if (object instanceof EdgeEditPart) {
			Edge edge = (Edge) ((EdgeEditPart) object).getModel();
			parameters = edge.getParameters();
		} else {
			return;
		}

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		if (page == null) {
			return;
		}

		IViewReference[] views = page.getViewReferences();
		for (IViewReference view : views) {
			if (view.getId().equals(ContextualPropertyView.ID)) {
				page.hideView(view);
			}
		}

		for (Parameter parameter : parameters) {
			if (parameter.getType() == Map.class) {
				try {
					page.showView(ContextualPropertyView.ID, parameter
							.getName(), IWorkbenchPage.VIEW_VISIBLE);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		}

		page.bringToTop(this);
	}
}