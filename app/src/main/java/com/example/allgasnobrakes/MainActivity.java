package com.example.allgasnobrakes;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;

public class MainActivity extends AppCompatActivity {
    EditText firstName;
    EditText lastName;
    Button registerButton;
    final String TAG = "Sample";
    FirebaseFirestore db;
    private int CAMERA_PERMISSION_CODE = 101;
    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
        Button camera = findViewById(R.id.camera_button);
        camera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setContentView(R.layout.camera);
                CodeScannerView scannerView = findViewById(R.id.scanner_view);
                mCodeScanner = new CodeScanner(MainActivity.this, scannerView);
                mCodeScanner.startPreview();
                mCodeScanner.setScanMode(ScanMode.CONTINUOUS);
                mCodeScanner.setAutoFocusMode(AutoFocusMode.CONTINUOUS);
                TextView t = findViewById(R.id.tv_textView);
                t.setText("");
                mCodeScanner.setDecodeCallback(new DecodeCallback() {
                    @Override
                    public void onDecoded(@NonNull final Result result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                t.setText(result.getText());
                            }
                        });
                    }
                });
            }
        });

    }
}