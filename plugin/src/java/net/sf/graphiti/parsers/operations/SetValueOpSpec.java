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
package net.sf.graphiti.parsers.operations;

import net.sf.graphiti.model.PropertyBean;
import net.sf.graphiti.transactions.IOperationSpecification;
import net.sf.graphiti.transactions.Result;

/**
 * This class provides a 4-ary operation that sets a graph/vertex/edge property
 * value. Operands: object (PropertyBean), parameter type (Class&lt;?&gt;),
 * parameter name and value.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SetValueOpSpec implements IOperationSpecification {

	@Override
	public void execute(Object[] operands, Result result) {
		PropertyBean obj = (PropertyBean) operands[0];
		if (obj != null) {
			Class<?> clasz = (Class<?>) operands[1];
			String name = (String) operands[2];
			Object value = operands[3];
			obj.setValue(name, value);
		}
	}

	@Override
	public String getName() {
		return "set value";
	}

	@Override
	public int getNbOperands() {
		return 4;
	}

	@Override
	public boolean hasResult() {
		return false;
	}

}