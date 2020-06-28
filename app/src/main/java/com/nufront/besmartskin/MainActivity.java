package com.nufront.besmartskin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.nufront.skin.SkinFactory;
import com.nufront.skin.SkinManager;

public class MainActivity extends AppCompatActivity {

    private SkinFactory skinFactory = new SkinFactory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getLayoutInflater().setFactory(skinFactory);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SkinManager.getInstance().init(this);

    }

    public void onClick(View view) {
        @SuppressLint("SdCardPath")
        boolean result = SkinManager.getInstance().load("/sdcard/BeSmartSkin/skinresource-debug.apk");
        Toast.makeText(this, String.format("你点击了换肤 %s", result), Toast.LENGTH_SHORT).show();

        skinFactory.apply();
    }
}