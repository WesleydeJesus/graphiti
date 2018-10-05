package org.ietr.dftools.graphiti.ui.actions;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.ietr.dftools.graphiti.ui.editparts.VertexEditPart;

/**
 *
 * @author anmorvan
 *
 */
public abstract class AbstractCopyCutAction extends SelectionAction {

  public AbstractCopyCutAction(final IWorkbenchPart part) {
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
    setId(getCopyCutActionId());
    setText(getCopyCutText());
    setToolTipText(getCopyCutText());

    final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    setImageDescriptor(sharedImages.getImageDescriptor(getToolImageDesc()));
    setDisabledImageDescriptor(sharedImages.getImageDescriptor(getDisabledToolImageDesc()));
    setEnabled(false);
  }

  protected abstract String getDisabledToolImageDesc();

  protected abstract String getToolImageDesc();

  protected abstract String getCopyCutText();

  protected abstract String getCopyCutActionId();

}