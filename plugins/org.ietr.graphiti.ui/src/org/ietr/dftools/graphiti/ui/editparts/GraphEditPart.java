/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008 - 2011)
 *
 * This software is a computer program whose purpose is to help prototyping
 * parallel applications using dataflow formalism.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package org.ietr.dftools.graphiti.ui.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.CompoundDirectedGraphLayout;
import org.eclipse.draw2d.graph.EdgeList;
import org.eclipse.draw2d.graph.NodeList;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.ietr.dftools.graphiti.model.AbstractObject;
import org.ietr.dftools.graphiti.model.Graph;
import org.ietr.dftools.graphiti.ui.editpolicies.LayoutPolicy;
import org.ietr.dftools.graphiti.ui.properties.ModelPropertySource;
import org.ietr.dftools.graphiti.ui.properties.PropertiesConstants;

// TODO: Auto-generated Javadoc
/**
 * This class extends {@link AbstractGraphicalEditPart} by setting its figure layout manager to {@link GraphLayoutManager}. It also extends the
 * {@link EditPart#isSelectable()} method to return false, causing the selection tool to act like the marquee tool when no particular children has been
 * selected.
 *
 * @author Matthieu Wipliez
 *
 */
public class GraphEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener, ITabbedPropertySheetPageContributor {

  /**
   * The subgraph associated with this graph edit part. Set by {@link GraphEditPart#addNodes}.
   */
  private Subgraph subgraph;

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
   */
  @Override
  public void activate() {
    super.activate();
    ((Graph) getModel()).addPropertyChangeListener(this);
  }

  /**
   * Adds edges of this EditPart to the {@link EdgeList} of the parent.
   *
   * @param edges
   *          A list of edges in the graph.
   */
  void addEdges(final EdgeList edges) {
    for (final Object child : getChildren()) {
      if (child instanceof VertexEditPart) {
        final VertexEditPart part = (VertexEditPart) child;
        part.addEdges(edges);
      }
    }
  }

  /**
   * Adds nodes of this EditPart to the {@link NodeList} of the parent. This method sets size and padding for all nodes. Subgraph have insets equal to 20 (top)
   * and 0 otherwise.
   *
   * @param nodes
   *          A list of nodes in the graph.
   */
  @SuppressWarnings("unchecked")
  void addNodes(final NodeList nodes) {
    this.subgraph = new Subgraph(this);
    this.subgraph.innerPadding = new Insets(0, 0, 0, 0);
    this.subgraph.insets = new Insets(20, 0, 0, 0);
    final Figure figure = (Figure) getFigure();
    this.subgraph.setSize(figure.getPreferredSize());
    this.subgraph.setPadding(new Insets(2, 2, 2, 2));
    nodes.add(this.subgraph);

    for (final Object child : getChildren()) {
      if (child instanceof VertexEditPart) {
        final VertexEditPart part = (VertexEditPart) child;
        part.addNodes(nodes, this.subgraph);
      }
    }
  }

  /**
   * Automatically layouts the graphs, vertices and edges in this graphiti document edit part.
   *
   * @param direction
   *          The direction, one of:
   *          <UL>
   *          <LI>{@link org.eclipse.draw2d.PositionConstants#EAST}
   *          <LI>{@link org.eclipse.draw2d.PositionConstants#SOUTH}
   *          </UL>
   */
  public void automaticallyLayoutGraphs(final int direction) {
    final LayoutManager layoutMgr = new GraphLayoutManager(this, direction);
    layoutMgr.layout(getFigure());

    getFigure().setLayoutManager(new FreeformLayout());
    getFigure().revalidate();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
   */
  @Override
  protected void createEditPolicies() {
    installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutPolicy());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
   */
  @Override
  protected IFigure createFigure() {
    // The figure associated with this graph edit part is only a
    // free form layer
    final Figure f = new FreeformLayer();
    f.setLayoutManager(new FreeformLayout());

    // Create the static router for the connection layer
    final ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
    final ShortestPathConnectionRouter router = new ShortestPathConnectionRouter(f);
    router.setSpacing(2);
    // ManhattanConnectionRouter router = new ManhattanConnectionRouter();
    connLayer.setConnectionRouter(router);

    return f;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
   */
  @Override
  public void deactivate() {
    super.deactivate();
    ((Graph) getModel()).removePropertyChangeListener(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
   */
  @Override
  @SuppressWarnings("rawtypes")
  public Object getAdapter(final Class adapter) {
    if (adapter == IPropertySource.class) {
      return new ModelPropertySource((AbstractObject) getModel());
    }
    return super.getAdapter(adapter);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor#getContributorId()
   */
  @Override
  public String getContributorId() {
    return PropertiesConstants.CONTRIBUTOR_ID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
   */
  @Override
  public List<Object> getModelChildren() {
    final Graph graph = (Graph) getModel();
    final List<Object> children = new ArrayList<>();
    children.addAll(graph.vertexSet());
    return children;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#isSelectable()
   */
  @Override
  public boolean isSelectable() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(Graph.PROPERTY_ADD)) {
      refresh();
    } else if (evt.getPropertyName().equals(Graph.PROPERTY_REMOVE)) {
      refresh();
    }
  }

  /**
   * This method is called by the {@link GraphLayoutManager}, and applies back the changes of the {@link CompoundDirectedGraphLayout} algorithm to the different
   * figures, by setting their bounds.
   */
  void updateFigures() {
    for (final Object child : getChildren()) {
      if (child instanceof VertexEditPart) {
        final VertexEditPart part = (VertexEditPart) child;
        part.updateFigures();
      }
    }
  }

}
