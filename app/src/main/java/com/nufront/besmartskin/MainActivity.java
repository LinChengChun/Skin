package com.nufront.besmartskin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nufront.skin.SkinFactory;
import com.nufront.skin.SkinManager;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private SkinFactory skinFactory = new SkinFactory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getLayoutInflater().setFactory(skinFactory);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(() -> {
            SkinManager.getInstance().init(MainActivity.this);
            String path = SkinManager.getInstance().getLastResourcesPath();
            if(!TextUtils.isEmpty(path)){
                SkinManager.getInstance().load(path);
                skinFactory.apply();
            }
        }, 10);

    }

    public void onClick(View view) {
        @SuppressLint("SdCardPath")
        boolean result = SkinManager.getInstance().load("/sdcard/BeSmartSkin/skinresource-debug.apk");
        Toast.makeText(this, String.format("你点击了换肤 %s", result), Toast.LENGTH_SHORT).show();

        skinFactory.apply();
    }
}