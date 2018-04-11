package com.xh2015.scanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.xh2015.scanner.scan_lib.activity.ScannerCodeActivity;
import com.xh2015.scanner.scan_lib.utils.ScanConfig;
import com.xh2015.scanner.scan_lib.utils.StatusBarUtils;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setNoTitle(this);
        setContentView(R.layout.activity_main);
        StatusBarUtils.setTransparentStatusBar(this);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanConfig config = new ScanConfig();
                config.selectPhoto = true;
                config.voice = true;
                config.vibrate = true;
                ScannerCodeActivity.startActivityForResult(MainActivity.this,config, 1001);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Toast.makeText(this, "扫描结果为:" + data.getStringExtra(ScannerCodeActivity.SCAN_RESULT),
                    Toast.LENGTH_LONG).show();
        }
    }
}
