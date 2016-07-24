package br.com.neuberoliveira.serviceRequest;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
/*import static com.google.android.gms.internal.zzid.runOnUiThread;*/

/**
 * Created by neuber on 13/04/14. -23.6834955 -46.67099403
 */
public abstract class ServiceRequest extends AsyncTask<String, Void, Object> {
	public static final String LOG_ID = "service_request";
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String JSON_RESPONSE_STATE = "json_response_state";
	public static final int MAX_BUFFER_SIZE = 1 * 1024 * 1024;
	public static final int STATUS_OK = 200;
	private static final int READ_TIME_OUT = 120000;
	private static final int CONNECTION_TIME_OUT = 120000;
	private final String crlf = "\r\n";
	protected String fullRequest = "";
	protected Bundle savedInstanceState = new Bundle();
	protected Context context;
	protected Object jsonResponse = null;
	protected HttpURLConnection conn;
	protected Object serverResponse;
	protected String serverResponseRaw = "";
	protected int requestStatus = 0;
	protected String requestMethod = METHOD_GET;
	protected String requestEndpoint = "";
	protected String baseURL = "";
	protected String fullURL = "";
	protected String token = "";
	protected ProgressDialog progressDialog;
	protected ServiceRequest.CustomLoader customLoader;
	protected FragmentManager fragmentManager;
	protected boolean closeOnFinish = true;
	protected boolean silentMode = false;
	protected boolean debugMode = false;
	protected boolean checkForConnection = false;
	protected List<Parameter> params = new ArrayList<>();
	protected List<String> parametersList = new ArrayList<>();
	protected List<String> uploadList = new ArrayList<>();
	protected ServiceRequestCallbackInterface listener;
	protected int progressTitle;
	protected int progressMessage;
	protected int alertTitle;
	protected int alertMessage;
	protected int alertPositiveTitle;
	protected int noConnectionTitle;
	protected int noConnectionMessage;
	private String twoHyphens = "--";
	private String boundary = "NdServiceRequestBoundary" + String.valueOf(System.currentTimeMillis());
	
	protected abstract void defBaseURL();
	protected abstract void defNoConnectionTitle();
	protected abstract void defNoConnectionMessage();
	protected abstract void defProgressTitle();
	protected abstract void defProgressMessage();
	protected abstract void defPositiveButton();
	
	public ServiceRequest(Context ctx) {
		this(ctx, null);
	}
	
	public ServiceRequest(Context ctx, ServiceRequestCallbackInterface callback) {
		this.context = ctx;
		this.listener = callback;
		
		defBaseURL();
		defNoConnectionMessage();
		defNoConnectionTitle();
		defProgressTitle();
		defProgressMessage();
		defPositiveButton();
	}

	public static Object parseResponse(String response) {
		Object json = null;
		try {
			json = new JSONTokener(response).nextValue();

		} catch (JSONException e) {
			e.getMessage();
			//e.printStackTrace();
		}

		return json;
	}

	//Connection Check
	public static boolean hasConnection(Context context) {
		ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMan.getActiveNetworkInfo();

		return networkInfo != null && networkInfo.isConnectedOrConnecting();
	}

	public String getBaseURL() {
		return baseURL;
	}

	protected void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public String getFullURL() {
		return fullURL;
	}
	
	public int getNoConnectionMessage() {
		return noConnectionMessage;
	}

	protected void setNoConnectionMessage(int noConnectionMessage) {
		this.noConnectionMessage = noConnectionMessage;
	}

	public int getNoConnectionTitle() {
		return noConnectionTitle;
	}

	protected void setNoConnectionTitle(int noConnectionTitle) {
		this.noConnectionTitle = noConnectionTitle;
	}
	
	public int getPositiveButton(){
		return alertPositiveTitle;
	}
	
	public void setPositiveButton(int alertPositiveTitle){
		this.alertPositiveTitle = alertPositiveTitle;
	}
	
	public int getProgressMessage() {
		return progressMessage;
	}

	protected void setProgressMessage(int progressMessage) {
		this.progressMessage = progressMessage;
	}

	public int getProgressTitle() {
		return progressTitle;
	}

	protected void setProgressTitle(int progressTitle) {
		this.progressTitle = progressTitle;
	}

	public boolean isGet() {
		return getRequestMethod().equals(METHOD_GET);
	}

	public boolean isPost() {
		return getRequestMethod().equals(METHOD_POST);
	}
	
	public String getRawServerResponse() {
		return serverResponseRaw;
	}
	
