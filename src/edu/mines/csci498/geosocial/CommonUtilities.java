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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class CommonUtilities {

    /**
     * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
     */
    public static final String SERVER_URL = "http://138.67.206.173:8080/gcm-demo";

    /**
     * Google API project id registered to use GCM.
     */
    public static final String SENDER_ID ="1041293767065";

    /**
     * Tag used on log messages.
     */
    public static final String TAG = "GCMDemo";

    /**
     * Intent used to display a message in the screen.
     */
    public static final String DISPLAY_MESSAGE_ACTION =
            "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    public static final String EXTRA_MESSAGE = "message";

    public static final String REQUEST_MESSAGE = "request";
    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
    
    static void displayFeedbackToast(Context context, String message) {
    	Toast.makeText(context,message, Toast.LENGTH_LONG).show();
    }
    
    static void displayAlert(Context context, String message) {
    	
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra("type", "Alert");
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    	/*
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);	
    	
    	alertDialogBuilder.setTitle("New Friend Request")
    					  .setMessage(message);
    	
    	AlertDialog alertDialog = alertDialogBuilder.create();
    	
    	alertDialog.show();
    					  */
    }
}