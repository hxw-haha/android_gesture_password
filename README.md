# android_gesture_password
首次提交 

设置页面调用：
    
      <com.example.gesture_password.view.GestureView
        android:id="@+id/gesture_view_set"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        gesture:state="SET" />
        
        GestureView mGestureView = findViewById(R.id.gesture_view_set);
        mGestureView.setOnGestureSetListener(new GestureView.IGestureSetListener() {
            @Override
            public void onSetSucceed(List<Integer> pointPos) {
                Log.e("SetActivity....",pointPos.toString());
                startActivity(new Intent(SetActivity.this, MainActivity.class));
                finish();
            }
        });
       
  验证页面调用：
        
      <com.example.gesture_password.view.GestureView
        android:id="@+id/gesture_view_verify"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="20dp"
        gesture:state="VERIFY" />
        
        GestureView   mGestureView = findViewById(R.id.gesture_view_verify);
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
   
