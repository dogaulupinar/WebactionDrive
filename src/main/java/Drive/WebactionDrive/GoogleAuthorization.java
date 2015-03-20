package Drive.WebactionDrive;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

// ...

class GoogleAuthorization {

	// Path to client_secrets.json which should contain a JSON document such as:
	//   {
	//     "web": {
	//       "client_id": "[[YOUR_CLIENT_ID]]",
	//       "client_secret": "[[YOUR_CLIENT_SECRET]]",
	//       "auth_uri": "https://accounts.google.com/o/oauth2/auth",
	//       "token_uri": "https://accounts.google.com/o/oauth2/token"
	//     }
	//   }
	final static Logger log = Logger.getLogger(GoogleAuthorization.class);

	private static final String CLIENTSECRETS_LOCATION = "client_secrets.json";

	static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	private static final List<String> SCOPES = Arrays.asList(
			"https://www.googleapis.com/auth/drive",
			"https://www.googleapis.com/auth/userinfo.email",
			"https://www.googleapis.com/auth/userinfo.profile");

	private static GoogleAuthorizationCodeFlow flow = null;

	/**
	 * Exception thrown when an error occurred while retrieving credentials.
	 */
	public static class GetCredentialsException extends Exception {

		protected String authorizationUrl;

		/**
		 * Construct a GetCredentialsException.
		 *
		 * @param authorizationUrl The authorization URL to redirect the user to.
		 */
		public GetCredentialsException(String authorizationUrl) {
			this.authorizationUrl = authorizationUrl;
		}

		/**
		 * Set the authorization URL.
		 */
		public void setAuthorizationUrl(String authorizationUrl) {
			this.authorizationUrl = authorizationUrl;
		}

		/**
		 * @return the authorizationUrl
		 */
		public String getAuthorizationUrl() {
			return authorizationUrl;
		}
	}

	/**
	 * Exception thrown when a code exchange has failed.
	 */
	public static class CodeExchangeException extends GetCredentialsException {

		/**
		 * Construct a CodeExchangeException.
		 *
		 * @param authorizationUrl The authorization URL to redirect the user to.
		 */
		public CodeExchangeException(String authorizationUrl) {
			super(authorizationUrl);
		}

	}

	/**
	 * Exception thrown when no refresh token has been found.
	 */
	public static class NoRefreshTokenException extends GetCredentialsException {

		/**
		 * Construct a NoRefreshTokenException.
		 *
		 * @param authorizationUrl The authorization URL to redirect the user to.
		 */
		public NoRefreshTokenException(String authorizationUrl) {
			super(authorizationUrl);
		}

	}

	/**
	 * Exception thrown when no user ID could be retrieved.
	 */
	private static class NoUserIdException extends Exception {
	}

	/**
	 * Retrieved stored credentials for the provided user ID.
	 *
	 * @param userId User's ID.
	 * @return Stored Credential if found, {@code null} otherwise.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	static Credential getStoredCredentials(String userId) throws IOException, ClassNotFoundException {
		// TODO: Implement this method to work with your database. Instantiate a new
		// Credential instance with stored accessToken and refreshToken.
		HashMap<String,Credential> h = new HashMap<String,Credential>();
		FileInputStream fis = new FileInputStream("userdatabase.ser");
		ObjectInputStream ois = new ObjectInputStream(fis);
		h = (HashMap) ois.readObject();
		ois.close();
		fis.close();
		return h.get(userId);
	}

	/**
	 * Store OAuth 2.0 credentials in the application's database.
	 *
	 * @param userId User's ID.
	 * @param credentials The OAuth 2.0 credentials to store.
	 * @throws IOException 
	 */
	static void storeCredentials(String userId, Credential credentials) throws IOException {
		// TODO: Implement this method to work with your database.
		// Store the credentials.getAccessToken() and credentials.getRefreshToken()
		// string values in your database.
		HashMap<String,Credential> h = new HashMap<String,Credential>();
		h.put(userId,credentials);
		System.out.println(credentials.getAccessToken());
		System.out.println(credentials.getRefreshToken());

		//		FileOutputStream fos = new FileOutputStream("userdatabase.ser");
		//		ObjectOutputStream oos = new ObjectOutputStream(fos);
		//		oos.writeObject(h);
		//		oos.close();
		//		fos.close();
	}

