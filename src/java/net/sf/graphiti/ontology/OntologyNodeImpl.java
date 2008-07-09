/**
 * 
 */
package net.sf.graphiti.ontology;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.graphiti.ontology.attributes.impl.ColorAttributeImpl;
import net.sf.graphiti.ontology.attributes.impl.FigureAttributeImpl;
import net.sf.graphiti.ontology.attributes.impl.ShapeAttributeImpl;
import net.sf.graphiti.ontology.enums.impl.ColorsImpl;
import net.sf.graphiti.ontology.enums.impl.DataTypesImpl;
import net.sf.graphiti.ontology.enums.impl.PositionImpl;
import net.sf.graphiti.ontology.enums.impl.ShapesImpl;
import net.sf.graphiti.ontology.nodes.impl.EdgeNodeImpl;
import net.sf.graphiti.ontology.nodes.impl.GraphNodeImpl;
import net.sf.graphiti.ontology.nodes.impl.ParserNodeImpl;
import net.sf.graphiti.ontology.nodes.impl.ParserParameterNodeImpl;
import net.sf.graphiti.ontology.nodes.impl.ParserRootNodeImpl;
import net.sf.graphiti.ontology.nodes.impl.SkipNodeImpl;
import net.sf.graphiti.ontology.nodes.impl.VertexNodeImpl;
import net.sf.graphiti.ontology.nodes.parameters.edges.impl.EdgeConnectionImpl;
import net.sf.graphiti.ontology.nodes.parameters.edges.impl.EdgeParameterNodeImpl;
import net.sf.graphiti.ontology.nodes.parameters.edges.impl.EdgeSourceConnectionImpl;
import net.sf.graphiti.ontology.nodes.parameters.edges.impl.EdgeTargetConnectionImpl;
import net.sf.graphiti.ontology.nodes.parameters.impl.ConstantParameterImpl;
import net.sf.graphiti.ontology.nodes.parameters.impl.DefaultParameterImpl;
import net.sf.graphiti.ontology.nodes.parameters.impl.IdParameterImpl;
import net.sf.graphiti.ontology.nodes.parameters.impl.ParserFixedParameterImpl;
import net.sf.graphiti.ontology.nodes.parameters.impl.PropertyBeanParameterImpl;
import net.sf.graphiti.ontology.parameters.impl.EdgeParameterImpl;
import net.sf.graphiti.ontology.parameters.impl.GraphParameterImpl;
import net.sf.graphiti.ontology.parameters.impl.ParameterImpl;
import net.sf.graphiti.ontology.parameters.impl.VertexParameterImpl;
import net.sf.graphiti.ontology.types.impl.EdgeTypeImpl;
import net.sf.graphiti.ontology.types.impl.GraphTypeImpl;
import net.sf.graphiti.ontology.types.impl.TypeImpl;
import net.sf.graphiti.ontology.types.impl.VertexTypeImpl;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * This class provides several methods.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class OntologyNodeImpl implements OntologyNode {

	private static Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

	static {
		// enums
		classes.put(OntologyFactory.getClassShapes(), ShapesImpl.class);
		classes.put(OntologyFactory.getClassColors(), ColorsImpl.class);
		classes.put(OntologyFactory.getClassDataTypes(), DataTypesImpl.class);
		classes.put(OntologyFactory.getClassPosition(), PositionImpl.class);
		// FigureAttribute
		classes.put(OntologyFactory.getClassColorAttribute(),
				ColorAttributeImpl.class);
		classes.put(OntologyFactory.getClassFigureAttribute(),
				FigureAttributeImpl.class);
		classes.put(OntologyFactory.getClassShapeAttribute(),
				ShapeAttributeImpl.class);

		// ParserNode
		classes.put(OntologyFactory.getClassEdgeNode(), EdgeNodeImpl.class);
		classes.put(OntologyFactory.getClassGraphNode(), GraphNodeImpl.class);
		classes.put(OntologyFactory.getClassParserNode(), ParserNodeImpl.class);
		classes.put(OntologyFactory.getClassParserParameterNode(),
				ParserParameterNodeImpl.class);
		classes.put(OntologyFactory.getClassParserRootNode(),
				ParserRootNodeImpl.class);
		classes.put(OntologyFactory.getClassSkipNode(), SkipNodeImpl.class);
		classes.put(OntologyFactory.getClassVertexNode(), VertexNodeImpl.class);
		
		classes.put(OntologyFactory.getClassParserFixedParameter(), ParserFixedParameterImpl.class);

		// PropertyBeanParameter
		classes.put(OntologyFactory.getClassConstantParameter(),
				ConstantParameterImpl.class);
		classes.put(OntologyFactory.getClassDefaultParameter(),
				DefaultParameterImpl.class);
		classes.put(OntologyFactory.getClassIdParameter(),
				IdParameterImpl.class);
		classes.put(OntologyFactory.getClassPropertyBeanParameter(),
				PropertyBeanParameterImpl.class);

		// EdgeParameterNode
		classes.put(OntologyFactory.getClassEdgeConnection(),
				EdgeConnectionImpl.class);
		classes.put(OntologyFactory.getClassEdgeParameterNode(),
				EdgeParameterNodeImpl.class);
		classes.put(OntologyFactory.getClassEdgeSourceConnection(),
				EdgeSourceConnectionImpl.class);
		classes.put(OntologyFactory.getClassEdgeTargetConnection(),
				EdgeTargetConnectionImpl.class);

		// Parameter
		classes.put(OntologyFactory.getClassEdgeParameter(),
				EdgeParameterImpl.class);
		classes.put(OntologyFactory.getClassGraphParameter(),
				GraphParameterImpl.class);
		classes.put(OntologyFactory.getClassParameter(), ParameterImpl.class);
		classes.put(OntologyFactory.getClassVertexParameter(),
				VertexParameterImpl.class);

		// Types
		classes.put(OntologyFactory.getClassEdgeType(), EdgeTypeImpl.class);
		classes.put(OntologyFactory.getClassGraphType(), GraphTypeImpl.class);
		classes.put(OntologyFactory.getClassType(), TypeImpl.class);
		classes.put(OntologyFactory.getClassVertexType(), VertexTypeImpl.class);
	}

	/**
	 * Converts the individuals accessible using the <code>it</code> iterator
	 * to the correct class.
	 * 
	 * @param it
	 *            An ExtendedIterator to a list of individuals.
	 * @return A set of objects.
	 */
	public static Set<?> convertIndividuals(ExtendedIterator it) {
		Set<Object> set = new HashSet<Object>();
		while (it.hasNext()) {
			RDFNode node = (RDFNode) it.next();
			if (node.canAs(Individual.class)) {
				Individual individual = (Individual) node.as(Individual.class);
				try {
					Class<?> clasz = classes.get(individual.getOntClass()
							.getURI());
					Constructor<?> constructors[] = clasz.getConstructors();
					Object ind = constructors[0].newInstance(individual);
					set.add(ind);
				} catch (InstantiationException e) {
					throw new IllegalArgumentException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException(e);
				} catch (InvocationTargetException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}

		return set;
	}

	private Individual individual;

	protected OntologyNodeImpl(Individual individual) {
		this.individual = individual;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OntologyNodeImpl) {
			return individual.getURI().equals(
					((OntologyNodeImpl) obj).individual.getURI());
		} else {
			return false;
		}
	}

	protected boolean getBooleanProperty(String propertyName) {
		DatatypeProperty property = individual.getOntModel()
				.getDatatypeProperty(propertyName);
		if (property == null) {
			System.err.println("property " + propertyName + " does not exist");
			throw new NullPointerException();
		}

		RDFNode node = individual.getPropertyValue(property);
		if (node.canAs(Literal.class)) {
			Literal lit = (Literal) node.as(Literal.class);
			return lit.getBoolean();
		}

		return false;
	}

	public String getIndividualName() {
		return individual.getLocalName();
	}

	protected Object getIndividualProperty(String propertyName) {
		ObjectProperty property = individual.getOntModel().getObjectProperty(
				propertyName);
		if (property == null) {
			System.err.println("property " + propertyName + " does not exist");
			throw new NullPointerException();
		}

		RDFNode node = individual.getPropertyValue(property);
		if (node != null && node.canAs(Individual.class)) {
			Individual individual = (Individual) node.as(Individual.class);
			try {
				Class<?> clasz = classes.get(individual.getOntClass().getURI());
				Constructor<?> constructors[] = clasz.getConstructors();
				return constructors[0].newInstance(individual);
			} catch (InstantiationException e) {
				throw new IllegalArgumentException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			} catch (InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			}
		}

		return null;
	}

	protected String getStringProperty(String propertyName) {
		DatatypeProperty property = individual.getOntModel()
				.getDatatypeProperty(propertyName);
		if (property == null) {
			System.err.println("property " + propertyName + " does not exist");
			throw new NullPointerException();
		}

		RDFNode node = individual.getPropertyValue(property);
		if (node != null && node.canAs(Literal.class)) {
			Literal lit = (Literal) node.as(Literal.class);
			return lit.getString();
		}

		return "";
	}

	public int hashCode() {
		return individual.getURI().hashCode();
	}

	@Override
	public boolean hasOntClass(String clasz) {
		OntClass ontClass = individual.getOntModel().getOntClass(clasz);

		// Tests all classes of this individual against ontClass
		ExtendedIterator it = individual.listOntClasses(false);
		boolean result = false;
		while (it.hasNext() && !result) {
			OntClass indOntClass = (OntClass) it.next();
			result = testOntClass(ontClass, indOntClass);
		}

		return result;
	}

	protected Set<?> listIndividuals(String propertyName) {
		ObjectProperty property = individual.getOntModel().getObjectProperty(
				propertyName);
		if (property == null) {
			System.err.println("property " + propertyName + " does not exist");
			throw new NullPointerException();
		}

		NodeIterator it = individual.listPropertyValues(property);
		return convertIndividuals(it);
	}

	/**
	 * Tests if the given ontology class <code>ontClass</code> is the same as
	 * the class of this individual (<code>indOntClass</code>) or one of its
	 * parents.
	 * 
	 * @param ontClass
	 *            The class to test against.
	 * @param indOntClass
	 *            The class of this individual (or one of its parents).
	 * @return True if ontClass.equals(indOntClass), or if ontClass.equals(a
	 *         parent of indOntClass).
	 */
	private boolean testOntClass(OntClass ontClass, OntClass indOntClass) {
		boolean result = false;
		if (ontClass.equals(indOntClass)) {
			result = true;
		} else {
			ExtendedIterator itClass = indOntClass.listSuperClasses();
			while (itClass.hasNext() && !result) {
				OntClass parentOntClass = (OntClass) itClass.next();
				result = testOntClass(ontClass, parentOntClass);
			}
		}

		return result;
	}

	public String toString() {
		return individual.getURI();
	}

}