# android_gesture_password

自定义样式：
  
    <declare-styleable name="gesture_view_attrs">
        <!-- 设置显示状态-->
        <attr name="state">
            <!-- 设置状态 默认状态 -->
            <enum name="SET" value="100" />
            <!-- 验证状态 -->
            <enum name="VERIFY" value="101" />
        </attr>
        <!-- 验证次数 默认5-->
        <attr name="verify_time" format="dimension" />
        <!-- 限制最小点的位数 默认4 -->
        <attr name="min_point" format="dimension" />
        <!-- 点之间的距离,以及圆点的大小(倍数) 默认30-->
        <attr name="point_dis" format="dimension" />
    </declare-styleable>

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
        
 ![image](https://github.com/hxw-haha/android_gesture_password/raw/master/设置页面-1.png)
 ![image](https://github.com/hxw-haha/android_gesture_password/raw/master/设置页面-2.png)
       
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
                    // 需要跳转到设置手机密码页面
                    // 手机密码未设置、保证在本地密码被清空
                    startActivity(new Intent(VerifyActivity.this, SetActivity.class));
                    finish();
                } else {
                    // 输入错误密码操作次数
                    finish();
                    Toast.makeText(VerifyActivity.this, "验证失败了。。。", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
   ![image](https://github.com/hxw-haha/android_gesture_password/raw/master/验证页面.png)
   
