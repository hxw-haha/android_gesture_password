package com.example.gesture_password;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.gesture_password.view.GestureView;

import java.util.List;

public class SetActivity extends Activity {


    private GestureView mGestureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        mGestureView = findViewById(R.id.gesture_view_set);
        mGestureView.setOnGestureSetListener(new GestureView.IGestureSetListener() {
            @Override
            public void onSetSucceed(List<Integer> pointPos) {
                Log.e("SetActivity....",pointPos.toString());
                startActivity(new Intent(SetActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
