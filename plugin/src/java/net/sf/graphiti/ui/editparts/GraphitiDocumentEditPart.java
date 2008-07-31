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
package net.sf.graphiti.ui.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import net.sf.graphiti.model.GraphitiDocument;
import net.sf.graphiti.ui.editpolicies.LayoutPolicy;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.EdgeList;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;

/**
 * This class extends {@link GraphEditPart} by setting its figure layout manager
 * to {@link GraphLayoutManager}. It also extends the
 * {@link EditPart#isSelectable()} method to return false, causing the selection
 * tool to act like the marquee tool when no particular children has been
 * selected.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class GraphitiDocumentEditPart extends AbstractGraphicalEditPart
		implements PropertyChangeListener {

	@Override
	public void activate() {
		super.activate();
		((GraphitiDocument) getModel()).addPropertyChangeListener(this);
	}

	/**
	 * Add the edges of the {@link GraphEditPart} children to the
	 * {@link EdgeList} <code>edges</code>.
	 * 
	 * @param edges
	 *            A list of {@link Edge}.
	 */
	public void addEdges(EdgeList edges) {
		for (Object child : getChildren()) {
			if (child instanceof GraphEditPart) {
				GraphEditPart part = (GraphEditPart) child;
				part.addEdges(edges);
			}
		}
	}

	/**
	 * Add the {@link GraphEditPart} nodes of this
	 * {@link GraphitiDocumentEditPart} to the {@link NodeList}
	 * <code>nodes</code>.
	 * 
	 * @param nodes
	 *            The list of {@link Node} in the Draw2D Graph.
	 */
	public void addNodes(NodeList nodes) {
		for (Object child : getChildren()) {
			if (child instanceof GraphEditPart) {
				GraphEditPart part = (GraphEditPart) child;
				part.addNodes(nodes);
			}
		}
	}

	/**
	 * Automatically layouts the graphs, vertices and edges in this graphiti
	 * document edit part.
	 * 
	 * @param direction
	 *            The direction, one of:
	 *            <UL>
	 *            <LI>{@link org.eclipse.draw2d.PositionConstants#EAST}
	 *            <LI>{@link org.eclipse.draw2d.PositionConstants#SOUTH}
	 *            </UL>
	 */
	public void automaticallyLayoutGraphs(int direction) {
		LayoutManager layoutMgr = new GraphLayoutManager(this, direction);
		layoutMgr.layout(getFigure());

		getFigure().setLayoutManager(new FreeformLayout());
		getFigure().revalidate();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutPolicy());
	}

	@Override
	protected IFigure createFigure() {
		// The figure associated with this graphiti document edit part is only a
		// free form layer
		Figure f = new FreeformLayer();
		f.setLayoutManager(new FreeformLayout());
		return f;
	}

	@Override
	public void deactivate() {
		super.deactivate();
		((GraphitiDocument) getModel()).removePropertyChangeListener(this);
	}

	@Override
	public List<Object> getModelChildren() {
		// A GraphitiDocument currently has only one child: a graph.
		GraphitiDocument document = (GraphitiDocument) getModel();
		List<Object> children = new ArrayList<Object>();
		children.add(document.getGraph());
		return children;
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
	}

	/**
	 * Updates all its children figures.
	 */
	public void updateFigures(int direction) {
		for (Object child : getChildren()) {
			if (child instanceof GraphEditPart) {
				GraphEditPart part = (GraphEditPart) child;
				part.updateFigures(direction);
			}
		}
	}
}
