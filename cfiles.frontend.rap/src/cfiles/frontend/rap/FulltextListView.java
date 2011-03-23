package cfiles.frontend.rap;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;

import cfiles.frontend.rap.core.SessionNexus;
import cfiles.frontend.rap.couchlink.CouchTreeLink;
import cfiles.frontend.rap.tree.TreeObject;
import cfiles.frontend.rap.tree.TreeParent;
import cfiles.frontend.rap.tree.ViewContentProvider;
import cfiles.frontend.rap.tree.ViewLabelProvider;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class FulltextListView extends ViewPart {
	public FulltextListView() {
	}

	public static final String ID = "cfiles.frontend.rap.fulltextListView";
	private CouchTreeLink couchTreeLink = SessionNexus.getInstance()
			.getCouchLink();
	private ViewContentProvider viewContentProvider = null;
	private Text filterText;
	private Composite banner;
	private Text text;
	private TreeViewer treeViewer;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite.heightHint = 34;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(2, false));

		text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button button = new Button(composite, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(e.widget + " - Default Selection");
				FulltextListView.this.treeViewer.setInput(FulltextListView.this
						.getViewContentProvider().getFullTextRoot(
								text.getText()));
				SessionNexus.getInstance().setFullTextTerm(text.getText());
				FulltextListView.this.treeViewer.refresh();
			}
		});
		button.setText("Find...");
		new Label(composite, SWT.NONE);

		this.treeViewer = new TreeViewer(parent, SWT.BORDER);
		Tree tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final CouchTreeLink ctl = new CouchTreeLink();
		this.viewContentProvider = new ViewContentProvider();
		this.viewContentProvider.setCouchLink(ctl);
		this.viewContentProvider.setHierarchySet(ctl.listDataStore());
		this.treeViewer.setContentProvider(this.getViewContentProvider());
		this.treeViewer.setLabelProvider(new ViewLabelProvider());
		this.treeViewer.setInput(this.viewContentProvider.getFullTextRoot(""));

		this.getSite().setSelectionProvider(this.treeViewer);

	}

	public ViewContentProvider getViewContentProvider() {
		return viewContentProvider;
	}

	public void setViewContentProvider(ViewContentProvider viewContentProvider) {
		this.viewContentProvider = viewContentProvider;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}
}