	/**
	 * Build an authorization flow and store it as a static class attribute.
	 *
	 * @return GoogleAuthorizationCodeFlow instance.
	 * @throws IOException Unable to load client_secrets.json.
	 */
	static GoogleAuthorizationCodeFlow getFlow() throws IOException {
		if (flow == null) {
			log.info("getting authorization flow");
			HttpTransport httpTransport = new NetHttpTransport();
			JacksonFactory jsonFactory = new JacksonFactory();
			InputStream is = GoogleAuthorization.class.getResourceAsStream(CLIENTSECRETS_LOCATION);
			Reader reader = new InputStreamReader(is);
			GoogleClientSecrets clientSecrets =
					GoogleClientSecrets.load(jsonFactory, reader);
			flow =
					new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, SCOPES)
			.setAccessType("offline").setApprovalPrompt("force").build();
		}
		return flow;
	}

	/**
	 * Exchange an authorization code for OAuth 2.0 credentials.
	 *
	 * @param authorizationCode Authorization code to exchange for OAuth 2.0
	 *        credentials.
	 * @return OAuth 2.0 credentials.
	 * @throws CodeExchangeException An error occurred.
	 */
	static Credential exchangeCode(String authorizationCode)
			throws CodeExchangeException {
		try {
			GoogleAuthorizationCodeFlow flow = getFlow();
			GoogleTokenResponse response =
					flow.newTokenRequest(authorizationCode).setRedirectUri(REDIRECT_URI).execute();
			return flow.createAndStoreCredential(response, null);
		} catch (IOException e) {
			System.err.println("An error occurred: " + e);
			throw new CodeExchangeException(null);
		}
	}

	/**
	 * Send a request to the Userinfoplus API to retrieve the user's information.
	 *
	 * @param credentials OAuth 2.0 credentials to authorize the request.
	 * @return User's information.
	 * @throws NoUserIdException An error occurred.
	 */
	static Userinfoplus getUserInfo(Credential credentials)
			throws NoUserIdException {
		Oauth2 userInfoService =
				new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credentials).build();
		Userinfoplus Userinfoplus = null;
		try {
			Userinfoplus = userInfoService.userinfo().get().execute();
		} catch (IOException e) {
			System.err.println("An error occurred: " + e);
		}
		if (Userinfoplus != null && Userinfoplus.getId() != null) {
			return Userinfoplus;
		} else {
			throw new NoUserIdException();
		}
	}

	/**
	 * Retrieve the authorization URL.
	 *
	 * @param emailAddress User's e-mail address.
	 * @param state State for the authorization URL.
	 * @return Authorization URL to redirect the user to.
	 * @throws IOException Unable to load client_secrets.json.
	 */
	public static String getAuthorizationUrl(String emailAddress, String state) throws IOException {
		GoogleAuthorizationCodeRequestUrl urlBuilder =
				getFlow().newAuthorizationUrl().setRedirectUri(REDIRECT_URI).setState(state);
		urlBuilder.set("user_id", emailAddress);
		return urlBuilder.build();
	}

	public static GoogleCredential tokentoCredential(String accessToken, String refreshToken ) throws IOException{
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		InputStream is = GoogleAuthorization.class.getResourceAsStream(CLIENTSECRETS_LOCATION);
		Reader reader = new InputStreamReader(is);
		GoogleClientSecrets clientSecrets =
				GoogleClientSecrets.load(jsonFactory, reader);
		GoogleCredential credential1 = new GoogleCredential.Builder().setJsonFactory(jsonFactory)
				.setTransport(httpTransport).setClientSecrets(clientSecrets).build();
		credential1.setAccessToken(accessToken);
		credential1.setRefreshToken(refreshToken);
		log.info("Creating credential from accessToken and refreshToken");
		return credential1;

	}

	/**
	 * Retrieve credentials using the provided authorization code.
	 *
	 * This function exchanges the authorization code for an access token and
	 * queries the Userinfoplus API to retrieve the user's e-mail address. If a
	 * refresh token has been retrieved along with an access token, it is stored
	 * in the application database using the user's e-mail address as key. If no
	 * refresh token has been retrieved, the function checks in the application
	 * database for one and returns it if found or throws a NoRefreshTokenException
	 * with the authorization URL to redirect the user to.
	 *
	 * @param authorizationCode Authorization code to use to retrieve an access
	 *        token.
	 * @param state State to set to the authorization URL in case of error.
	 * @return OAuth 2.0 credentials instance containing an access and refresh
	 *         token.
	 * @throws NoRefreshTokenException No refresh token could be retrieved from
	 *         the available sources.
	 * @throws IOException Unable to load client_secrets.json.
	 * @throws ClassNotFoundException 
	 */
	public static Credential getCredentials(String authorizationCode, String state)
			throws CodeExchangeException, NoRefreshTokenException, IOException, ClassNotFoundException {
		String emailAddress = "";
		try {
			Credential credentials = exchangeCode(authorizationCode);
			Userinfoplus userinfo = getUserInfo(credentials);
			String userId = userinfo.getId();
			emailAddress = userinfo.getEmail();
			if (credentials.getRefreshToken() != null) {
				storeCredentials(userId, credentials);
				return credentials;
			} else {
				credentials = getStoredCredentials(userId);
				if (credentials != null && credentials.getRefreshToken() != null) {
					return credentials;
				}
			}
		} catch (CodeExchangeException e) {
			e.printStackTrace();
			// Drive apps should try to retrieve the user and credentials for the current
			// session.
			// If none is available, redirect the user to the authorization URL.
			e.setAuthorizationUrl(getAuthorizationUrl(emailAddress, state));
			throw e;
		} catch (NoUserIdException e) {
			e.printStackTrace();
		}
		// No refresh token has been retrieved.
		String authorizationUrl = getAuthorizationUrl(emailAddress, state);
		throw new NoRefreshTokenException(authorizationUrl);
	}

	static Drive buildService(GoogleCredential credentials){
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		log.info("Building new Drive Service");
		return new Drive.Builder(httpTransport, jsonFactory, credentials).setApplicationName("Test Plan Mapping").build();
	}

	/**
	 * This Should almost never be used. it creates a new authorization code to create new 
	 * refresh tokens and accessTokens
	 * @throws IOException
	 * @throws GoogleAuthorization.NoRefreshTokenException
	 * @throws ClassNotFoundException
	 */
	public void newAuthCode() throws IOException, GoogleAuthorization.NoRefreshTokenException, ClassNotFoundException{
		String url = GoogleAuthorization.getAuthorizationUrl("doga@webaction.com", "helpasfad");
		System.out.println("Please open the following URL in your browser then type the authorization code:");
		System.out.println("  " + url);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String authcode = br.readLine();
		String state = "no idea what this is for";
		try{
			Credential c = GoogleAuthorization.getCredentials(authcode, state);
			System.out.println("Keep these refreshTokens and AuthTokens they will be used to make api calls");
			System.out.println("Refresh Token " + c.getRefreshToken());
			System.out.println("Access Token " + c.getAccessToken());
		}catch(GoogleAuthorization.CodeExchangeException e){
			System.out.println(e.getAuthorizationUrl());
		}
	}

}