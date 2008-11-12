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
package net.sf.graphiti.io.asn1;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a sequence of items.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Sequence extends Production {

	/**
	 * The sequence elements.
	 */
	private List<Item> elements;

	/**
	 * Creates a new sequence with the given name.
	 * 
	 * @param name
	 *            The production name.
	 */
	public Sequence(String name) {
		super(name);
		elements = new ArrayList<Item>();
	}
	
	@Override
	public void accept(ASN1Visitor visitor) {
		for (Item item : elements) {
			visitor.visit(item);
		}
	}

	/**
	 * Adds an element to this sequence.
	 * 
	 * @param element
	 *            An element as an {@link Item}.
	 */
	public void addElement(Item element) {
		elements.add(element);
	}

	@Override
	public String toString() {
		return super.toString() + " ::= " + elements;
	}

}
