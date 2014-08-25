/*
 * Copyright (c) 2011, IETR/INSA of Rennes
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
package org.ietr.dftools.graphiti.io;

import static org.ietr.dftools.graphiti.model.ObjectType.PARAMETER_ID;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.ietr.dftools.graphiti.model.Graph;
import org.ietr.dftools.graphiti.model.Vertex;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class reads the .layout file associated with graphs.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class LayoutReader {

	public void read(Graph graph, InputStream byteStream) {
		Map<String, Point> pointMap = new HashMap<String, Point>();
		Document document = DomHelper.parse(byteStream);
		Element layout = document.getDocumentElement();
		NodeList vertices = layout.getElementsByTagName("vertex");
		for (int i = 0; i < vertices.getLength(); i++) {
			Element vertex = (Element) vertices.item(i);
			String id = vertex.getAttribute("id");
			String x = vertex.getAttribute("x");
			String y = vertex.getAttribute("y");
			Point point = new Point(Integer.parseInt(x), Integer.parseInt(y));
			pointMap.put(id, point);
		}

		graph.setValue(Graph.PROPERTY_HAS_LAYOUT, true);
		for (Vertex vertex : graph.vertexSet()) {
			String id = (String) vertex.getValue(PARAMETER_ID);
			Point p = pointMap.get(id);
			vertex.setValue(Vertex.PROPERTY_SIZE, new Rectangle(p.x, p.y, 0, 0));
		}
	}

}