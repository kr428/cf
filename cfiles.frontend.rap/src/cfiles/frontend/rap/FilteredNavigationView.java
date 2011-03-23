package cfiles.frontend.rap;

import java.io.InputStream;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import cfiles.frontend.rap.core.SessionNexus;
import cfiles.frontend.rap.tree.TreeObject;
import cfiles.frontend.rap.tree.ViewContentProvider;
import cfiles.frontend.rap.tree.ViewLabelProvider;

import org.eclipse.swt.widgets.Label;

public class FilteredNavigationView extends ViewPart implements
		ISelectionListener {
	public FilteredNavigationView() {
	}

	public static final String ID = "cfiles.frontend.rap.filteredNavigationView";
	private ViewContentProvider viewContentProvider = null;
	private Text filterText;
	private Composite banner;
	private TreeViewer treeViewer;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		this.getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);

		this.viewContentProvider = new ViewContentProvider();
		this.viewContentProvider.setCouchLink(SessionNexus.getInstance()
				.getCouchLink());
		this.viewContentProvider.setHierarchySet(SessionNexus.getInstance()
				.getCouchLink().listDataStore());

		parent.setLayout(new GridLayout(2, false));

		Button button = new Button(parent, SWT.NONE);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2,
				1));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FilteredNavigationView.this.getViewContentProvider()
						.setHierarchySet(
								SessionNexus.getInstance().getCouchLink()
										.listDataStore());

				FilteredNavigationView.this.treeViewer
						.setInput(FilteredNavigationView.this
								.getViewContentProvider().getRoot());
				FilteredNavigationView.this.treeViewer.refresh();
			}
		});
		// button.setText("[R]");
		button.setImage(this.loadImage("reload.gif"));

		this.treeViewer = new TreeViewer(parent, SWT.BORDER);
		Tree tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		this.treeViewer.setContentProvider(this.getViewContentProvider());
		this.treeViewer.setLabelProvider(new ViewLabelProvider());
		this.treeViewer.setInput(this.viewContentProvider.getRoot());

		this.getSite().setSelectionProvider(this.treeViewer);

	}

	public ViewContentProvider getViewContentProvider() {
		return viewContentProvider;
	}

	public void setViewContentProvider(ViewContentProvider viewContentProvider) {
		this.viewContentProvider = viewContentProvider;
	}

	private Image loadImage(final String name) {
		final InputStream imageStream = getClass().getClassLoader()
				.getResourceAsStream("/icons/" + name);
		if (imageStream != null) {
			return Graphics.getImage(name, imageStream);
		}
		return null;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof TreeSelection) {
			final TreeSelection ts = (TreeSelection) selection;
			final TreeObject t = (TreeObject) ts.getFirstElement();

			if (null == t || null == t.getIdentifier()) {
				System.out.println("ident == null fuer " + t + " ?");
				return;
			}

			if (!(t.getIdentifier().equals(""))) {
				SessionNexus.getInstance()
						.setItemInSelection(t.getIdentifier());
			}
		}

	}
}