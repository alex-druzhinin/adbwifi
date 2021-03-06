package com.adruzh.adbwifi;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView mStatus;
    private TextView mConnectString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStatus = (TextView) findViewById(R.id.status_text_view);
        mConnectString = (TextView) findViewById(R.id.ip_text_view);

        String IPADDRESS_PATTERN =
                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

        final Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Shell.SU.available()) {
                    Log.d(TAG, "SU not available");
                    mStatus.setText(R.string.su_not_available);
                    Snackbar.make(view, getResources().getText(R.string.su_not_available), Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                Shell.SU.run(new String[]{
                        "setprop service.adb.tcp.port 5555"
                });
                sleep(1);

                Shell.SU.run(new String[]{
                        "stop adbd"
                });
                sleep(1);

                Shell.SU.run(new String[]{
                        "start adbd"
                });
                sleep(1);

                List<String> suResult = Shell.SU.run(new String[]{
                        "id",
                        "ip -f inet addr show"
                });

                mStatus.setText(R.string.wifi_enabled);

                StringBuilder connectStringBuilder = new StringBuilder();
                if (suResult != null) {
                    boolean ipFound = false;
                    for (String line : suResult) {
                        Matcher matcher = pattern.matcher(line);
                        Log.d(TAG, "result: " + line);
                        if (matcher.find()) {
                            String text = matcher.group();
                            if (text.equalsIgnoreCase("127.0.0.1")) {
                                continue;
                            }
                            ipFound = true;
                            connectStringBuilder.append(text).append('\n');
                        }
                    }

                    if (!ipFound) {
                        mConnectString.setText(suResult.toString());
                    }
                    else {
                        mConnectString.setText(connectStringBuilder.toString());
                    }
                }



            }
        });
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
