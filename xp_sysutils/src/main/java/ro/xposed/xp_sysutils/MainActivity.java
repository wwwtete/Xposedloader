package ro.xposed.xp_sysutils;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

/**
 * 系统工具
 */
public class MainActivity extends AppCompatActivity {

    public static final String SPNAME = "sys_config";
    public static final String KEY_DEBUG = "key_debugable";
    public static final String KEY_BACKUP = "key_backup";

    private static SharedPreferences mSp;

    private SwitchCompat mScDebug,mScbackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ro.xposed.xp_sysutils.R.layout.activity_main);
        mSp = getSharedPreferences(SPNAME,MODE_WORLD_READABLE);

        mScbackup = findViewById(R.id.switch_backup);
        mScDebug = findViewById(R.id.switch_debug);
        mScDebug.setChecked(mSp.getBoolean(KEY_DEBUG,false));
        mScbackup.setChecked(mSp.getBoolean(KEY_BACKUP,false));

        mScbackup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSp.edit()
                        .putBoolean(KEY_BACKUP,isChecked)
                        .apply();
            }
        });

        mScDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSp.edit()
                        .putBoolean(KEY_DEBUG,isChecked)
                        .apply();
            }
        });
    }
}
