package Drive.WebactionDrive;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import scala.util.Random;
import net.sf.antcontrib.net.httpclient.Credentials;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;


public class DriveCommands {
	private static String refreshToken = "1/KJkOSMT1zu4l5yhEmWthW-Kj3hcJUcAyVcovSI1NjC0";
	private static String accessToken  = "ya29.NAFVNKvSv9pitbbxuh1EJpT9ddrYnzHJl5MWk2MwdQqupFa91-h1017QsmbvqJOL0gyDQZPDbmFIXQ";
	private static String folderId     = "0B6PlMpwtqrfpd0lNZEYyU2p1RWs";

	final static Logger log = Logger.getLogger(DriveCommands.class);
	
	public static class GoogleDriveException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6060911503886814988L;

		public GoogleDriveException(String string) {
			super(string);
		}
		
	}
	
	public static class GoogleDocNotFoundExcpetion extends GoogleDriveException{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;


		public GoogleDocNotFoundExcpetion(String string) {
			super(string);
		}
		
	}

	/**
	 * Creates a Drive service. This service is then used to perform all drive actions
	 * @return
	 * @throws IOException
	 */
	public static Drive createMyDriveService() throws IOException{
		GoogleCredential c = GoogleAuthorization.tokentoCredential(accessToken, refreshToken);
		return  GoogleAuthorization.buildService(c);
	}

	/**
	 * Given The drive service update a file with the given fileID
	 * @param service
	 * @param fileId
	 * @param fileContent
	 * @return
	 */
	public static File updateFile(Drive service, String fileId, java.io.File fileContent) {
		try {
			// First retrieve the file from the API.
			File file = service.files().get(fileId).execute();

			// File's new content
			FileContent mediaContent = new FileContent(file.getMimeType(), fileContent);

			// Send the request to the API.
			log.info("updating File");
			File updatedFile = service.files().update(fileId, file, mediaContent).setConvert(true).execute();

			return updatedFile;
		} catch (IOException e) {
			log.error("Exception when updating file ", e);
			return null;
		}
	}
	
	public static void uploadFileToFolder(Drive myDrive, java.io.File file, String title, String mimeType) throws IOException{
		log.info("upload file");
		File metaData = new File();
		//Try uploading 
		metaData.setMimeType(mimeType);
		metaData.setTitle(title);
		metaData.setParents(Arrays.asList(new ParentReference().setId(folderId)));
		FileContent mediaContent = new FileContent(mimeType,file);
		//try convert to false and see what happens
		myDrive.files().insert(metaData,mediaContent).setConvert(true).execute();
	}
	
	public static boolean fileExists(Drive myDrive, String filename) throws GoogleDriveException, IOException{
		boolean exists = true;
		try{
			getFileIdFromName(myDrive, filename);
		}catch(GoogleDocNotFoundExcpetion e){
			exists = false;
		}
		return exists;
	}

	public static String isAutomated(boolean state){
		if(state){
			return "AUTOMATED";
		} else{
			return "NOT AUTOMATED";
		}
	}

	public static HashMap<String,Boolean> parseTableOfContents(InputStream googleContent) throws IOException{
		BufferedReader googleReader = new BufferedReader( new InputStreamReader(googleContent));
		String line;
		Matcher m;
		HashMap <String,Boolean> results = new HashMap<String,Boolean>();
		while ((line = googleReader.readLine()) !=null){
			if ((m = Pattern.compile("(?i)([0-9])\\s+Test Cases.*").matcher(line)).find()){
				String digit = m.group(1);
				while ((line = googleReader.readLine()) !=null){
					if (line.matches(String.format("[%s]\\.[0-9]\\.[0-9].*",digit))){
						//TO-DO CHANGE THIS TO ALWAYS  PUT A FALSE
						results.put(line, false);
					}
				}
			}
		}

		return results;

	}
	
	/**
	 * Download a file given the google file instance
	 * @param service
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static InputStream downloadFile(Drive service, File file) throws IOException {
		Map map;
		String downloadURL = null;
		if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
			downloadURL = file.getDownloadUrl();
		} else if ( (map = file.getExportLinks())!= null){
			downloadURL = (String) map.get("text/plain");

			// The file doesn't have any content stored on Drive.

		}else{
			return null;
		}

		try {
			HttpResponse resp =
					service.getRequestFactory().buildGetRequest(new GenericUrl(downloadURL))
					.execute();
			return resp.getContent();
		} catch (IOException e) {
			// An error occurred.
			throw e;
		}
	}
	
	/**
	 * Given File Name give File
	 * @param mydrive
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static File getFileFromName(Drive mydrive,String name) throws IOException, Exception{
		return mydrive.files().get(DriveCommands.getFileIdFromName(mydrive,name)).execute();
	}

	/**
	 * Print request
	 * @param request
	 */
	public static void printRequest(com.google.api.services.drive.Drive.Files.List request){
		List<File> result = new ArrayList<File>();
		do {
			try {
				FileList files = request.execute();

				result.addAll(files.getItems());
				request.setPageToken(files.getNextPageToken());
			} catch (IOException e) {
				System.out.println("An error occurred: " + e);
				request.setPageToken(null);
			}
		} while (request.getPageToken() != null &&
				request.getPageToken().length() > 0);
		while(result.isEmpty() == false){
			System.out.println(result.remove(0).get("id"));
		}
	}

	/**
	 * Get the file id from the name
	 * @param mydrive
	 * @param title
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	public static String getFileIdFromName(Drive mydrive, String title) throws GoogleDriveException, GoogleDocNotFoundExcpetion, IOException{
		log.info("Requesting file " + title);
		com.google.api.services.drive.Drive.Files.List request = mydrive.files().list().setQ(String.format("title = \"%s\"",title));
		List<File> result = new ArrayList<File>();
		do {
			try {
				FileList files = request.execute();
				result.addAll(files.getItems());
				request.setPageToken(files.getNextPageToken());
			} catch (IOException e) {
				System.out.println("An error occurred: " + e);
				request.setPageToken(null);
			}
		} while (request.getPageToken() != null &&
				request.getPageToken().length() > 0);

		if (result.size() > 1){
			throw new GoogleDriveException("Found too many files by name " + title);
		}else if (result.size() == 0){
			throw new GoogleDocNotFoundExcpetion("Google Doc Does not exist: "+ title);
		}

		return result.remove(0).getId();

	}
	
	public static HTMLCreate parseTableOfContentsToHTML(InputStream googleContent) throws IOException{
		BufferedReader googleReader = new BufferedReader( new InputStreamReader(googleContent));
		String line;
		Matcher m;
		HTMLCreate html = new HTMLCreate();
		while ((line = googleReader.readLine()) !=null){
			if ((m = Pattern.compile("(?i)([0-9])\\s+Test Cases.*").matcher(line)).find()){
				String digit = m.group(1);
				while ((line = googleReader.readLine()) !=null){
					if ((m =Pattern.compile(String.format("(^%s\\.[0-9])\\s+(.*)",digit)).matcher(line)).find()){
						html.addTestSuite(m.group(1), m.group(2));
					}
					if ((m =Pattern.compile(String.format("(^%s\\.[0-9]\\.[0-9])\\s+(.*)",digit)).matcher(line)).find()){
						html.addTestCase(m.group(1), m.group(2), "not Automated");

					}
				}
			}
		}
		return html;
	}
	
	private static void writetoTestOut() throws Exception{
		String accessToken = "ya29.NAFVNKvSv9pitbbxuh1EJpT9ddrYnzHJl5MWk2MwdQqupFa91-h1017QsmbvqJOL0gyDQZPDbmFIXQ";
		String refreshToken = "1/KJkOSMT1zu4l5yhEmWthW-Kj3hcJUcAyVcovSI1NjC0";
		GoogleCredential c = GoogleAuthorization.tokentoCredential(accessToken, refreshToken);
		Drive mydrive = GoogleAuthorization.buildService(c);
		com.google.api.services.drive.Drive.Files.List request = mydrive.files().list().setFields("items/title");


		//find the test plans folder
		request = mydrive.files().list().setFields("items(id,title)").setQ("mimeType = \'application/vnd.google-apps.folder\' and title = \"Test Plans\"");
		//printRequest(request);



		File googleDoc = mydrive.files().get(getFileIdFromName(mydrive,"Oracle_Logminer_3.1.1_TestPlan")).execute();
		InputStream googleContent = downloadFile(mydrive,googleDoc);



		BufferedReader googleString = new BufferedReader( new InputStreamReader(googleContent));

		PrintWriter pw = new PrintWriter("testout.txt");
		String line;
		while ((line = googleString.readLine()) != null){
			pw.println(line);
			System.out.println(line);
		}
		pw.close();

		//		InputStream is = new FileInputStream("testout.txt");
		//		HashMap <String,Boolean> results = parseTableOfContents(is);
		//		is.close();
		//		is = new FileInputStream("testout.txt");
		//		String filestring = new String(IOUtils.toByteArray(is));
		//		
		//		for (Entry<String,Boolean> e: results.entrySet()){
		//			String lines = e.getKey();
		//			String updatedLine = lines + isAutomated(e.getValue());
		//			filestring = filestring.replaceAll(lines, updatedLine);
		//		}
		//		
		//		System.out.println(filestring);
		//		PrintWriter pw1 = new PrintWriter("testout1.txt");
		//		pw1.write(filestring);
		//		pw1.close();
		//		java.io.File f = new java.io.File("testout1.txt");
		//		
		//		updateFile(mydrive, "1YF0SPEIQz8wTOOA38HtbzjCT3YJYXtjFJXJkexN73pw", f);
	}
}