	public void setRawServerResponse(String response) {
		serverResponseRaw = response;
	}
	
	public Object getServerResponse() {
		return serverResponse;
	}

	protected void setServerResponse(Object serverResponse) {
		this.serverResponse = serverResponse;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public int getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(int requestStatus) {
		this.requestStatus = requestStatus;
	}

	public String getRequestEndpoint() {
		return requestEndpoint;
	}

	protected boolean isInSilentMode() {
		return silentMode;
	}

	protected boolean isDebugMode() {
		return debugMode;
	}

	protected boolean isCheckForConnection() {
		return checkForConnection;
	}
	
	
	
	//Configure request
	@SuppressWarnings("unused")
	public ServiceRequest makeGet() {
		requestMethod = METHOD_GET;
		return this;
	}

	@SuppressWarnings("unused")
	public ServiceRequest makePost() {
		requestMethod = METHOD_POST;
		return this;
	}

	@SuppressWarnings("unused")
	public ServiceRequest endpoint(String endpoint) {
		requestEndpoint = endpoint;
		return this;
	}

	@SuppressWarnings("unused")
	public ServiceRequest then(ServiceRequestCallbackInterface srci){
		listener = srci;
		return this;
	}

	@SuppressWarnings("unused")
	public ServiceRequest silentMode(boolean isSilent) {
		silentMode = isSilent;
		return this;
	}

	@SuppressWarnings("unused")
	public ServiceRequest checkForConnection(boolean check) {
		checkForConnection = check;
		return this;
	}

	@SuppressWarnings("unused")
	public ServiceRequest debugMode(boolean debugMode) {
		this.debugMode = debugMode;
		return this;
	}

	@SuppressWarnings("unused")
	public void closeOnFinish(boolean closeOnFinish) {
		this.closeOnFinish = closeOnFinish;
	}

	//Make Request
	public Object request() {
		InputStream inputStream = null;
		URL url;

		//boolean isGet = isGet( method );
		boolean isPost = isPost();

		buildParamsList();

		try {
			fullURL = getBaseURL() + getRequestEndpoint() + buildQueryString();
			url = new URL(getFullURL());
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setReadTimeout(READ_TIME_OUT);
			conn.setConnectTimeout(CONNECTION_TIME_OUT);
			conn.setDoInput(true);
			conn.setRequestMethod(getRequestMethod());
			//conn.setChunkedStreamingMode( 512 );

			if (isPost) {
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

				buildPostString();
				writeBoundarieEnd();
			}
			
			
			if (isPost && conn.getOutputStream() != null) {
				conn.getOutputStream().flush();
				conn.getOutputStream().close();
			}
			
			conn.connect();
			inputStream = conn.getInputStream();

		} catch (OutOfMemoryError oomExeption) {
			handleOOMException(oomExeption);

		} catch (FileNotFoundException notFoundException) {
			handle404Exception(notFoundException);

		} catch (EOFException eofException) {
			handleEOFException(eofException);

		} catch (ConnectException connectException) {
			handleConnectionException(connectException);

		} catch (MalformedURLException mfuException) {
			handleMFURLException(mfuException);

		} catch (ProtocolException protocolException) {
			handleProtoclException(protocolException);

		} catch (IOException ioException) {
			handleIOException(ioException);
		}

		try {
			if (inputStream != null) {
				setRawServerResponse(getStringResponse(inputStream));
				inputStream.close();
			} else if(conn.getErrorStream()!=null) {
				setRawServerResponse(getStringResponse(conn.getErrorStream()));
			}else{
				setRawServerResponse("No response stream");
			}

			setServerResponse(parseResponse(getRawServerResponse()));
			setRequestStatus(conn.getResponseCode());
		} catch (IOException e) {
			if (isDebugMode())
				Log.d(LOG_ID, "Ops! Something went wrong :/");
		}

		if (isDebugMode()) {
			Log.d(LOG_ID, "Request to: [" + getRequestMethod() + "] " + getFullURL());
			Log.d(LOG_ID, "The response code is: " + getRequestStatus());
			Log.d(LOG_ID, "The response raw: " + getServerResponse());
			Log.d(LOG_ID, "Parameters: " + TextUtils.join("&", parametersList));
			//Log.d(LOG_ID, "Uploads: " + TextUtils.join("&", uploadList));
			Log.d(LOG_ID, "Full Request: \n" + fullRequest);
		}

		return jsonResponse;
	}

	//Manage Instance State
	@SuppressWarnings("unused")
	public void restoreInstanceState(Bundle state) {
		if (state != null) {
			setServerResponse(state.getString(JSON_RESPONSE_STATE, ""));
		}
	}

	@SuppressWarnings("unused")
	public void restoreInstanceState(String response) {
		if (response != null) {
			setServerResponse(response);
		}
	}
	
	//Build request parameters
	protected List<String> buildParamsList() {
		for(Parameter param : params){
			if(param.isMultiValue()){
				for (Object value : param.getValues()) {
					addToParamList(param.getName()+"[]", value);
				}
			}else{
				addToParamList(param.getName(), param.getValue());
			}
			
			
		}

		return parametersList;
	}
	
	private void addToParamList(String key, Object value){
		try{
			String param = key+"="+URLEncoder.encode(value+"", "utf-8");
			parametersList.add(param);
			
			if (isDebugMode())
				Log.d(LOG_ID, "PARAM: " + param);
		} catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
	}

	protected String buildQueryString() {
		boolean hasToken = false;
		String queryString = "";
		String parametersProperty = TextUtils.join("&", parametersList);

		if ((token != null && !token.isEmpty())) {
			queryString += "?token=" + token;
			hasToken = true;
		}

		if (isGet()) {
			queryString += (hasToken ? "&" : "?") + parametersProperty;
		}

		return queryString;
	}

	protected void buildPostString() throws IOException {
		writeParamText();
		//writeParamFile();
	}

	protected void writeBoundarie() throws IOException {
		String bound = twoHyphens + boundary + crlf;
		fullRequest += bound;
		conn.getOutputStream().write(bound.getBytes());
	}

	protected void writeBoundarieEnd() throws IOException {
		String bound = twoHyphens + boundary + twoHyphens;
		fullRequest += bound;
		conn.getOutputStream().write(bound.getBytes());
	}

	protected void writeParamText() throws IOException {
		for(Parameter param : params){
			if(param.isMultiValue()){
				for( Object value : param.getValues()){
					writePostParam(param.getName(), value, true);
				}
			}else{
				writePostParam(param.getName(), param.getValue(), false);
			}
		}
	}

	/*protected void writeParamFile() throws IOException {
		Iterator itSingle = uploadSingle.entrySet().iterator();
		Iterator itMulti = uploadMulti.entrySet().iterator();

		while (itSingle.hasNext()) {
			Map.Entry pair = (Map.Entry) itSingle.next();
			String key = (String) pair.getKey();

			FileUploadHolder upload = (FileUploadHolder) pair.getValue();
			writeFile(key, upload);

			itSingle.remove(); // avoids a ConcurrentModificationException
		}


		while (itMulti.hasNext()) {
			Map.Entry pair = (Map.Entry) itMulti.next();
			String key = (String) pair.getKey();
			List<FileUploadHolder> uploads = (List<FileUploadHolder>) pair.getValue();

			for (FileUploadHolder upload : uploads) {
				writeFile(key, upload);
			}

			itMulti.remove(); // avoids a ConcurrentModificationException
		}
	}*/

	protected void writePostParam(String key, Object value, boolean isArray) throws IOException {
		writeBoundarie();
		String param = "Content-Disposition: form-data; name=\"" + key + (isArray ? "[]" : "") + "\"" + crlf + crlf + value + crlf;
		fullRequest += param;
		conn.getOutputStream().write(param.getBytes("UTF-8"));
		//outputStream.writeBytes(param);

		conn.getOutputStream().flush();
	}

	protected void writeFile(String key, FileUploadHolder upload) throws IOException {
		InputStream fileInputStream = null;
		String path = upload.getFilename();
		//File file = new File(path);
		boolean compress;
		Bitmap bitmap = BitmapFactory.decodeFile(path);

		String param = "Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + path + "\"" + crlf + "Content-type: image/jpeg" + crlf + crlf;

		writeBoundarie();
		fullRequest += param;
		conn.getOutputStream().write(param.getBytes());

		try {
			/*fileInputStream = new BufferedInputStream(new FileInputStream(file));
			// create a buffer of maximum size
			int bytesAvailable = fileInputStream.available();

			int maxBufferSize = 2048;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];
			byte byt[] = new byte[bufferSize];

			int bytesRead;
			while ((bytesRead = fileInputStream.read(buffer)) > 0) {
				conn.getOutputStream().write(buffer, 0, bytesRead);
			}*/

			compress = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, conn.getOutputStream());
			fullRequest += compress + crlf;

			if (compress) {
				bitmap.recycle();
			}

			conn.getOutputStream().write(crlf.getBytes());
			conn.getOutputStream().flush();
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
	}

	//Set Request Paramenters
	@SuppressWarnings("unused")
	public ServiceRequest addParam(Parameter param){
		params.add(param);
		return this;
	}

	/*@SuppressWarnings("unused")
	public ServiceRequest addUpload(String key, FileUploadHolder upload) {
		uploadSingle.put(key, upload);
		return this;
	}

	@SuppressWarnings("unused")
	public ServiceRequest addUpload(String key, List<FileUploadHolder> uploads) {
		uploadMulti.put(key, uploads);
		return this;
	}*/

	protected String getStringResponse(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line;

		while ((line = br.readLine()) != null) {
			sb.append(line).append("\n");
		}
		br.close();

		return sb.toString();
	}

	public Context getContext() {
		return context;
	}

	@SuppressWarnings("unused")
	public void setToken(String token) {
		this.token = token;
	}

	//Loaders
	@SuppressWarnings("unused")
	public ProgressDialog getLoader() {
		return progressDialog;
	}
	
	@SuppressWarnings("unused")
	public void setCustomLoader(CustomLoader customLoader, FragmentManager manager) {
		this.fragmentManager = manager;
		this.customLoader = customLoader;
	}
	
	@SuppressWarnings("unused")
	public void displayNoConnectionAlert() {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle(getNoConnectionTitle());
		alert.setMessage(getNoConnectionMessage());
		alert.setPositiveButton(getPositiveButton(), null);

		alert.show();
	}
	
	protected void handleOOMException(OutOfMemoryError oomExeption) {
		if (isDebugMode())
			oomExeption.printStackTrace();
	}

	private void handle404Exception(FileNotFoundException notFoundException) {
		if (isDebugMode())
			notFoundException.printStackTrace();
	}

	protected void handleEOFException(EOFException eofException) {
		if (isDebugMode())
			eofException.printStackTrace();
	}

	protected void handleConnectionException(ConnectException connectException) {
		if (isDebugMode())
			connectException.printStackTrace();
	}

	protected void handleIOException(IOException ioException) {
		if (isDebugMode())
			ioException.printStackTrace();
	}

	protected void handleMFURLException(MalformedURLException mfuException) {
		if (isDebugMode())
			mfuException.printStackTrace();
	}

	protected void handleProtoclException(ProtocolException protocolException) {
		if (isDebugMode())
			protocolException.printStackTrace();
	}

	protected void callCallback() {
		if (listener != null) {
			
			if(isSuccess())
				listener.onSuccess(this);
			else
				listener.onFail(this);
		}
	}


	//Async task operations
	@Override
	protected void onPreExecute() {
		int pt = getProgressTitle();
		String progressTitle = context.getResources().getString(pt);
		String progressMessage = context.getResources().getString(getProgressMessage());

		if (!isInSilentMode()) {
			if (customLoader == null) {
				progressDialog = ProgressDialog.show(context, progressTitle, progressMessage, true, false);
			} else {
				customLoader.displayLoader(fragmentManager);
			}
		}
	}

	@Override
	protected Object doInBackground(String... params) {
		//try {
		if (isCheckForConnection()) {
			if (hasConnection(context)) {
				request();
			} else {
				/*runOnUiThread(new Runnable() {
					public void run() {
						displayNoConnectionAlert();
					}
				});*/
			}
		} else {
			request();
		}
		/*} catch (IOException ioException) {
			handleIOException(ioException);
			
			if( isDebugMode() ){
				Log.e( LOG_ID, "Could no connect to the server: [" + getRequestMethod() + "] " + baseURL + getRequestEndpoint() );
				Log.e( LOG_ID, "IO Exception: " + ioException.getMessage() );
			}
		}*/

		return getServerResponse();
	}

	protected void onPostExecute(Object json) {
		if (!isInSilentMode()) {
			if (closeOnFinish) {
				if (customLoader == null) {
					progressDialog.dismiss();
				} else {
					customLoader.dismissLoader();
				}
			}
		}
		
		callCallback();
	}

	public boolean isSuccess() {
		return getRequestStatus() >=200 && getRequestStatus() <= 299 ;
	}

	public boolean isError() {
		return !isSuccess();
	}


	public interface CustomLoader {
		void displayLoader(FragmentManager manager);

		void dismissLoader();

		@SuppressWarnings("unused")
		String getLoaderTag();
	}
}
