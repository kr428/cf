package cfiles.frontend.rap;

import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import cfiles.frontend.rap.core.SessionNexus;
import cfiles.frontend.rap.couchlink.CouchTreeLink;
import cfiles.frontend.rap.tree.TreeObject;


public class HeaderView extends ViewPart implements ISelectionListener {
	public HeaderView() {
	}

	public static final String ID = "cfiles.frontend.rap.headerview";

	private final CouchTreeLink ctl = SessionNexus.getInstance().getCouchLink();
	private Text text = null;
	private Text revLabel = null;
	private Text nameLabel = null;
	private Text pathLabel = null;
	private Text keywordLabel = null;
	private Composite banner = null;
	private Combo stateCombo = null;

	public String getKeywords() {
		return this.keywordLabel.getText();
	}

	public void createPartControl(Composite parent) {

		this.getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);

		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginLeft = 3;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		top.setLayout(layout);
		// top banner
		this.banner = new Composite(top, SWT.NONE);
		GridData gd_banner = new GridData(SWT.FILL,
				SWT.FILL, true, true);
		gd_banner.heightHint = 154;
		gd_banner.widthHint = 585;
		banner.setLayoutData(gd_banner);
		layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.numColumns = 2;
		banner.setLayout(layout);

		// setup bold font
		Font boldFont = JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT);

		Label l = new Label(banner, SWT.WRAP);
		l.setText("Document:");
		l.setFont(boldFont);
		this.nameLabel = new Text(banner, SWT.BORDER | SWT.READ_ONLY);
		GridData gd_nameLabel = new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1);
		gd_nameLabel.widthHint = 1;
		nameLabel.setLayoutData(gd_nameLabel);
		this.nameLabel.setText("---");
		this.nameLabel.setEditable(false);

		l = new Label(banner, SWT.WRAP);
		l.setText("Storage:");
		l.setFont(boldFont);

		this.pathLabel = new Text(banner, SWT.BORDER | SWT.READ_ONLY);
		GridData gd_pathLabel = new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1);
		gd_pathLabel.widthHint = 1;
		pathLabel.setLayoutData(gd_pathLabel);
		this.pathLabel.setText("---");
		this.pathLabel.setEditable(false);

		l = new Label(banner, SWT.WRAP);
		l.setText("Version:");
		l.setFont(boldFont);
		this.revLabel = new Text(banner, SWT.BORDER | SWT.READ_ONLY);
		GridData gd_dateLabel = new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1);
		gd_dateLabel.widthHint = 0;
		revLabel.setLayoutData(gd_dateLabel);
		revLabel.setText("");
		this.revLabel.setEditable(false);

		l = new Label(banner, SWT.WRAP);
		l.setText("Tags:");
		l.setFont(boldFont);

		// message contents
		this.keywordLabel = new Text(banner, SWT.BORDER);
		keywordLabel.setText("---");
		GridData gd_keywordLabel = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gd_keywordLabel.widthHint = 0;
		keywordLabel.setLayoutData(gd_keywordLabel);

		Label lblStatus = new Label(banner, SWT.NONE);
		lblStatus.setFont(boldFont);
		lblStatus.setText("State:");

		this.stateCombo = new Combo(banner, SWT.NONE);
		stateCombo.setItems(new String[] { "new", "old", "locked",
				"obsolete" });
		stateCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Button button = new Button(top, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SessionNexus
						.getInstance()
						.getCouchLink()
						.storeKeywords(
								SessionNexus.getInstance().getItemInSelection(),
								HeaderView.this.getKeywords().replace(" ", ",")
										.replace(",,", ","));
				try {
					SessionNexus
							.getInstance()
							.getCouchLink()
							.storeState(
									SessionNexus.getInstance()
											.getItemInSelection(),
									"" + stateCombo.getSelectionIndex());
				} catch (ArrayIndexOutOfBoundsException ex) {

				}
			}
		});
		button.setText("Save");

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

				final Map<String, String> value = this.ctl.getEntity(t
						.getIdentifier());

				this.revLabel.setText(value.get("_rev"));

				this.nameLabel.setText(value.get("name"));
				this.pathLabel.setText(value.get("path"));

				final StringBuilder keywords = new StringBuilder();

				keywords.append(value.get("keywords"));

				if (value.containsKey("meta.tika.Keywords")) {
					keywords.append(" ")
							.append(value.get("meta.tika.Keywords"));
				}

				if (value.containsKey("userstate")) {
					this.stateCombo.select(Integer.parseInt(value
							.get("userstate")));
					this.stateCombo.redraw();
				} else {
					this.stateCombo.deselectAll();
					this.stateCombo.redraw();
				}

				this.keywordLabel.setText(keywords.toString().replace("[", "")
						.replace("]", "").replace(" ", ""));

				this.keywordLabel.redraw();
				this.revLabel.redraw();
				this.banner.redraw();
			} else {
				this.nameLabel.setText(t.getName());
				this.pathLabel.setText(t.getName());
				this.revLabel.setText("");
				this.keywordLabel.setText("");
				this.banner.redraw();
			}
		}

	}
}
