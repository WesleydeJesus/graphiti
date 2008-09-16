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
package net.sf.graphiti.model;

import java.util.List;

/**
 * This class represents a vertex.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Vertex extends PropertyBean {

	/**
	 * String for the "color" attribute. Defines the vertex color.
	 */
	public static final String ATTRIBUTE_COLOR = "color";

	/**
	 * String for the "height" attribute. Defines the vertex height.
	 */
	public static final String ATTRIBUTE_HEIGHT = "height";

	/**
	 * String for the "shape" attribute. Defines the vertex shape.
	 */
	public static final String ATTRIBUTE_SHAPE = "shape";

	/**
	 * String for the "width" attribute. Defines the vertex width.
	 */
	public static final String ATTRIBUTE_WIDTH = "width";

	/**
	 * String for the "id" parameter. Defines the vertex id.
	 */
	public static final String PARAMETER_ID = "id";

	/**
	 * String for the "refinement" parameter. Defines the vertex refinement.
	 */
	public static final String PARAMETER_REFINEMENT = "refinement";

	/**
	 * String for the "type" parameter. Defines the vertex type.
	 */
	public static final String PARAMETER_TYPE = "type";

	/**
	 * String for the "destination vertex" property. Set when a vertex becomes
	 * the destination of a dependency.
	 */
	public static final String PROPERTY_DST_VERTEX = "destination vertex";

	/**
	 * String for the "size" property. Set when the location/size of a vertex
	 * changes. This property should only be stored when the vertex has
	 * temporarily no associated edit part, like when cut/copy/paste occurs.
	 * Otherwise, one shall use the figure bounds.
	 */
	public static final String PROPERTY_SIZE = "size";

	/**
	 * String for the "source vertex" property. Set when a vertex becomes the
	 * source of a dependency.
	 */
	public static final String PROPERTY_SRC_VERTEX = "source vertex";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The parent graph of this vertex.
	 */
	Graph parent;

	/**
	 * Creates a new Vertex with no type.
	 * 
	 */
	public Vertex() {
		this("");
	}

	/**
	 * Creates a vertex with the given type.
	 * 
	 * @param type
	 *            The vertex type.
	 */
	public Vertex(String type) {
		this.setValue(PARAMETER_TYPE, type);
	}

	/**
	 * Creates a new vertex which is a copy of the given vertex.
	 * 
	 * @param vertex
	 *            The source vertex.
	 */
	public Vertex(Vertex vertex) {
		super(vertex);
		parent = vertex.parent;
	}

	/**
	 * Returns the value of an attribute associated with this vertex type and
	 * the given attribute name <code>attributeName</code>.
	 * 
	 * @param attributeName
	 *            The name of an attribute.
	 * @return The value of the attribute as an object.
	 */
	public Object getAttribute(String attributeName) {
		Configuration config = parent.getConfiguration();
		return config.getVertexAttribute(getType(), attributeName);
	}

	/**
	 * Returns the configuration associated with this Vertex.
	 * 
	 * @return The configuration associated with this Vertex.
	 */
	public Configuration getConfiguration() {
		return parent.getConfiguration();
	}

	/**
	 * Returns the parameter in this vertex type with the given name.
	 * 
	 * @param parameterName
	 *            The parameter name.
	 * @return A {@link Parameter}.
	 */
	public Parameter getParameter(String parameterName) {
		Configuration config = parent.getConfiguration();
		return config.getVertexParameter(getType(), parameterName);
	}

	/**
	 * Returns a list of parameters associated with this vertex type.
	 * 
	 * @return A List of Parameters.
	 */
	public List<Parameter> getParameters() {
		Configuration config = parent.getConfiguration();
		return config.getVertexParameters(getType());
	}

	/**
	 * Returns the parent {@link Graph} of this Vertex.
	 * 
	 * @return The parent {@link Graph} of this Vertex.
	 */
	public Graph getParent() {
		return parent;
	}

	/**
	 * Returns this vertex's type.
	 * 
	 * @return This vertex's type.
	 */
	public String getType() {
		return (String) this.getValue(PARAMETER_TYPE);
	}

	public String toString() {
		return getType() + " : " + getValue(PARAMETER_ID);
	}

}
