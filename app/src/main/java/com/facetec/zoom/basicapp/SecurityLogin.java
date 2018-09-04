package com.facetec.zoom.basicapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facetec.zoom.sdk.ZoomAuthenticationResult;
import com.facetec.zoom.sdk.ZoomEnrollmentActivity;
import com.facetec.zoom.sdk.ZoomEnrollmentResult;
import com.facetec.zoom.sdk.ZoomSDK;

public class SecurityLogin extends Activity {

    Button setupBtn;
    final private String userId = "umobileFL2018";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zoomactivity);

        setupBtn = (Button)findViewById(R.id.btn_setup);

    }

    public void initZoom(){
        ZoomSDK.initialize(this, "d0HNQjxNAp8GnGSq77DdjEMg0RCmZlNY", new ZoomSDK.InitializeCallback() {
            @Override
            public void onCompletion(boolean successful) {
                if (!successful) {
                    // handle unsuccessful init
                    Toast.makeText(SecurityLogin.this,"Face Recognition Initialize Failed", Toast.LENGTH_LONG).show();
                }else {
                    Intent enrollmentIntent = new Intent(SecurityLogin.this, ZoomEnrollmentActivity.class);

                    enrollmentIntent.putExtra(ZoomSDK.EXTRA_USER_ENCRYPTION_SECRET, userId);

                    enrollmentIntent.putExtra(ZoomSDK.EXTRA_ENROLLMENT_SECRET, "0189801162");

                    startActivityForResult(enrollmentIntent, ZoomSDK.REQUEST_CODE_ENROLLMENT);
                }
            }
        });
    }

    public void enrollClick(View v) {
        initZoom();
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == ZoomSDK.REQUEST_CODE_ENROLLMENT && data != null) {
            ZoomEnrollmentResult result = data.getParcelableExtra(ZoomSDK.EXTRA_ENROLL_RESULTS);
        }
        else if (requestCode == ZoomSDK.REQUEST_CODE_AUTHENTICATION && data != null) {
            ZoomAuthenticationResult result = data.getParcelableExtra(ZoomSDK.EXTRA_AUTH_RESULTS);
        }
    }
}
