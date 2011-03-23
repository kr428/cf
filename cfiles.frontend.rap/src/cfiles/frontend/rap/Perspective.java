package cfiles.frontend.rap;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Configures the perspective layout. This class is contributed through the
 * plugin.xml.
 */
public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		IFolderLayout navFolder = layout.createFolder("nav", IPageLayout.LEFT,
				0.25f, editorArea);
		navFolder.addView(FilteredNavigationView.ID);
		navFolder.addView(FulltextListView.ID);

		// layout.addStandaloneView(FilteredNavigationView.ID, false,
		// IPageLayout.LEFT, 0.35f, editorArea);
		IFolderLayout folder = layout.createFolder("messages", IPageLayout.TOP,
				0.25f, editorArea);
		folder.addPlaceholder(HeaderView.ID + ":*");
		folder.addView(HeaderView.ID);
		folder.addView(AnnotationView.ID);
		folder.addView(RevisionListView.ID);

		IFolderLayout folder2 = layout.createFolder("views",
				IPageLayout.BOTTOM, 0.63f, editorArea);
		folder2.addView(BrowserView.ID);
		folder2.addView(AttachmentContentView.ID);
		layout.getViewLayout(NavigationView.ID).setCloseable(false);
	}
}
