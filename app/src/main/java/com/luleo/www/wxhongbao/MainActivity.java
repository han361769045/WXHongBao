package com.luleo.www.wxhongbao;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.KeyDown;
import org.androidannotations.annotations.KeyLongPress;
import org.androidannotations.annotations.KeyUp;
import org.androidannotations.annotations.ViewById;

/**
 * Created by leo on 2016/2/4.
 */
@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewById
    Button btnStartService;

    @Click
    void btnStartService() {
        try {
            //打开系统设置中辅助功能
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "找到抢红包，然后开启服务即可", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KeyDown
    void enterPressed() {
        //...
    }

    @KeyUp(KeyEvent.KEYCODE_ESCAPE)
    boolean handleEscapeActionUpEvent() {
        //...
        return false;
    }

    @KeyLongPress({ KeyEvent.KEYCODE_F, KeyEvent.KEYCODE_G })
    void fOrGKeyLongPress(KeyEvent keyEvent) {
        //...
    }
}
