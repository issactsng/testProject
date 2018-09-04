package com.facetec.zoom.basicapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.facetec.zoom.sdk.*;
import java.util.ArrayList;

public class BasicAppActivity extends Activity {

    private String MY_SECRET_DATA_TO_STORE = "A piece of secret data to be secured by ZoOm, only to be unlocked on successful authentication!";
    private String userID = "AppUser";
    private String encryptionSecretForUserID = "MY_APP_OR_USER_ENCRYPTION_KEY";
    private ContextThemeWrapper ctw = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light);

    private Button enrollButton;
    private Button authButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enrollButton = (Button) findViewById(R.id.enrollButton);
        authButton = (Button) findViewById(R.id.authButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enrollButton.setLetterSpacing(0.05f);
            authButton.setLetterSpacing(0.05f);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //
        // Initialize the SDK
        //
        initializeZoom();
    }

    private void initializeZoom() {
        //
        // Visit https://dev.zoomlogin.com/zoomsdk/#/account to retrieve your app token
        // Replace BuildConfig.ZOOM_APP_TOKEN below with your app token
        //
        String zoomAppToken = "d0HNQjxNAp8GnGSq77DdjEMg0RCmZlNY";

        //
        // Create ZoomCustomization object to modify look and feel
        //
        ZoomCustomization zoomCustomization = new ZoomCustomization();
        ZoomSDK.setCustomization(zoomCustomization);

        ZoomSDK.initialize(
                this,
                zoomAppToken,
                mInitializeCallback
        );

        //
        // signal to the ZoOm SDK that audit trail should be captured
        // note: this is enabled on a per-application basis
        // please contact support@zoomlogin.com to request access
        //
        ZoomSDK.setAuditTrailType(AuditTrailType.HEIGHT_640);

        //
        // preload sdk resources so the UI is snappy (optional)
        //
        ZoomSDK.preload(BasicAppActivity.this);
    }

    private final ZoomSDK.InitializeCallback mInitializeCallback = new ZoomSDK.InitializeCallback() {
        @Override
        public void onCompletion(boolean successful) {
            if (successful) {
                BasicAppActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enrollButton.setEnabled(true);
                        authButton.setEnabled(true);
                    }
                });
            }
            else {
                BasicAppActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showInitFailedMessage();
                    }
                });
            }
        }
    };

    public void showInitFailedMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setTitle("Initialization failed.  ");
        builder.setMessage("Please check that you have set your ZoOm app token to the zoomAppToken variable in this file.  To retrieve your app token, visit https://dev.zoomlogin.com/zoomsdk/#/account.");
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    //
    // delete user if they exist and enroll a new user
    //
    public void onEnrollClick(View v) {
        Intent enrollmentIntent = new Intent(this, ZoomEnrollmentActivity.class);

        //
        // highly recommended but not required
        //
        enrollmentIntent.putExtra(ZoomSDK.EXTRA_ENROLLMENT_USER_ID, userID);

        //
        // required
        //
        enrollmentIntent.putExtra(ZoomSDK.EXTRA_USER_ENCRYPTION_SECRET, encryptionSecretForUserID);

        //
        // The secret data we will store with the user.
        // This data is encrypted and secured by ZoOm.
        // The secret is only return upon successful authentication by the user.
        //
        enrollmentIntent.putExtra(ZoomSDK.EXTRA_ENROLLMENT_SECRET, MY_SECRET_DATA_TO_STORE);

        startActivityForResult(enrollmentIntent, ZoomSDK.REQUEST_CODE_ENROLLMENT);
    }

    //
    // authenticate current user
    //
    public void onAuthClick(View v) {
        if(ZoomSDK.isUserEnrolled(this, userID)) {
            Intent authenticationIntent = new Intent(this, ZoomAuthenticationActivity.class);

            //
            // highly recommended but not required
            //
            authenticationIntent.putExtra(ZoomSDK.EXTRA_AUTHENTICATION_USER_ID, userID);

            //
            // required
            //
            authenticationIntent.putExtra(ZoomSDK.EXTRA_USER_ENCRYPTION_SECRET, encryptionSecretForUserID);

            startActivityForResult(authenticationIntent, ZoomSDK.REQUEST_CODE_AUTHENTICATION);
        }
        else {
            Toast.makeText(this, "User not enrolled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        //
        // make sure the result was returned correctly
        //
        if (resultCode == RESULT_OK) {
            //
            // result from enrollment
            //
            if (requestCode == ZoomSDK.REQUEST_CODE_ENROLLMENT) {
                //
                // parse enrollment results into ZoomEnrollmentResult object
                //
                ZoomEnrollmentResult result = data.getParcelableExtra(ZoomSDK.EXTRA_ENROLL_RESULTS);

                //
                // retrieve the enrollment audit trail image
                // note: this is enabled on a per-application basis
                // please contact support@zoomlogin.com to request access
                //
                if (result.getFaceMetrics() != null) {
                    ArrayList<Bitmap> enrollmentAuditTrail = result.getFaceMetrics().getAuditTrail();
                }
            }

            //
            // result from authentication
            //
            else if (requestCode == ZoomSDK.REQUEST_CODE_AUTHENTICATION) {
                ZoomAuthenticationResult result = data.getParcelableExtra(ZoomSDK.EXTRA_AUTH_RESULTS);

                if (result.getStatus() == ZoomAuthenticationStatus.USER_WAS_AUTHENTICATED) {
                    if (result.getSecret() != null) {
                        //
                        // the user successfully authenticated, now we can make authenticated API requests!
                        //
                        Log.d("Zoom Sample App", "Secret data returned from successful authentication: " + result.getSecret());
                    }
                }
                else {
                    //
                    // handle cases where user is unable to auth, cancellation, etc.
                    //
                }

                //
                // retrieve the enrollment audit trail image
                // note: this is enabled on a per-application basis
                // please contact support@zoomlogin.com to request access
                //

                if (result.getFaceMetrics() != null) {
                    ArrayList<Bitmap> authenticationAuditTrail = result.getFaceMetrics().getAuditTrail();
                }
            }
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //
                // preload sdk resources so the UI is snappy (optional)
                //
                ZoomSDK.preload(BasicAppActivity.this);
            }
        });
    }
}
