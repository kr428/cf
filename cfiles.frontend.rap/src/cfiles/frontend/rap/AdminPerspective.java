package cfiles.frontend.rap;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import cfiles.frontend.rap.admin.DBChangesView;
import cfiles.frontend.rap.admin.FtxStatsView;
import cfiles.frontend.rap.admin.ImporterStatsView;
import cfiles.frontend.rap.admin.ServerStatsView;


public class AdminPerspective implements IPerspectiveFactory {

	public static final String ID = "cfiles.frontend.rap.adminperspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		layout.addView(DBChangesView.ID, IPageLayout.LEFT, 0.5f, editorArea);
		layout.addView(ImporterStatsView.ID, IPageLayout.TOP, 0.5f, editorArea);
		layout.addView(FtxStatsView.ID, IPageLayout.BOTTOM, 0.5f, editorArea);
		layout.addView(ServerStatsView.ID, IPageLayout.RIGHT, 0.5f, editorArea);
	}

}
