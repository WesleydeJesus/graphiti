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
package net.sf.graphiti.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.graphiti.util.FileLocator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class provides methods to transform an XML file or a DOM element to:
 * <ul>
 * <li>a string</li>
 * <li>a new document</li>
 * <li>a child of an existing element</li>
 * </ul>
 * 
 * @author Matthieu Wipliez
 * 
 */
public class XsltTransformer {

	private Transformer transformer;

	/**
	 * Creates a new {@link XsltTransformer} with an XSLT stylesheet contained
	 * in the file whose name is <code>fileName</code>.
	 * 
	 * @param fileName
	 *            The XSLT stylesheet file name.
	 * @throws TransformerConfigurationException
	 *             Thrown if there are errors when parsing the Source or it is
	 *             not possible to create a {@link Transformer} instance.
	 */
	public XsltTransformer(String fileName)
			throws TransformerConfigurationException {
		File file = FileLocator.getFile(fileName);
		TransformerFactory factory = TransformerFactory.newInstance(
				"net.sf.saxon.TransformerFactoryImpl", null);
		StreamSource xsltSource = new StreamSource(file);
		transformer = factory.newTransformer(xsltSource);
	}

	/**
	 * Transforms the given DOM element (and its children) and append the result
	 * to the <code>target</code> node's children.
	 * 
	 * @param source
	 *            The source element.
	 * @param target
	 *            The target node.
	 * @throws TransformerException
	 *             If an unrecoverable error occurs during the course of the
	 *             transformation.
	 */
	public void transformDomToDom(Element source, Node target)
			throws TransformerException {
		Source xmlSource = new DOMSource(source);
		Result outputTarget = new DOMResult(target);
		transformer.transform(xmlSource, outputTarget);
	}

	/**
	 * Transforms the given DOM element (and its children) and returns the
	 * result. The result element is in a different document than the source's
	 * owner document.
	 * 
	 * @param source
	 *            The source element to transform.
	 * @return The document element (and its children) resulting from the
	 *         transformation.
	 * @throws ClassCastException
	 *             If any specified class does not implement
	 *             DOMImplementationSource
	 * @throws ClassNotFoundException
	 *             If any specified class can not be found
	 * @throws InstantiationException
	 *             If any specified class is an interface or abstract class
	 * @throws IllegalAccessException
	 *             If the default constructor of a specified class is not
	 *             accessible
	 * @throws TransformerException
	 *             If an unrecoverable error occurs during the course of the
	 *             transformation.
	 */
	public Element transformDomToDom(Element source) throws ClassCastException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, TransformerException {
		// create document
		Document document = DomHelper.createDocument("", "dummy");
		document.removeChild(document.getDocumentElement());
		transformDomToDom(source, document);
		return document.getDocumentElement();
	}

	/**
	 * Transforms the given DOM element (and its children) and returns the
	 * result as a string. The string may contain text or XML.
	 * 
	 * @param element
	 *            The source element to transform.
	 * @return The string resulting from the transformation.
	 * @throws TransformerException
	 *             If an unrecoverable error occurs during the course of the
	 *             transformation.
	 */
	public String transformDomToString(Element element)
			throws TransformerException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		DOMSource source = new DOMSource(element);
		StreamResult result = new StreamResult(os);
		transformer.transform(source, result);
		try {
			os.close();
		} catch (IOException e) {
			// never happens on a byte array output stream
		}

		String value = os.toString();
		return value;
	}

}