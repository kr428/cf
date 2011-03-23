package cfiles.frontend.rap.servlets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cfiles.frontend.rap.couchlink.CouchTreeLink;

public class BrowserServlet extends HttpServlet {

	private CouchTreeLink ctl = new CouchTreeLink();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			final String[] uri = req.getRequestURI().split("/");
			final String fileName = uri[uri.length - 1];

			final String tid = req.getParameter("src");
			if (null == tid || tid.isEmpty()) {
				resp.getWriter().write("----> //cfiles// docbrowser loaded");
			} else {
				final Map<String, String> vals = this.ctl.getEntity(tid);

				String fname = vals.get("stored");

				final String rev = req.getParameter("rev");

				if ((null != rev) && (!(rev.isEmpty()))) {

					final String revfile = req.getParameter("revfile");
					System.out.println("rev: " + rev + " -- revfile: "
							+ revfile);
					fname = (new File(fname).getParent()) + File.separator
							+ revfile;
				}
				System.out.println("about to open: " + fname);
				final File file = new File(fname);

				boolean cTypeSet = false;
				resp.setContentType("application/octet-stream");
				if (vals.containsKey("meta.tika.Content-Type")) {
					resp.setContentType(vals.get("meta.tika.Content-Type"));
					cTypeSet = true;
				}

				if (!cTypeSet) {
					if (file.getName().endsWith("pdf")) {
						resp.setContentType("application/pdf");
					}
				}
				// resp.setHeader("Content-Disposition", "attachment, filename="
				// + fileName);
				RandomAccessFile raf = new RandomAccessFile(file, "r");
				resp.setContentLength((int) raf.length());
				final OutputStream out = resp.getOutputStream();

				byte[] loader = new byte[(int) raf.length()];
				while ((raf.read(loader)) > 0) {
					out.write(loader);
				}

				raf.close();
				out.close();
			}
		} catch (Exception ex) {
			resp.getWriter().write("----> //cfiles// docbrowser loaded");
		}
	}
}
