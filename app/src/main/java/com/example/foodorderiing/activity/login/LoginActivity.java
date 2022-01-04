package com.example.foodorderiing.activity.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorderiing.R;
import com.example.foodorderiing.activity.home.HomeActivity;
import com.example.foodorderiing.database.DatabaseHelper;
import com.example.foodorderiing.database.dao.UserDao;
import com.example.foodorderiing.design.BlureImage;
import com.example.foodorderiing.model.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;


public class LoginActivity extends AppCompatActivity {
    private ImageView img_back;
    private CheckBox checkBox;
    private TextView tv_login , tv_NewAcount;
    private EditText et_name,et_password;
    private DatabaseHelper db;
    private UserDao dao;
    private SharedPreferences shpr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        initID();
        initDataBase();
        blurBg();
        createNewAcount();
        hideKeyBoad();
        initLogin();
        shpr = getSharedPreferences("shpr" , MODE_PRIVATE);
        setCheckBox();
    }


    private void initDataBase(){
        db = DatabaseHelper.getInstance(this);
        dao = db.userDao();
    }


    private void blurBg(){
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.food_honey);
        Bitmap bit_kebab = BlureImage.blur18(getApplicationContext(),bm,18f);
        img_back.setImageBitmap(bit_kebab);
    }


    private void initID(){
        img_back = findViewById(R.id.img_background);
        et_password = findViewById(R.id.et_pass);
        et_name = findViewById(R.id.et_name);
        tv_login = findViewById(R.id.tv_login);
        tv_NewAcount = findViewById(R.id.tv_newAcount);
        checkBox = findViewById(R.id.checkBox_loging);
    }



    private void setCheckBox(){
        checkBox.setOnClickListener(v -> {
            String name = et_name.getText().toString();
            String phone = et_password.getText().toString();
            SharedPreferences.Editor editor = shpr.edit();
            editor.putString("name" , name);
            editor.putString("phone" , phone);
            editor.apply();
            Toast.makeText(getApplicationContext(), "save", Toast.LENGTH_SHORT).show();
        });
        if(shpr.contains("name") && shpr.contains("phone")){
            et_name.setText(shpr.getString("name",null));
            et_password.setText(shpr.getString("phone",null));
        }
    }


    private void hideKeyBoad(){
        et_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken() , 0);
                }
            }
        });

        et_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken() , 0);
                }
            }
        });
    }


    private void createNewAcount(){
        tv_NewAcount.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(R.layout.bottomsheet_sign_up);

            EditText name = (EditText)bottomSheetDialog.findViewById(R.id.et_name_signup);
            EditText pass = (EditText)bottomSheetDialog.findViewById(R.id.et_pass_signup);
            Button btn_signup = (Button)bottomSheetDialog.findViewById(R.id.sign_up);
            btn_signup.setOnClickListener(v1 -> {
                String getName = name.getText().toString();
                String getPass = pass.getText().toString();

                if(TextUtils.isEmpty(getName) || TextUtils.isEmpty(getPass)){
                    Toast.makeText(getApplicationContext(), "لطفا فیلدها را پر کنید", Toast.LENGTH_SHORT).show();
                }else {
                    dao.insertUser(new User(getName,getPass));
                    Toast.makeText(getApplicationContext(), "با موفقیت ثبت شد", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                }

            });
            bottomSheetDialog.show();

        });
    }


    private void initLogin(){
        tv_login.setOnClickListener(v -> {
            String name = et_name.getText().toString();
            String pass = et_password.getText().toString();
            if(TextUtils.isEmpty(name) || TextUtils.isEmpty(pass)){
                Toast.makeText(getApplicationContext(), "لطفا فیلدها را پر کنید", Toast.LENGTH_SHORT).show();

            }else if(dao.getUser(name , pass) == null ){
                Toast.makeText(getApplicationContext(), "این کاربر وجود ندارد", Toast.LENGTH_SHORT).show();

            }else {
                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in , android.R.anim.fade_out);
                finish();
            }
        });
    }

}









