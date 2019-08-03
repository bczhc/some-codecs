package com.zhc.tools.toast;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.zhc.codecs.R;

public class AToast extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toast_activity);
        Button btn = findViewById(R.id.toast);
        EditText et = findViewById(R.id.toast_et);
        btn.setOnClickListener(v -> {
            Editable text = et.getText();
            Toast.makeText(this, text.toString(), Toast.LENGTH_SHORT).show();
        });
    }
}