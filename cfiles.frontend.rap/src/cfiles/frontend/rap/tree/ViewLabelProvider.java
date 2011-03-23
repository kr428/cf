package cfiles.frontend.rap.tree;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ViewLabelProvider extends LabelProvider {

	public String getText(Object obj) {
		if (obj instanceof TreeObject) {
			String name = ((TreeObject) obj).getName();

			if (name.equals("/")) {
				return "/";
			}

			if (name.contains("/")) {
				String[] fields = name.split("/");
				return fields[fields.length - 1];
			}

			return name;
		}

		return obj.toString();
	}

	public Image getImage(Object obj) {
		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		if (obj instanceof TreeParent)
			imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
}
