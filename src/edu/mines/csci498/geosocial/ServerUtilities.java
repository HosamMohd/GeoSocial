/*NOTE: Usage of Pre-existing code provided by Google Inc. Demonstrating the usage of GCM from mobile device 
 * The code has been modified for the use for the GeoSocial App  
 */


/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mines.csci498.geosocial;

import static edu.mines.csci498.geosocial.CommonUtilities.SERVER_URL;
import static edu.mines.csci498.geosocial.CommonUtilities.TAG;
import static edu.mines.csci498.geosocial.CommonUtilities.displayMessage;

import com.google.android.gcm.GCMRegistrar;

import edu.mines.csci498.geosocial.R;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    /**
     * Register this account/device pair within the server.
     *
     * @return whether the registration succeeded or not.
     */
    public static boolean register(final Context context, final String regId, final String name, final String number) {
        Log.i(TAG, "registering device (regId = " + regId + ")");
        String serverUrl = SERVER_URL + "/register";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("name", name);
        params.put("number", number);
        
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {

                 post(serverUrl, params);
                
                GCMRegistrar.setRegisteredOnServer(context, true);
                
                CommonUtilities.displayMessage(context, context.getString(R.string.register_success));
                return true;
            } catch (IOException e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Log.e(TAG, "Failed to register on attempt " + i, e);
                int returnCode = Integer.parseInt(e.getMessage());
                
                if( returnCode >= 500 && returnCode < 600 ){ //Recoverable Error(5xx) RETRY 
	                if (i == MAX_ATTEMPTS) {
	                    break;
	                }
	                try {
	                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
	                    Thread.sleep(backoff);
	                } catch (InterruptedException e1) {
	                    // Activity finished before we complete - exit.
	                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
	                    Thread.currentThread().interrupt();
	                    return false;
	                }
	                // increase backoff exponentially
	                backoff *= 2;
                } else {
                	String message = "Registration: UNRECOVERABLE ERROR";
                	if(e.getMessage().equals("99")){
                		message = context.getString(R.string.register_fail_Id);
                	}else if(e.getMessage().equals("77")) {
                		message = context.getString(R.string.register_fail_Number);
                	}
                   CommonUtilities.displayMessage(context, message);
                   return false;
                }
             }
        }
        String message = context.getString(R.string.server_register_error,
                MAX_ATTEMPTS);
        CommonUtilities.displayMessage(context, message);
        return false;
    }

    /**
     * Unregister this account/device pair within the server.
     */
    static void unregister(final Context context, final String regId) {
        Log.i(TAG, "unregistering device (regId = " + regId + ")");
        String serverUrl = SERVER_URL + "/unregister";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        try {
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
            String message = context.getString(R.string.server_unregistered);
            CommonUtilities.displayMessage(context, message);
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            String message = context.getString(R.string.server_unregister_error,
                    e.getMessage());
            CommonUtilities.displayMessage(context, message);
        }
    }
    
    static void sendFriendRequest(final Context context, final String regId, final String friendNumber) {
       
        String serverUrl = SERVER_URL + "/friendReq";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("friend", friendNumber);
        params.put("respond", "Request"); //null indicates that a request is being made (Not fullfilled) 

        try {
            post(serverUrl, params);

            String message = context.getString(R.string.friend_req_sent);

            CommonUtilities.displayMessage(context, message);
        } catch (IOException e) {
        	String message = context.getString(R.string.server_unregister_error,
                    e.getMessage());
        	if(e.getMessage().equalsIgnoreCase("33")) {
        		
        		message = context.getString(R.string.friend_req_same);
        		
        	} else if (e.getMessage().equalsIgnoreCase("404")) {
        		
        		message = context.getString(R.string.friend_req_not_found);
        	} else if (e.getMessage().equalsIgnoreCase("50"))  {
        		
        		message = context.getString(R.string.friend_req_already);
        	}else if (e.getMessage().equalsIgnoreCase("3"))	{
        		message = context.getString(R.string.friend_req_sending_error);
        	}
        	//CommonUtilities.displayFeedbackToast(context, message);
           CommonUtilities.displayMessage(context, message);
        }
    }
    
    static void sendConfirmRequest(final Context context, final String regId ) {
        
        String serverUrl = SERVER_URL + "/confirmReq";
        Map<String, String> params = new HashMap<String, String>();
        
        //Sending data to same servlet requires the same parameteres ad sendFriendRequest
        params.put("regId", regId);
        
        try {
            post(serverUrl, params);
        } catch (IOException e) {
        	
        	String message = context.getString(R.string.server_unregister_error,
                    e.getMessage());
        	if(e.getMessage() == null) {
        		message = "NULL POINTER RETURNED"; 
        	}else if (e.getMessage().equalsIgnoreCase("3"))	{
        		message = context.getString(R.string.friend_req_sending_error);
        	}

           CommonUtilities.displayMessage(context, message);
        	
        }
    }
    
    static void sendStatusUpdate(final Context context, final String regId, final String status, final String lat, final String lon) {
        
        String serverUrl = SERVER_URL + "/updateStatus";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("status", status);
        params.put("latitude",lat);
        params.put("longitude", lon);

        try {
            post(serverUrl, params);
            CommonUtilities.displayMessage(context, context.getString(R.string.status_update_successful));
        } catch (IOException e) {
        	
        	CommonUtilities.displayMessage(context, context.getString(R.string.status_update_fail));
   
        }
    }
    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    private static void post(String endpoint, Map<String, String> params)
            throws IOException {
        URL url; 
        int status;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            status = conn.getResponseCode();
            if (status != 200) {
              throw new IOException(Integer.toString(status));
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
      }
    	
}