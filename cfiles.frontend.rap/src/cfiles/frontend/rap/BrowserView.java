package cfiles.frontend.rap;

import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import cfiles.frontend.rap.core.SessionNexus;
import cfiles.frontend.rap.couchlink.CouchTreeLink.Revision;
import cfiles.frontend.rap.tree.TreeObject;


public class BrowserView extends ViewPart implements ISelectionListener {

	public static final String ID = "cfiles.frontend.rap.BrowserView";
	private Browser browser = null;

	public BrowserView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		this.getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);
		this.browser = new Browser(parent, SWT.NONE);

		this.browser
				.setUrl("http://" + SessionNexus.getInstance().getHostBase()
						+ "/browserservlet/");
		this.browser.setVisible(true);
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

				final StringBuilder browserUrl = new StringBuilder();
				browserUrl.append("http://").append(
						SessionNexus.getInstance().getHostBase());
				browserUrl.append("/browserservlet/").append(t.getName())
						.append("?src=").append(t.getIdentifier());
				this.setBrowserUrl(browserUrl.toString());
			}
		} else {
			if (selection instanceof StructuredSelection) {
				final StructuredSelection s = (StructuredSelection) selection;
				final Object o = s.getFirstElement();

				if ((null != o) && (o instanceof Revision)) {

					final Revision m = (Revision) o;
					final Map<String, String> e = SessionNexus.getInstance()
							.getCurrentEntity();

					final String fileName = m.getFile().replace(
							"." + m.getRevision(), "");

					final String[] fileNameFields = fileName.split("/");
					final String[] fileNameFullFields = m.getFile().split("/");

					final StringBuilder browserUrl = new StringBuilder();
					browserUrl.append("http://").append(
							SessionNexus.getInstance().getHostBase());
					browserUrl
							.append("/browserservlet/")
							.append(fileNameFields[fileNameFields.length - 1])
							.append("?src=")
							.append(SessionNexus.getInstance()
									.getItemInSelection());

					if (!(m.getRevision().equals(e.get("_rev")))) {
						browserUrl
								.append("&rev=")
								.append(m.getRevision())
								.append("&revfile=")
								.append(fileNameFullFields[fileNameFullFields.length - 1]);
					}

					this.setBrowserUrl(browserUrl.toString());

				}
			}
		}
	}

	private void setBrowserUrl(String url) {
		System.out.println("set browser to " + url);
		if (SessionNexus.getInstance().getFullTextTerm().isEmpty()) {
			this.browser.setUrl(url + "#pagemode=thumbs&statusbar=0&toolbar=0");
		} else {
			this.browser.setUrl(url
					+ "#pagemode=thumbs&statusbar=0&toolbar=0&search="
					+ SessionNexus.getInstance().getFullTextTerm());
		}
		this.browser.redraw();
	}
}
