package Drive.WebactionDrive;

import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class DriveTester {

	final static Logger log = Logger.getLogger(DriveTester.class);
	
	public static void main(String[] args) throws Exception{
		Drive mydrive = DriveCommands.createMyDriveService();
		java.io.File f = new java.io.File("testout.txt");
		DriveCommands.uploadFileToFolder(mydrive, f,"MY First Upload", "text/html");
		
		
		File googleDoc = DriveCommands.getFileFromName(mydrive, "Oracle_Logminer_3.1.1_TestPlan");
		InputStream googleContent = DriveCommands.downloadFile(mydrive,googleDoc);
		boolean exists = DriveCommands.fileExists(mydrive, "DOESNT EXIST");
		HTMLCreate html = DriveCommands.parseTableOfContentsToHTML(googleContent);

		FileUtils.writeStringToFile(f, html.toString());
		//DriveCommands.updateFile(mydrive, "1YF0SPEIQz8wTOOA38HtbzjCT3YJYXtjFJXJkexN73pw", f);
		
		
	
	}

}
