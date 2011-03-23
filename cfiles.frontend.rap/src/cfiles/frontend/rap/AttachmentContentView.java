package cfiles.frontend.rap;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import cfiles.frontend.rap.core.SessionNexus;
import cfiles.frontend.rap.tree.TreeObject;


public class AttachmentContentView extends ViewPart implements
		ISelectionListener {

	public static final String ID = "cfiles.frontend.rap.attachmentContentView";
	private Text text = null;

	public AttachmentContentView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		this.getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);
		this.text = new Text(parent, SWT.WRAP);
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
				this.text.setText(SessionNexus.getInstance().getCouchLink()
						.getAttachmentContent(t.getIdentifier()));
				this.text.redraw();
			}
		}
	}

}
