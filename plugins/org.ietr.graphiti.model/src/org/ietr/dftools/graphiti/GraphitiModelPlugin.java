package org.ietr.dftools.graphiti;

/*******************************************************************************
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008 - 2011)
 *
 * This software is a computer program whose purpose is to [describe
 * functionalities and technical features of your software].
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 *******************************************************************************/

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.ietr.dftools.graphiti.io.ConfigurationParser;
import org.ietr.dftools.graphiti.model.Configuration;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GraphitiModelPlugin extends AbstractUIPlugin {

	/**
	 * The shared instance.
	 */
	private static GraphitiModelPlugin plugin;

	/**
	 * The plug-in ID.
	 */
	public static final String PLUGIN_ID = "org.ietr.dftools.graphiti.model";

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static GraphitiModelPlugin getDefault() {
		return plugin;
	}

	/**
	 * map of configuration name to configuration
	 */
	private Map<String, Configuration> configurations;

	/**
	 * The constructor
	 */
	public GraphitiModelPlugin() {
		plugin = this;
	}

	/**
	 * Returns the configuration with the given name.
	 * 
	 * @param name
	 *            configuration name
	 */
	public Configuration getConfiguration(String name) {
		return configurations.get(name);
	}

	/**
	 * Returns the list of configurations.
	 * 
	 * @return A reference to the {@link Configuration} list.
	 */
	public Collection<Configuration> getConfigurations() {
		return configurations.values();
	}

	/**
	 * Parses the configurations available and (re)loads them.
	 * 
	 * @throws CoreException
	 *             If the file formats cannot be added to Eclipse content type
	 *             system.
	 */
	public void loadConfigurations() throws CoreException {
		ConfigurationParser parser = new ConfigurationParser();
		configurations = parser.getConfigurations();
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		loadConfigurations();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}
