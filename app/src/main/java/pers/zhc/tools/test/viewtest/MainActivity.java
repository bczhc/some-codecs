package pers.zhc.tools.test.viewtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import pers.zhc.tools.R;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InputStream inputStream = getResources().openRawResource(R.raw.a);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        AView aView = new AView(this, bitmap);
        setContentView(aView);
    }
}