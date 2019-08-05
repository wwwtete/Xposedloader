package com.box.xposedloader;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import static com.box.xposedloader.StrConstants.SP_NAME;

/**
 * XposedLoader使用步骤:
 *
 * 1.选择 Hook Package
 * 2.选择 Xposed Plugin Package
 * 3.新建Xposed插件项目
 * 4.拷贝MainHook.java模板代码到项目根目录，
 *   按照官方文档进行设置: https://github.com/rovo89/XposedBridge/wiki/Development-tutorial
 *
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {



    public static SharedPreferences SP;

    TextView mTvTarget;
    TextView mTvCode;
    EditText mEdClass;
    EditText mEdMethod;
    SwitchCompat mScDebug;
    XposedLoader mLoader;
    MainHook mMainHook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SP = getSharedPreferences(SP_NAME,MODE_WORLD_READABLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvCode = findViewById(R.id.tv_code);
        mTvTarget = findViewById(R.id.tv_target);
        mEdClass = findViewById(R.id.ev_class);
        mScDebug = findViewById(R.id.switch_debug);
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
        mScDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.SP.edit()
                        .putBoolean(StrConstants.KEY_DEBUG_ISOPEN,isChecked)
                        .apply();
            }
        });
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
                .putString(StrConstants.KEY_TARGET_APK,null)
                .putString(StrConstants.KEY_XPOSED_APK,null)
                .putString(StrConstants.KEY_HOOK_CLASS,null)
                .putString(StrConstants.KEY_HOOK_MEHTOD,null)
                .putBoolean(StrConstants.KEY_DEBUG_ISOPEN,false)
                .apply();
        update();

    }

    @Override
    protected void onResume() {
        super.onResume();

        update();
    }

    public void update() {
        mTvTarget.setText(SP.getString(StrConstants.KEY_TARGET_APK,StrConstants.SELECTHOOKTARGET));
        mTvCode.setText(SP.getString(StrConstants.KEY_XPOSED_APK,StrConstants.SELECTXPOSEDPLUGIN));
        mEdClass.setText(SP.getString(StrConstants.KEY_HOOK_CLASS,StrConstants.DEFAULT_CLASS_NAME));
        //onHandleLoadPackage(ClassLoader loader)
        mEdMethod.setText(SP.getString(StrConstants.KEY_HOOK_MEHTOD,StrConstants.DEFAULT_METHOD_NAME));
        mScDebug.setChecked(SP.getBoolean(StrConstants.KEY_DEBUG_ISOPEN,false));
    }
}
