package Drive.WebactionDrive;

import java.io.File;
import java.io.IOException;

import Drive.WebactionDrive.WADrive.GoogleDocNotFoundExcpetion;
import Drive.WebactionDrive.WADrive.GoogleDriveException;

public interface OnlineFileSystem {
	
	public static class OnlineFileSystemException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public OnlineFileSystemException(String string){
			super(string);
		}

		public OnlineFileSystemException() {
			super();
			// TODO Auto-generated constructor stub
		}
	}
	public void updateFile(String fileID, File File);
	public void uploadFile(File file, String title, String mimeType, String folderID) throws IOException;
	public boolean fileExists(String fileID ) throws OnlineFileSystemException, IOException;
	public void getFile(String fileID);
	public String getObjectID(String title) throws OnlineFileSystemException, IOException;
	
}
