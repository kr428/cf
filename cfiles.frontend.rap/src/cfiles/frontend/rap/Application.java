package cfiles.frontend.rap;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.rwt.RWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * This class controls all aspects of the application's execution and is
 * contributed through the plugin.xml.
 */
public class Application implements IApplication {

	public Object start(IApplicationContext context) throws Exception {
		final String session = RWT.getRequest().getParameter("s");
		System.out.println("user: " + RWT.getRequest().getParameter("user"));
		System.out.println("session: " + session);
		Display display = PlatformUI.createDisplay();

		if (null != session) {
			WorkbenchAdvisor advisor = new ApplicationWorkbenchAdvisor();
			return PlatformUI.createAndRunWorkbench(display, advisor);
		} else {
			WorkbenchAdvisor advisor = new ApplicationWorkbenchAdvisor();
			return PlatformUI.createAndRunWorkbench(display, advisor);
		}
	}

	public void stop() {
		// Do nothing
	}
}
