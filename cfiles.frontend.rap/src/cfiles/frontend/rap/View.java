package cfiles.frontend.rap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import cfiles.frontend.rap.couchlink.CouchTreeLink;
import cfiles.frontend.rap.tree.TreeObject;


public class View extends ViewPart implements ISelectionListener {
	public View() {
	}

	public static final String ID = "cfiles.frontend.rap.view";

	private final CouchTreeLink ctl = new CouchTreeLink();
	private Text text = null;
	private Text dateLabel = null;
	private Text nameLabel = null;
	private Text pathLabel = null;
	private Composite banner = null;

	public void createPartControl(Composite parent) {

		this.getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);

		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		top.setLayout(layout);
		// top banner
		this.banner = new Composite(top, SWT.NONE);
		banner.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL,
				GridData.VERTICAL_ALIGN_BEGINNING, true, false));
		layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.numColumns = 2;
		banner.setLayout(layout);

		// setup bold font
		Font boldFont = JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT);

		Label l = new Label(banner, SWT.WRAP);
		l.setText("File:");
		l.setFont(boldFont);
		this.nameLabel = new Text(banner, SWT.NONE);
		GridData gd_nameLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_nameLabel.widthHint = 150;
		nameLabel.setLayoutData(gd_nameLabel);
		this.nameLabel.setText("---");

		l = new Label(banner, SWT.WRAP);
		l.setText("Path:");
		l.setFont(boldFont);

		this.pathLabel = new Text(banner, SWT.NONE);
		GridData gd_pathLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_pathLabel.widthHint = 150;
		pathLabel.setLayoutData(gd_pathLabel);
		this.pathLabel.setText("---");

		l = new Label(banner, SWT.WRAP);
		l.setText("Last accessed:");
		l.setFont(boldFont);
		this.dateLabel = new Text(banner, SWT.NONE);
		GridData gd_dateLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_dateLabel.widthHint = 150;
		dateLabel.setLayoutData(gd_dateLabel);
		dateLabel.setText("");

		// message contents
		this.text = new Text(top, SWT.MULTI | SWT.WRAP);
		text.setText("This RCP Application was generated from the PDE Plug-in Project wizard. This sample shows how to:\n"
				+ "- add a top-level menu and toolbar with actions\n"
				+ "- add keybindings to actions\n"
				+ "- create views that can't be closed and\n"
				+ "  multiple instances of the same view\n"
				+ "- perspectives with placeholders for new views\n"
				+ "- use the default about dialog\n"
				+ "- create a product definition\n");
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	public void setFocus() {
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

				System.out.println("object in select: " + t.getIdentifier());

				final SimpleDateFormat sdf = new SimpleDateFormat(
						"dd.MM.yyyy HH:mm");
				final Map<String, String> value = this.ctl.getEntity(t
						.getIdentifier());

				System.out.println(t.getIdentifier()
						+ " -> lastmodified: "
						+ value.get("lastmodified")
						+ " -> date: "
						+ sdf.format((new Date(Long.parseLong(value
								.get("lastmodified"))))));
				this.dateLabel.setText(""
						+ sdf.format((new Date(Long.parseLong(value
								.get("lastmodified"))))));

				this.nameLabel.setText(value.get("name"));
				this.pathLabel.setText(value.get("path"));
				this.dateLabel.redraw();
				this.banner.redraw();

				this.text.setText("");
				for (String key : value.keySet()) {
					this.text.append(key + " : " + value.get(key) + "\n");
				}
			}
		}

	}
}
