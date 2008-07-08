package net.sf.graphiti.ui.editpolicies;

import java.util.List;

import net.sf.graphiti.ui.commands.AddCommand;
import net.sf.graphiti.ui.commands.CreateCommand;
import net.sf.graphiti.ui.commands.MoveOrResizeCommand;
import net.sf.graphiti.ui.editparts.GraphEditPart;
import net.sf.graphiti.ui.editparts.VertexEditPart;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

/**
 * This class provides the policy of the layout used in the editor view. Namely
 * it implements the <code>createChangeConstraintCommand</code> and
 * <code>getCreateCommand</code> methods to move and create a graph
 * respectively.
 * 
 * @author Samuel Beaussier
 * @author Nicolas Isch
 * @author Matthieu Wipliez
 */
public class LayoutPolicy extends XYLayoutEditPolicy {

	public LayoutPolicy() {
	}

	private Command createAddCommand(EditPart child, Point moveDelta) {
		AddCommand add = new AddCommand();
		add.setChild(child.getModel());
		add.setMoveDelta(moveDelta);
		add.setParent(getHost().getModel());
		return add;
	}

	@Override
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {
		MoveOrResizeCommand command = null;

		if (child instanceof GraphEditPart || child instanceof VertexEditPart) {
			command = new MoveOrResizeCommand();
			command.setModel(child.getModel());
			command.setConstraint((Rectangle) constraint);
		}

		return command;
	}

	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		if (child instanceof GraphEditPart || child instanceof VertexEditPart) {
			return new ResizableEditPolicy();
		} else {
			return new NonResizableEditPolicy();
		}
	}

	@Override
	protected Command getAddCommand(Request req) {
		ChangeBoundsRequest request = (ChangeBoundsRequest) req;
		List<?> editParts = request.getEditParts();
		CompoundCommand command = new CompoundCommand();
		for (Object editPart : editParts) {
			command.add(createAddCommand((EditPart) editPart, request
					.getMoveDelta()));
		}
		return command.unwrap();
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		// if (createRequest.getExtendedData() != null) {
		// DragHelper dh = (DragHelper) createRequest.getExtendedData().get(
		// "DragHelper");
		// if (model instanceof Graph) {
		// if (((GraphGef) model).getDnd()) {
		//
		// if (dh.getDragScreenGraph() == null) {
		// GraphContainerModel container = ((GenericEditor) PlatformUI
		// .getWorkbench().getActiveWorkbenchWindow()
		// .getActivePage().getActiveEditor())
		// .getGraphContainer();
		//
		// DndCreateGraphCommand cmd = new DndCreateGraphCommand(
		// container, (GraphGef) model);
		// cmd
		// .setLocation(new Point(
		// ((Rectangle) getConstraintFor(createRequest)).x,
		// ((Rectangle) getConstraintFor(createRequest)).y));
		// cmd.findContainer(createRequest.getLocation());
		//
		// return cmd;
		// } else {
		// GraphContainerModel container = ((GenericEditor) PlatformUI
		// .getWorkbench().getActiveWorkbenchWindow()
		// .getActivePage().getActiveEditor())
		// .getGraphContainer();
		//
		// DndMoveGraphCommand cmd = new DndMoveGraphCommand(
		// container, (ScreenGraphModel) dh
		// .getDragScreenGraph().getModel(),
		// (GraphGef) dh.getData(), (GraphGef) model);
		// cmd
		// .setLocation(new Point(
		// ((Rectangle) getConstraintFor(createRequest)).x,
		// ((Rectangle) getConstraintFor(createRequest)).y));
		// cmd.findContainer(createRequest.getLocation());
		//
		// return cmd;
		// }
		// }
		// } else if (model instanceof ScreenGraphModel) {
		//
		// DndMoveScreenGraphCommand cmd = new DndMoveScreenGraphCommand(
		// dh.getName());
		// cmd.findDropEditPart(createRequest.getLocation());
		//
		// return cmd;
		// }
		// }

		Object newObject = request.getNewObject();
		CreateCommand command = new CreateCommand();

		command.setNewObject(newObject);
		command.setModel(getHost().getModel());
		command.setBounds((Rectangle) getConstraintFor(request));

		return command;
	}
}
