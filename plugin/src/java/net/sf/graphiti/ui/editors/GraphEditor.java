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
package net.sf.graphiti.ui.editors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import net.sf.graphiti.io.GenericGraphParser;
import net.sf.graphiti.io.GenericGraphWriter;
import net.sf.graphiti.model.AbstractType;
import net.sf.graphiti.model.Configuration;
import net.sf.graphiti.model.Graph;
import net.sf.graphiti.model.Parameter;
import net.sf.graphiti.ui.GraphitiPlugin;
import net.sf.graphiti.ui.actions.CopyAction;
import net.sf.graphiti.ui.actions.CutAction;
import net.sf.graphiti.ui.actions.OpenRefinementNewTabAction;
import net.sf.graphiti.ui.actions.PasteAction;
import net.sf.graphiti.ui.actions.SetRefinementAction;
import net.sf.graphiti.ui.editparts.EditPartFactoryImpl;
import net.sf.graphiti.ui.editparts.GraphEditPart;
import net.sf.graphiti.ui.views.ComplexPropertyView;
import net.sf.graphiti.ui.wizards.SaveAsWizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * This class provides the graph editor.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class GraphEditor extends GraphicalEditorWithFlyoutPalette implements
		IShowEditorInput {

	/**
	 * The editor ID
	 */
	public static final String ID = "net.sf.graphiti.ui.editors.GraphEditor";

	private Graph graph;

	private ZoomManager manager;

	private ThumbnailOutlinePage outlinePage;

	private PaletteRoot paletteRoot;

	private IStatus status;

	/**
	 * Create an editor
	 */
	public GraphEditor() {
		setEditDomain(new DefaultEditDomain(this));
		getPalettePreferences().setPaletteState(
				FlyoutPaletteComposite.STATE_PINNED_OPEN);
	}

	/**
	 * Automatically layout the graph edited with the given direction.
	 * 
	 * @param direction
	 *            The direction, one of:
	 *            <UL>
	 *            <LI>{@link org.eclipse.draw2d.PositionConstants#EAST}
	 *            <LI>{@link org.eclipse.draw2d.PositionConstants#SOUTH}
	 *            </UL>
	 */
	public void automaticallyLayout(int direction) {
		GraphEditPart doc = (GraphEditPart) getGraphicalViewer()
				.getRootEditPart().getContents();
		doc.automaticallyLayoutGraphs(direction);
	}

	@Override
	public void commandStackChanged(EventObject event) {
		firePropertyChange(PROP_DIRTY);
		super.commandStackChanged(event);
	}

	@Override
	protected void configureGraphicalViewer() {
		double[] zoomLevels;
		ArrayList<String> zoomContributions;

		super.configureGraphicalViewer();
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new EditPartFactoryImpl());

		ScalableFreeformRootEditPart rootEditPart = new ScalableFreeformRootEditPart();
		viewer.setRootEditPart(rootEditPart);

		manager = rootEditPart.getZoomManager();
		getActionRegistry().registerAction(new ZoomInAction(manager));
		getActionRegistry().registerAction(new ZoomOutAction(manager));

		// List of possible zoom levels. 1 = 100%
		zoomLevels = new double[] { 0.1, 0.15, 0.25, 0.5, 0.75, 1.0, 1.5, 2.0 };
		manager.setZoomLevels(zoomLevels);

		// Predefined zoom levels
		zoomContributions = new ArrayList<String>();
		zoomContributions.add(ZoomManager.FIT_ALL);
		zoomContributions.add(ZoomManager.FIT_HEIGHT);
		zoomContributions.add(ZoomManager.FIT_WIDTH);
		manager.setZoomLevelContributions(zoomContributions);

		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.CTRL),
				MouseWheelZoomHandler.SINGLETON);

		// Context menu
		ContextMenuProvider provider = new GraphEditorContextMenuProvider(
				viewer, getActionRegistry());
		viewer.setContextMenu(provider);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void createActions() {
		// create actions that will be used inside the editor, for instance in a
		// contextual menu
		super.createActions();

		ActionRegistry registry = getActionRegistry();
		Class<?> actions[] = { CopyAction.class, CutAction.class,
				OpenRefinementNewTabAction.class, PasteAction.class,
				PrintAction.class, SelectAllAction.class,
				SetRefinementAction.class };

		// Constructs all actions
		for (Class<?> clz : actions) {
			try {
				Constructor<?> ctor = clz.getConstructor(IWorkbenchPart.class);

				IAction action = (IAction) ctor.newInstance(this);
				registry.registerAction(action);
				getSelectionActions().add(action.getId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			@Override
			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				viewer
						.addDragSourceListener(new TemplateTransferDragSourceListener(
								viewer));
			}
		};
	}

	/**
	 * Displays a new {@link ComplexPropertyView} for the given parameter on the
	 * given page.
	 * 
	 * @param object
	 */
	private void displayViews() {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		Configuration configuration = graph.getConfiguration();
		List<Object> types = new ArrayList<Object>();
		types.addAll(configuration.getEdgeTypes());
		types.addAll(configuration.getGraphTypes());
		types.addAll(configuration.getVertexTypes());
		for (Object obj : types) {
			List<Parameter> parameters = ((AbstractType) obj).getParameters();
			for (Parameter parameter : parameters) {
				Class<?> type = parameter.getType();
				if (type == Map.class || type == List.class) {
					String parameterName = parameter.getName();
					try {
						IViewPart part = page.showView(ComplexPropertyView.ID,
								parameterName, IWorkbenchPage.VIEW_VISIBLE);
						((ComplexPropertyView) part).setParameter(parameter,
								(AbstractType) obj);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		IFile file = ((IFileEditorInput) getEditorInput()).getFile();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GenericGraphWriter writer = new GenericGraphWriter(graph);
		try {
			writer.write(file.getLocation().toString(), out);
			file.setContents(new ByteArrayInputStream(out.toByteArray()), true,
					false, monitor);
			try {
				out.close();
			} catch (IOException e) {
				// Can never occur on a ByteArrayOutputStream
			}
			getCommandStack().markSaveLocation();
			return;
		} catch (ClassCastException e) {
			errorMessage(
					"There was a problem with the creation of a DOM document.",
					e);
		} catch (ClassNotFoundException e) {
			errorMessage("A DOM class could not be found.", e);
		} catch (CoreException e) {
			errorMessage("Could not set the file contents.", e);
		} catch (IllegalAccessException e) {
			errorMessage("A DOM class could not be accessed.", e);
		} catch (InstantiationException e) {
			errorMessage("A DOM class could not be instantiated.", e);
		} catch (TransformerException e) {
			errorMessage("An unrecoverable error occurred during "
					+ "the course of the transformation.", e);
		}

		monitor.setCanceled(true);
	}

	@Override
	public void doSaveAs() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editor = page.getActiveEditor();

		SaveAsWizard wizard = new SaveAsWizard();
		wizard.init(workbench, new StructuredSelection(editor));

		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.open();
	}

	/**
	 * Displays an error message with the given exception.
	 * 
	 * @param message
	 *            A description of the error.
	 * @param exception
	 *            An exception.
	 */
	private void errorMessage(String message, Throwable exception) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		Shell shell = window.getShell();

		IStatus status = new Status(IStatus.ERROR, GraphitiPlugin.PLUGIN_ID,
				message, exception);
		ErrorDialog dialog = new ErrorDialog(shell, "Save error",
				"The file could not be saved.", status, IStatus.ERROR);
		dialog.open();
	}

	/**
	 * Executes the given command.
	 * 
	 * @param command
	 */
	public void executeCommand(Command command) {
		CommandStack stack = getEditDomain().getCommandStack();
		stack.execute(command);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class type) {
		if (type == ZoomManager.class) {
			return ((ScalableFreeformRootEditPart) getGraphicalViewer()
					.getRootEditPart()).getZoomManager();
		} else if (type == IContentOutlinePage.class) {
			outlinePage = new ThumbnailOutlinePage(this);
			return outlinePage;
		} else {
			return super.getAdapter(type);
		}
	}

	/**
	 * Returns the contents of this editor.
	 * 
	 * @return The contents of this editor.
	 */
	public Graph getContents() {
		return graph;
	}

	@Override
	public GraphicalViewer getGraphicalViewer() {
		return super.getGraphicalViewer();
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		if (paletteRoot == null) {
			paletteRoot = GraphitiPalette.getPaletteRoot(graph);
		}
		return paletteRoot;
	}

	@Override
	public SelectionSynchronizer getSelectionSynchronizer() {
		return super.getSelectionSynchronizer();
	}

	/**
	 * Gives the current zoom factor
	 * 
	 * @return double
	 */
	public double getZoom() {
		return manager.getZoom();
	}

	@Override
	protected void initializeGraphicalViewer() {
		GraphicalViewer viewer = getGraphicalViewer();

		if (graph == null) {
			viewer.setContents(status);
		} else {
			viewer.setContents(graph);
			if (!(Boolean) graph.getValue(Graph.PROPERTY_HAS_LAYOUT)) {
				automaticallyLayout(PositionConstants.EAST);
			}
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		IFile file = ((IFileEditorInput) input).getFile();
		setPartName(file.getName());
		try {
			GenericGraphParser parser = new GenericGraphParser(GraphitiPlugin
					.getDefault().getConfigurations());
			graph = parser.parse(file);

			// Updates the palette
			getEditDomain().setPaletteRoot(getPaletteRoot());

			firePropertyChange(PROP_INPUT);
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					displayViews();
				}
			});
		} catch (Throwable e) {
			status = new Status(Status.ERROR, GraphitiPlugin.PLUGIN_ID,
					"An error occurred while parsing the file", e);
		}
	}

	/**
	 * Sets the zoom to see the entire width of the graph in editor
	 */
	public void setWidthZoom() {
		manager.setZoomAsText(ZoomManager.FIT_WIDTH);
	}

	@Override
	public void showEditorInput(IEditorInput editorInput) {
		setInput(editorInput);
		initializeGraphicalViewer();
	}
}
