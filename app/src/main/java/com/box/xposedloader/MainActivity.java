package com.box.xposedloader;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static com.box.xposedloader.StrConstants.SP_NAME;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {



    public static SharedPreferences SP;

    TextView mTvTarget;
    TextView mTvCode;
    EditText mEdClass;
    EditText mEdMethod;
    XposedLoader mLoader;
    MainHook mMainHook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SP = getSharedPreferences(SP_NAME,1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvCode = findViewById(R.id.tv_code);
        mTvTarget = findViewById(R.id.tv_target);
        mEdClass = findViewById(R.id.ev_class);
        mEdClass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                MainActivity.SP.edit()
                        .putString(StrConstants.KEY_HOOK_CLASS,mEdClass.getText().toString()).apply();
            }
        });
        mEdMethod = findViewById(R.id.ev_method);
        mEdMethod.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                MainActivity.SP.edit()
                        .putString(StrConstants.KEY_HOOK_MEHTOD,mEdMethod.getText().toString()).apply();
            }
        });

        findViewById(R.id.btn_update).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.tv_code).setOnClickListener(this);
        findViewById(R.id.tv_target).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_update:
            case R.id.tv_target:
                showSelectDialog(0);
                break;
            case R.id.btn_clear:
                clear();
                break;
            case R.id.tv_code:
                showSelectDialog(1);
                break;
        }
    }

    private void showSelectDialog(int type) {
        SelectPackNameDialog.newInstance(type)
                .show(getSupportFragmentManager(),"select_apk_dialog");
    }

    private void clear() {
        SP.edit()
                .putString(StrConstants.KEY_TARGET_APK,"")
                .putString(StrConstants.KEY_XPOSED_APK,"")
                .putString(StrConstants.KEY_HOOK_CLASS,"")
                .putString(StrConstants.KEY_HOOK_MEHTOD,"")
                .apply();
        update();

    }

    @Override
    protected void onResume() {
        super.onResume();

        update();
    }

    public void update() {
        mTvTarget.setText(SP.getString(StrConstants.KEY_TARGET_APK,""));
        mTvCode.setText(SP.getString(StrConstants.KEY_XPOSED_APK,""));
        mEdClass.setText(SP.getString(StrConstants.KEY_HOOK_CLASS,""));
        mEdMethod.setText(SP.getString(StrConstants.KEY_HOOK_MEHTOD,""));
    }
}
