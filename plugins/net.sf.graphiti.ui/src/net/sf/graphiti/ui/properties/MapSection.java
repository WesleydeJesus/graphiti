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
package net.sf.graphiti.ui.properties;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.sf.graphiti.model.AbstractObject;
import net.sf.graphiti.ui.editors.GraphEditor;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * This class defines a map section.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class MapSection extends AbstractSection {

	/**
	 * This class is a {@link CellLabelProvider} for a map.
	 * 
	 * @author Matthieu Wipliez
	 */
	private class MapCellLabelProvider extends CellLabelProvider {

		@Override
		@SuppressWarnings("unchecked")
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			Entry<Object, Object> entry = (Entry<Object, Object>) element;
			if (cell.getColumnIndex() == 0) {
				cell.setText(entry.getKey().toString());
			} else {
				Object value = entry.getValue();
				if (value == null) {
					value = "";
				}
				cell.setText(value.toString());
			}
		}

	}

	/**
	 * This class defines a content provider for a map.
	 * 
	 * @author Matthieu Wipliez
	 */
	private class MapContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			AbstractObject model = (AbstractObject) inputElement;
			Map<?, ?> map = (Map<?, ?>) model.getValue(parameterName);
			return map.entrySet().toArray();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	/**
	 * This class provides {@link EditingSupport} for keys of a map.
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	private class MapNameEditingSupport extends EditingSupport {

		private TextCellEditor editor;

		/**
		 * Creates a new {@link MapNameEditingSupport} on the given column
		 * viewer and table.
		 * 
		 * @param viewer
		 * @param table
		 */
		public MapNameEditingSupport(ColumnViewer viewer, Table table) {
			super(viewer);
			editor = new TextCellEditor(table);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected Object getValue(Object element) {
			Entry<Object, Object> entry = (Entry<Object, Object>) element;
			Object value = entry.getKey();
			if (value == null) {
				value = "";
			}

			return value.toString();
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void setValue(Object element, Object newKey) {
			AbstractObject model = (AbstractObject) getViewer().getInput();
			Map<Object, Object> oldMap = (Map<Object, Object>) model
					.getValue(parameterName);

			Entry<Object, Object> entry = (Entry<Object, Object>) element;
			if (entry.getKey().equals(newKey)) {
				return;
			}

			Map<Object, Object> newMap = new TreeMap<Object, Object>(oldMap);
			Object value = newMap.remove(entry.getKey());
			newMap.put(newKey, value);

			IWorkbenchPart part = getPart();
			if (part instanceof GraphEditor) {
				ParameterChangeValueCommand command = new ParameterChangeValueCommand(
						model, "Change name of value");
				command.setValue(parameterName, newMap);
				((GraphEditor) part).executeCommand(command);
			}
		}

	}

	/**
	 * This class provides {@link EditingSupport} for values of a map.
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	private class MapValueEditingSupport extends EditingSupport {

		private TextCellEditor editor;

		/**
		 * Creates a new {@link MapValueEditingSupport} on the given column
		 * viewer and table.
		 * 
		 * @param viewer
		 * @param table
		 */
		public MapValueEditingSupport(ColumnViewer viewer, Table table) {
			super(viewer);
			editor = new TextCellEditor(table);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected Object getValue(Object element) {
			Entry<Object, Object> entry = (Entry<Object, Object>) element;
			Object value = entry.getValue();
			if (value == null) {
				value = "";
			}
			return value.toString();
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void setValue(Object element, Object value) {
			AbstractObject model = (AbstractObject) getViewer().getInput();
			Map<Object, Object> oldMap = (Map<Object, Object>) model
					.getValue(parameterName);

			Entry<Object, Object> entry = (Entry<Object, Object>) element;
			if (entry.getValue().equals(value)) {
				return;
			}

			Map<Object, Object> newMap = new TreeMap<Object, Object>(oldMap);
			newMap.put(entry.getKey(), value);

			IWorkbenchPart part = getPart();
			if (part instanceof GraphEditor) {
				ParameterChangeValueCommand command = new ParameterChangeValueCommand(
						model, "Change value");
				command.setValue(parameterName, newMap);
				((GraphEditor) part).executeCommand(command);
			}
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	protected void buttonAddSelected() {
		String dialogTitle = "New value";
		String dialogMessage = "Please enter a value:";
		String initialValue = "";
		InputDialog dialog = new InputDialog(getShell(), dialogTitle,
				dialogMessage, initialValue, new IInputValidator() {

					@Override
					public String isValid(String newText) {
						return newText.isEmpty() ? "" : null;
					}

				});

		if (dialog.open() == InputDialog.OK) {
			AbstractObject model = (AbstractObject) tableViewer.getInput();
			Map<Object, Object> oldMap = (Map<Object, Object>) model
					.getValue(parameterName);
			Map<Object, Object> newMap = new TreeMap<Object, Object>(oldMap);
			newMap.put(dialog.getValue(), "");

			IWorkbenchPart part = getPart();
			if (part instanceof GraphEditor) {
				ParameterChangeValueCommand command = new ParameterChangeValueCommand(
						model, "Add element from map");
				command.setValue(parameterName, newMap);
				((GraphEditor) part).executeCommand(command);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void buttonRemoveSelected() {
		ISelection sel = tableViewer.getSelection();
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) sel;
			Object obj = ssel.getFirstElement();

			AbstractObject model = (AbstractObject) tableViewer.getInput();
			Map<Object, Object> oldMap = (Map<Object, Object>) model
					.getValue(parameterName);
			Map<Object, Object> newMap = new TreeMap<Object, Object>(oldMap);
			newMap.remove(((Entry<Object, Object>) obj).getKey());

			IWorkbenchPart part = getPart();
			if (part instanceof GraphEditor) {
				ParameterChangeValueCommand command = new ParameterChangeValueCommand(
						model, "Remove element from map");
				command.setValue(parameterName, newMap);
				((GraphEditor) part).executeCommand(command);
			}
		}
	}

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);

		createMapTable(getForm().getBody());
	}

	private void createMapTable(Composite parent) {
		final Table table = createTable(parent);

		// spans on 2 vertical cells
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2);
		data.horizontalIndent = 10;
		table.setLayoutData(data);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new MapContentProvider());
		tableViewer.setLabelProvider(new MapCellLabelProvider());

		MapCellLabelProvider labelProvider = new MapCellLabelProvider();

		// 1st column
		final TableColumn column1 = new TableColumn(table, SWT.NONE, 0);
		column1.setText("Name");
		column1.setWidth(200);

		// 2nd column
		final TableColumn column2 = new TableColumn(table, SWT.NONE, 1);
		column2.setText("Value");
		column2.setWidth(200);

		TableViewerColumn tvc1 = new TableViewerColumn(tableViewer, column1);
		tvc1.setLabelProvider(labelProvider);

		TableViewerColumn tvc2 = new TableViewerColumn(tableViewer, column2);
		tvc2.setLabelProvider(labelProvider);

		// editing support for first and second column
		tvc1.setEditingSupport(new MapNameEditingSupport(tvc1.getViewer(),
				table));
		tvc2.setEditingSupport(new MapValueEditingSupport(tvc2.getViewer(),
				table));
	}

}
