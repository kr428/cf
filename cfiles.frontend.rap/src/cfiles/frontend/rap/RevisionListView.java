package cfiles.frontend.rap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import cfiles.frontend.rap.core.SessionNexus;
import cfiles.frontend.rap.couchlink.CouchTreeLink.Revision;
import cfiles.frontend.rap.tree.TreeObject;

public class RevisionListView extends ViewPart implements ISelectionListener {

	public static final String ID = "cfiles.frontend.rap.revisionlistview";
	private TableViewer tableViewer;
	private Table table;
	private RevisionListViewerComparator comparator;

	public RevisionListView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		this.getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);
		this.tableViewer = new TableViewer(parent, SWT.BORDER
				| SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		tableViewer.setContentProvider(new RevisionContentProvider());
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		this.comparator = new RevisionListViewerComparator();
		this.tableViewer.setComparator(this.comparator);
		this.getSite().setSelectionProvider(this.tableViewer);
		this.setRevisions(new TreeMap<String, Revision>());

	}

	private void setRevisions(Map<String, Revision> revs) {
		final String[] headers = new String[] { "Version", "File" };
		final int[] boundaries = { 250, 450 };

		TableViewerColumn column;

		for (int i = 0; i < headers.length; i++) {
			column = this.createTableViewerColumn(headers[i], boundaries[i], i);
			if (i == 0) {
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object o) {
						return ((Revision) o).getRevision();
					}
				});
			}
			if (i == 1) {
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object o) {
						final String[] fileArray = ((Revision) o).getFile()
								.split("/");
						return fileArray[fileArray.length - 1];
					}
				});
			}
		}

	}

	public class RevisionContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
			// TODO Auto-generated method stub

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}

		@Override
		public Object[] getElements(Object inputElement) {

			if (inputElement instanceof SortedMap) {
				final List<Revision> revs = new ArrayList<Revision>();

				for (Object o : ((SortedMap) inputElement).values()) {
					revs.add((Revision) o);
				}

				return revs.toArray();
			}

			return null;
		}

	}

	private TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(
				tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(this.getSelectionAdapter(column, colNumber));
		return viewerColumn;

	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = RevisionListView.this.tableViewer.getTable()
						.getSortDirection();
				if (RevisionListView.this.tableViewer.getTable()
						.getSortColumn() == column) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				RevisionListView.this.tableViewer.getTable().setSortDirection(
						dir);
				RevisionListView.this.tableViewer.getTable().setSortColumn(
						column);
				RevisionListView.this.tableViewer.refresh();
			}
		};
		return selectionAdapter;
	}

	@Override
	public void setFocus() {
		try {
			this.tableViewer.setInput(SessionNexus
					.getInstance()
					.getCouchLink()
					.getRevisionHistoryForDocument(
							SessionNexus.getInstance().getItemInSelection()));
			this.tableViewer.refresh();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof TreeSelection) {
			final TreeSelection ts = (TreeSelection) selection;
			final TreeObject t = (TreeObject) ts.getFirstElement();

			this.tableViewer.setInput(SessionNexus
					.getInstance()
					.getCouchLink()
					.getRevisionHistoryForDocument(
							SessionNexus.getInstance().getItemInSelection()));
			this.tableViewer.refresh();

		}
	}

	public class Sorter {

		private String column;
		private int dir;

		public Sorter(String column, int dir) {
			super();
			this.column = column;
			this.dir = dir;
		}
	}

	private class RevisionListViewerComparator extends ViewerComparator {
		private int propertyIndex;
		private static final int DESCENDING = 1;
		private int direction = DESCENDING;

		public RevisionListViewerComparator() {
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Revision m1 = (Revision) e1;
			Revision m2 = (Revision) e2;
			int rc = 0;
			switch (propertyIndex) {
			case 0:
				rc = m1.getRevision().compareTo(m2.getRevision());
				break;
			case 1:
				rc = m1.getFile().compareTo(m2.getFile());
				break;
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}

	}

}
