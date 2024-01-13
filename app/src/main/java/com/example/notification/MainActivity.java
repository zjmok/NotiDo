package com.example.notification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends Activity {

    private SharedPreferences sp;

    private final Context mContext = this;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setFinishOnTouchOutside(false);

        sp = getSharedPreferences("text", Context.MODE_PRIVATE);

        // 为了极致减少安装包大小，不用 lambda
        findViewById(R.id.iv_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle("提示")
                        .setMessage("本 APP 极度小巧（安装包不到 20 KB，安装后存储占用不到 200 KB）。\n仅有的功能是展示一条无法直接移除的通知，可用作备忘。\n后台运行几乎不耗电。\n建议（特别针对国内魔改定制 ROM）：\n1. 在多任务页面锁定任务防止被清理；\n2. 打开自启动和电池策略无限制等；\n3. 打开通知相关权限。如过滤规则设置为重要，锁屏通知权限等；")
                        .setPositiveButton("确定", null)
                        .show();
            }
        });

        resetText();

        EditText etTitle = findViewById(R.id.et_title);
        EditText etMessage = findViewById(R.id.et_message);

        findViewById(R.id.tv_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle("提示")
                        .setMessage("是否移除通知？\n移除的同时将会关闭应用程序。")
                        .setNeutralButton("清空内容并移除", (dialog, which) -> {
                            etTitle.setText("");
                            etMessage.setText("");

                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("title", "");
                            editor.putString("message", "");
                            editor.apply();

                            Intent intent = new Intent(mContext, ForegroundService.class);
                            MainActivity.this.stopService(intent);

                            MainActivity.this.finish();
                        })
                        .setNegativeButton("取消", null)
                        .setPositiveButton("移除", (dialog, which) -> {
                            Intent intent = new Intent(mContext, ForegroundService.class);
                            MainActivity.this.stopService(intent);

                            MainActivity.this.finish();
                        })
                        .show();
            }
        });

        findViewById(R.id.tv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Android 33 请求打开通知权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    NotificationManager notificationManager = MainActivity.this.getSystemService(NotificationManager.class);
                    boolean enable = notificationManager.areNotificationsEnabled();
                    if (!enable) {
                        MainActivity.this.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 9527);
                        return;
                    }
                }

                String title = etTitle.getText().toString();
                String message = etMessage.getText().toString();

                SharedPreferences.Editor editor = sp.edit();
                editor.putString("title", title);
                editor.putString("message", message);
                editor.apply();

                Intent intent = new Intent(mContext, ForegroundService.class);
                if (Objects.equals(title.trim(), "") && Objects.equals(message.trim(), "")) {
                    Toast.makeText(mContext, "至少保留一项内容", Toast.LENGTH_SHORT).show();
                } else {
                    intent.putExtra("title", Objects.equals(title.trim(), "") ? "-" : title);
                    intent.putExtra("message", Objects.equals(message.trim(), "") ? "-" : message);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        MainActivity.this.startForegroundService(intent);
                    } else {
                        MainActivity.this.startService(intent);
                    }

                    MainActivity.this.finish();
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resetText();
    }

    /**
     * @noinspection NullableProblems
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 9527) {
            if (Objects.equals(permissions[0], Manifest.permission.POST_NOTIFICATIONS)
                    && grantResults != null && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "已打开通知权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resetText() {
        findViewById(R.id.ll_root).requestFocus();

        String title = sp.getString("title", "");
        String message = sp.getString("message", "");

        EditText etTitle = findViewById(R.id.et_title);
        EditText etMessage = findViewById(R.id.et_message);

        etTitle.setText(title);
        etMessage.setText(message);
    }

    @Override
    public void onBackPressed() {
        onMultiClick(new Function() {
            @Override
            public void continueAction(int size) {
                Toast.makeText(mContext, "快速再按 " + size + " 次 退出程序", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void doneAction() {
                finish();
            }
        });
    }

    private static final int SIZE = 2;
    private static final long INTERVAL = 500;
    private long[] mHints = new long[SIZE];

    private void onMultiClick(Function listener) {
        System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1); // 每次点击时，数组向前移动一位
        mHints[mHints.length - 1] = SystemClock.uptimeMillis(); // 为数组最后一位赋值
        long time = INTERVAL * SIZE;
        if (SystemClock.uptimeMillis() - mHints[0] <= time) { // 连续点击之间有效间隔
            mHints = new long[SIZE];
            listener.doneAction();
        } else {
            for (int i = 0; i < SIZE; i++) {
                if (SystemClock.uptimeMillis() - mHints[i] <= time) {
                    listener.continueAction(i);
                    break;
                }
            }
        }
    }

    interface Function {
        void continueAction(int size);

        void doneAction();
    }

}