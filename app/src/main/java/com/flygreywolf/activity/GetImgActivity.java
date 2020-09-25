package com.flygreywolf.activity;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.flygreywolf.bean.Image;
import com.flygreywolf.util.Application;
import com.flygreywolf.util.ImgUtil;

public class GetImgActivity extends AppCompatActivity {

    private ImageView imageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_img);

        Image img = (Image) Application.appMap.get(Application.BIG_IMG_INFO);

        imageView = findViewById(R.id.big_img);

        imageView.setImageBitmap(ImgUtil.Bytes2Bimap(img.getContent()));

    }
}