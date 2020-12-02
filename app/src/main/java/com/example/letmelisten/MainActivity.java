package com.example.letmelisten;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public Iterable<byte[]> streamer = null;
    int halfSecondBytesNumber = 22050 * 8 / 2;
    String[] permission_list = {
            Manifest.permission.RECORD_AUDIO
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
        MediaRecord recorder = new MediaRecord();
        Thread recorderThread  = new Thread(recorder);
        recorderThread.start();

        streamer = recorder;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(halfSecondBytesNumber);

        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (byte[] bytes : streamer) {
                        System.out.println(bytes[10]);
                        /*
                        if (bytes == null) {
                            break;
                        }
                        try {
                            buffer.write(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(buffer.size() >= halfSecondBytesNumber) {
                            byte[] toBeSent = buffer.toByteArray();
                            int startIndex = 0;
                            while(startIndex < toBeSent.length) {
                                int endIndex = Math.min(toBeSent.length, startIndex + 1024*1024);

                                ByteString data = ByteString.copyFrom(toBeSent, startIndex, endIndex);

                                // data 사용

                                startIndex = endIndex;
                            }
                            buffer.reset();
                        }
                        */
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            });

        sendThread.start();




        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        ImageButton button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4); //푸시알림 테스트
        Button button5 = findViewById(R.id.button5); //사운드설정창 연결
        Button button6 = findViewById(R.id.button6); //푸시알림 기록 로그

        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button1:
                        break;
                    case R.id.button2:
                        break;
                    case R.id.button3:
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        };
        button1.setOnClickListener(onClickListener);
        button2.setOnClickListener(onClickListener);
        button3.setOnClickListener(onClickListener);
        button4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                    /**
                     * 누가버전 이하 노티처리
                     */
                    Toast.makeText(getApplicationContext(), "누가버전이하", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher);
                    Bitmap bitmap = bitmapDrawable.getBitmap();

                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext()).
                            setLargeIcon(bitmap)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setWhen(System.currentTimeMillis()).
                                    setShowWhen(true).
                                    setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle("노티테스트!!")
                            .setDefaults(Notification.DEFAULT_VIBRATE)
                            .setFullScreenIntent(pendingIntent, true)
                            .setContentIntent(pendingIntent);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Toast.makeText(getApplicationContext(), "오레오이상", Toast.LENGTH_SHORT).show();
                    /**
                     * 오레오 이상 노티처리
                     */
//                    BitmapDrawable bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.mipmap.ic_launcher);
//                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    /**
                     * 오레오 버전부터 노티를 처리하려면 채널이 존재해야합니다.
                     */

                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    String Noti_Channel_ID = "Noti";
                    String Noti_Channel_Group_ID = "Noti_Group";

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);

//                    notificationManager.deleteNotificationChannel("testid"); 채널삭제

                    /**
                     * 채널이 있는지 체크해서 없을경우 만들고 있으면 채널을 재사용합니다.
                     * 나중에 위로 올리기! 채널은 한번만 생성하면 됨 https://choi3950.tistory.com/9
                     */
                    if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                        Toast.makeText(getApplicationContext(), "채널이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "채널이 없어서 만듭니다.", Toast.LENGTH_SHORT).show();
                        notificationManager.createNotificationChannel(notificationChannel);
                    }

                    notificationManager.createNotificationChannel(notificationChannel);
//                    Log.e("로그확인","===="+notificationManager.getNotificationChannel("testid1"));
//                    notificationManager.getNotificationChannel("testid");


                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID)
                            .setLargeIcon(null).setSmallIcon(R.mipmap.ic_launcher)
                            .setWhen(System.currentTimeMillis()).setShowWhen(true).
                                    setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle("노티테스트!!");
//                            .setContentIntent(pendingIntent);

//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());


                }
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, "Noti");
                startActivity(intent);
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){

            }
        });
    }

    public void checkPermission() {
        //현재 안드로이드 버전이 6.0미만이면 메서드를 종료
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        for (String permission : permission_list) {
            //권한 허용 여부를 확인
            int chk = checkCallingOrSelfPermission(permission);

            if (chk == PackageManager.PERMISSION_DENIED) {
                //권한 허용 여부를 확인하는 창을 띄움
                requestPermissions(permission_list, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            for (int i = 0; i < grantResults.length; i++) {
                //허용됐다면
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(getApplicationContext(), "앱 권한을 설정하세요", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }
}