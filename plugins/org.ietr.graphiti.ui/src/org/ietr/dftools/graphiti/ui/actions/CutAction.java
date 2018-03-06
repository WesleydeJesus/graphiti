/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008 - 2010)
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
package org.ietr.dftools.graphiti.ui.actions;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.ietr.dftools.graphiti.ui.commands.copypaste.CutCommand;
import org.ietr.dftools.graphiti.ui.editparts.VertexEditPart;

/**
 * This class provides an implementation of the cut action.
 *
 * @author Samuel Beaussier
 * @author Nicolas Isch
 * @author Matthieu Wipliez
 *
 */
public class CutAction extends SelectionAction {

  /**
   * Constructs a CutAction and associates it with the given workbench part.
   *
   * @param part
   *          The workbench part.
   */
  public CutAction(final IWorkbenchPart part) {
    super(part);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
   */
  @Override
  protected boolean calculateEnabled() {
    // enabled when at least one object is selected
    final ISelection selection = getSelection();
    if (selection instanceof IStructuredSelection) {
      final IStructuredSelection ssel = (IStructuredSelection) selection;
      return ((!ssel.isEmpty()) && (ssel.getFirstElement() instanceof VertexEditPart));
    } else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#init()
   */
  @Override
  protected void init() {
    setId(ActionFactory.CUT.getId());
    setText("Cut");
    setToolTipText("Cut");

    final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
    setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
    setEnabled(false);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    // execute the cut command
    final CutCommand command = new CutCommand(getSelectedObjects());
    execute(command);
  }
}
