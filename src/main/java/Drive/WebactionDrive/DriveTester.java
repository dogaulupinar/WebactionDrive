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
		WADrive myDrive = new WADrive();
		java.io.File f = new java.io.File("testout.txt");
		myDrive.uploadFileToFolder( f,"MY First Upload", "text/html");
		
		
//		File googleDoc = WADrive.getFileFromName(mydrive, "Oracle_Logminer_3.1.1_TestPlan");
//		InputStream googleContent = WADrive.downloadFile(mydrive,googleDoc);
//		boolean exists = WADrive.fileExists(mydrive, "DOESNT EXIST");
//		HTMLCreate html = WADrive.parseTableOfContentsToHTML(googleContent);

//		FileUtils.writeStringToFile(f, html.toString());
		//DriveCommands.updateFile(mydrive, "1YF0SPEIQz8wTOOA38HtbzjCT3YJYXtjFJXJkexN73pw", f);
		
		
	
	}

}
