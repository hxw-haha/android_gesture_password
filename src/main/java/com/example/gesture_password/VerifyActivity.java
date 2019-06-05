package com.example.gesture_password;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.gesture_password.view.GestureView;

import java.util.List;

public class VerifyActivity extends Activity {

    private GestureView mGestureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        mGestureView = findViewById(R.id.gesture_view_verify);
        mGestureView.setOnGestureVerifyListener(new GestureView.IGestureVerifyListener() {
            @Override
            public void onVerifySucceed(List<Integer> pointPos) {
                Log.e("VerifyActivity....", pointPos.toString());
                Toast.makeText(VerifyActivity.this, "验证成功了。。。", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onVerifyFailed(boolean isSetState) {
                if (isSetState) {
                    startActivity(new Intent(VerifyActivity.this, SetActivity.class));
                    finish();
                } else {
                    finish();
                    Toast.makeText(VerifyActivity.this, "验证失败了。。。", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
