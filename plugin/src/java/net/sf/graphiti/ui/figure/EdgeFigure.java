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
package net.sf.graphiti.ui.figure;

import java.util.List;

import net.sf.graphiti.model.Edge;
import net.sf.graphiti.model.Parameter;

import org.eclipse.draw2d.DelegatingLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;

/**
 * This class provides drawing for a dependency.
 * 
 * @author Samuel Beaussier
 * @author Nicolas Isch
 * @author Matthieu Wipliez
 */
public class EdgeFigure extends PolylineConnection {

	/**
	 * Creates the Figure associated to the connection
	 * 
	 * @param edge
	 *            The Edge model associated with this figure.
	 */
	public EdgeFigure(Edge edge) {
		// Sets Layout Manager
		this.setLayoutManager(new DelegatingLayout());
		if (edge != null) {
			if (edge.getValue("type") != null) {
				List<Parameter> parameters = edge.getDocumentConfiguration()
						.getEdgeParameters((String) edge.getValue("type"));
				for (Parameter parameter : parameters) {
					if (parameter.getPosition() != null) {
						Object value = edge.getValue(parameter.getName());
						if (value != null) {
							Label parameterLabel = new Label(value.toString());
							Object locator = new PropertyLocator(this,
									parameter.getPosition());
							add(parameterLabel, locator);
						}
					}
				}
			}
		}

		setTargetDecoration(new PolygonDecoration());
		setLineWidth(1);
		this.setValid(true);
	}
}
