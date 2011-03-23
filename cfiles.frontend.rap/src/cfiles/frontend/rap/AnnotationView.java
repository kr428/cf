package cfiles.frontend.rap;

import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import cfiles.frontend.rap.core.SessionNexus;
import cfiles.frontend.rap.tree.TreeObject;


public class AnnotationView extends ViewPart implements ISelectionListener {

	public static final String ID = "cfiles.frontend.rap.annotationview";

	private Text text;

	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	public AnnotationView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		this.getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);

		text = new Text(parent, SWT.BORDER | SWT.MULTI);
		GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_text.heightHint = 100;
		text.setLayoutData(gd_text);

		Button button = new Button(parent, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("text: "
						+ AnnotationView.this.getText().getText());

				SessionNexus
						.getInstance()
						.getCouchLink()
						.storeAnnotation(
								SessionNexus.getInstance().getItemInSelection(),
								AnnotationView.this.getText().getText());

			}
		});
		button.setAlignment(SWT.RIGHT);
		button.setText("Save");
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

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

				final Map<String, String> rslt = SessionNexus.getInstance()
						.getEntityInSelection();

				if (rslt.containsKey("annotation")) {
					this.getText().setText(rslt.get("annotation"));
				} else {
					this.getText().setText("");
				}
				this.getText().redraw();
			}
		}

	}
}
