package cfiles.frontend.webdav;

import java.io.File;
import java.io.InputStream;
import java.security.Principal;

import net.sf.webdav.ITransaction;
import net.sf.webdav.LocalFileSystemStore;
import net.sf.webdav.StoredObject;
import net.sf.webdav.exceptions.WebdavException;

public class WebDAVDatatStore extends LocalFileSystemStore {

	public WebDAVDatatStore(File folder) {
		super(folder);
	}

	@Override
	public ITransaction begin(Principal principal) throws WebdavException {
		// TODO Auto-generated method stub
		return super.begin(principal);
	}

	@Override
	public void checkAuthentication(ITransaction transaction)
			throws SecurityException {
		// TODO Auto-generated method stub
		super.checkAuthentication(transaction);
	}

	@Override
	public void commit(ITransaction transaction) throws WebdavException {
		// TODO Auto-generated method stub
		super.commit(transaction);
	}

	@Override
	public void rollback(ITransaction transaction) throws WebdavException {
		// TODO Auto-generated method stub
		super.rollback(transaction);
	}

	@Override
	public void createFolder(ITransaction transaction, String uri)
			throws WebdavException {
		// TODO Auto-generated method stub
		super.createFolder(transaction, uri);
	}

	@Override
	public void createResource(ITransaction transaction, String uri)
			throws WebdavException {
		// TODO Auto-generated method stub
		super.createResource(transaction, uri);
	}

	@Override
	public long setResourceContent(ITransaction transaction, String uri,
			InputStream is, String contentType, String characterEncoding)
			throws WebdavException {
		// TODO Auto-generated method stub
		return super.setResourceContent(transaction, uri, is, contentType,
				characterEncoding);
	}

	@Override
	public String[] getChildrenNames(ITransaction transaction, String uri)
			throws WebdavException {
		// TODO Auto-generated method stub
		return super.getChildrenNames(transaction, uri);
	}

	@Override
	public void removeObject(ITransaction transaction, String uri)
			throws WebdavException {
		// TODO Auto-generated method stub
		super.removeObject(transaction, uri);
	}

	@Override
	public InputStream getResourceContent(ITransaction transaction, String uri)
			throws WebdavException {
		// TODO Auto-generated method stub
		return super.getResourceContent(transaction, uri);
	}

	@Override
	public long getResourceLength(ITransaction transaction, String uri)
			throws WebdavException {
		// TODO Auto-generated method stub
		return super.getResourceLength(transaction, uri);
	}

	@Override
	public StoredObject getStoredObject(ITransaction transaction, String uri) {
		// TODO Auto-generated method stub
		return super.getStoredObject(transaction, uri);
	}
	
}
