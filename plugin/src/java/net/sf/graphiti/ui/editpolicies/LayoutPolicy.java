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
package net.sf.graphiti.ui.editpolicies;

import net.sf.graphiti.model.Vertex;
import net.sf.graphiti.ui.commands.CreateCommand;
import net.sf.graphiti.ui.commands.MoveVertexCommand;
import net.sf.graphiti.ui.commands.OpenRefinementNewTabCommand;
import net.sf.graphiti.ui.editparts.VertexEditPart;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.jface.viewers.StructuredSelection;

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

	@Override
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {
		MoveVertexCommand command = null;

		if (child instanceof VertexEditPart) {
			VertexEditPart editPart = (VertexEditPart) child;
			Vertex vertex = (Vertex) editPart.getModel();

			command = new MoveVertexCommand(vertex, (Rectangle) constraint);
		}

		return command;
	}

	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		return new NonResizableEditPolicy();
	}

	public Command getCommand(Request request) {
		if (REQ_OPEN.equals(request.getType())) {
			OpenRefinementNewTabCommand command = new OpenRefinementNewTabCommand();
			command.setSelection(new StructuredSelection(getHost()));
			return command;
		} else {
			return super.getCommand(request);
		}
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		Object newObject = request.getNewObject();
		CreateCommand command = new CreateCommand();

		command.setNewObject(newObject);
		command.setModel(getHost().getModel());
		command.setBounds((Rectangle) getConstraintFor(request));

		return command;
	}
}
