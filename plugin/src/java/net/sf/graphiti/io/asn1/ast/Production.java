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
package net.sf.graphiti.io.asn1.ast;

import net.sf.graphiti.io.asn1.ASN1Visitable;
import net.sf.graphiti.io.asn1.ASN1Visitor;

/**
 * This class represents a grammar production. A production has a name and a
 * type.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Production implements ASN1Visitable {

	private String name;

	private Type type;

	/**
	 * Creates a new production.
	 * 
	 * @param name
	 *            A string representing the production name.
	 */
	public Production(String name) {
		this.name = name;
	}

	@Override
	public void accept(ASN1Visitor visitor) {
		visitor.visit(type);
	}

	/**
	 * Returns this production's name.
	 * 
	 * @return This production's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns this production's type.
	 * 
	 * @return This production's type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Sets this production's type.
	 * 
	 * @param type
	 *            A {@link Type}.
	 */
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return name + " ::= " + type;
	}
}
