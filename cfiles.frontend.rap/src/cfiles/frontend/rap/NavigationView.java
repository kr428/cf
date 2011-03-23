package cfiles.frontend.rap;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import cfiles.frontend.rap.couchlink.CouchTreeLink;
import cfiles.frontend.rap.tree.TreeObject;
import cfiles.frontend.rap.tree.TreeParent;
import cfiles.frontend.rap.tree.ViewContentProvider;
import cfiles.frontend.rap.tree.ViewLabelProvider;


public class NavigationView extends ViewPart {

	public static final String ID = "cfiles.frontend.rap.navigationView";
	private CouchTreeLink couchTreeLink = new CouchTreeLink();
	private ViewContentProvider viewContentProvider = null;

	private TreeViewer viewer;
	private Text filterText;
	private Composite banner;

	/**
	 * We will set up a dummy model to initialize tree heararchy. In real code,
	 * you will connect to a real model and expose its hierarchy.
	 */
	private TreeObject createDummyModel() {
		TreeObject to1 = new TreeObject("Inbox");
		TreeObject to2 = new TreeObject("Drafts");
		TreeObject to3 = new TreeObject("Sent");
		TreeParent p1 = new TreeParent("me@this.com");
		p1.addChild(to1);
		p1.addChild(to2);
		p1.addChild(to3);

		TreeObject to4 = new TreeObject("Inbox");
		TreeParent p2 = new TreeParent("other@aol.com");
		p2.addChild(to4);

		TreeParent root = new TreeParent("");
		root.addChild(p1);
		root.addChild(p2);
		return root;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
//		Composite top = new Composite(parent, SWT.NONE);
//		GridLayout layout = new GridLayout();
//		this.banner = new Composite(top, SWT.NONE);
//		GridData gd_banner = new GridData(GridData.HORIZONTAL_ALIGN_FILL,
//				GridData.VERTICAL_ALIGN_BEGINNING, true, false);
//		banner.setLayoutData(gd_banner);
//		layout = new GridLayout();
//		layout.marginHeight = 5;
//		layout.marginWidth = 10;
//		layout.numColumns = 2;
//		banner.setLayout(layout);
//
//		GridData gd_filterText = new GridData(SWT.LEFT, SWT.CENTER, false,
//				false, 1, 1);
//		this.filterText = new Text(banner, SWT.NONE);
//		this.filterText.setText(" ");
//		this.filterText.setLayoutData(gd_filterText);

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);

		final CouchTreeLink ctl = new CouchTreeLink();
		this.viewContentProvider = new ViewContentProvider();
		this.viewContentProvider.setCouchLink(ctl);
		this.viewContentProvider.setHierarchySet(ctl.listDataStore());

		viewer.setContentProvider(this.viewContentProvider);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(this.viewContentProvider.getRoot());

		this.getSite().setSelectionProvider(this.viewer);

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}