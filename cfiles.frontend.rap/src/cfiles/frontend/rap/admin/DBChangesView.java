package cfiles.frontend.rap.admin;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;

import cfiles.frontend.rap.core.SessionNexus;


public class DBChangesView extends ViewPart {

	public static final String ID = "cfiles.frontend.rap.admin.dbchangesview";
	private Text text;

	public void setText(String text) {
		this.text.setText(text);
	}

	public String getText() {
		return this.text.getText();
	}

	public DBChangesView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		text = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// TODO Auto-generated method stub
		this.setText(SessionNexus.getInstance().getCouchLink().getDBChanges());

	}

	@Override
	public void setFocus() {
		this.setText(SessionNexus.getInstance().getCouchLink().getDBChanges());
	}

}
