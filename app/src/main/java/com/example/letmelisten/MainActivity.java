package com.example.letmelisten;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public Iterable<byte[]> streamer = null;
    FileOutputStream fos = null;
    FileOutputStream fos2 = null;
    FileInputStream fis = null;
    int halfSecondBytesNumber = 22050 * 8 / 2;
    String[] permission_list = {
            Manifest.permission.RECORD_AUDIO
    };

    //Noise noise = new Noise();
    String result = "Unknown";
    float[] arr;
    Map<String, Float> floatMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
        MediaRecord recorder = new MediaRecord();
        Thread recorderThread  = new Thread(recorder);
        recorderThread.start();
        //진동객체 생성
        final Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        streamer = recorder;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(halfSecondBytesNumber);

        Noise noise = new Noise();

        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //String mFilePath ="/sdcard/record.wav";
                String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/record.wav";
                String mFilePath2 = Environment.getExternalStorageDirectory().getAbsolutePath() +"/dog.wav";
                String mFilePath3 = Environment.getExternalStorageDirectory().getAbsolutePath() +"/record.pcm";

                try {
                    fos = new FileOutputStream(mFilePath);
                    fos2 = new FileOutputStream(mFilePath3);
                    //fos = openFileOutput(mFilePath,Context.MODE_PRIVATE);
                } catch(FileNotFoundException e) {
                    e.printStackTrace();
                }
                /*try {
                    writeWavHeader(fos, (short)1,22050 , (short)16);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                try {
                    writeWavHeader(fos, (short)1, 22050 , (short)16);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while(true) {
                    try {
                        if(recorder.recordStart==1) {

                            int cnt = 0;
                            for(byte[] bytes : streamer) {
                                //여기서 서버로 전송
                                fos.write(bytes, 0,bytes.length);
                                fos2.write(bytes,0,bytes.length);
                                int total = 0;

                                if(recorder.recordStart==0) {
                                    //File file = new File(mFilePath);
                                    fos.close();
                                    File file = new File(mFilePath);
                                    File file2 = new File(mFilePath2);
                                    updateWavHeader(file);
                                    arr = noise.classifyNoise(mFilePath);
                                    //System.out.println(arr[20]);
                                    modeling(arr);
                                    fos = null;
                                    fos = new FileOutputStream(mFilePath);
                                    try {
                                        writeWavHeader(fos, (short)1, 22050 , (short)16);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    //noise.classifyNoise(mFilePath);
                                    List<Recognition> list = noise.getTopKProbability(floatMap);
                                    String predictionResult = noise.getPredictedValue(list);
                                    System.out.println("최종결과 : "+predictionResult);
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this,result,Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    if(!result.equals("undefined")){
                                        vibrator.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
                                    }

                                    //여기서 푸시알림이랑 진동 기능 하면 될듯
                                    //predictionResult가 분류결과임
                                   break;
                                }

                            }
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                        //Intent intent = new Intent(MainActivity.this, Noise.class);
                        //startActivity(intent);
                        recorder.decibel_threshold = 10;
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"실내모드로 전환합니다.",Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case R.id.button2:
                        recorder.decibel_threshold = 20;
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"실외모드로 전환합니다.",Toast.LENGTH_SHORT).show();
                            }
                        });
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

    public static void writeWavHeader(OutputStream out, short channels, int sampleRate, short bitDepth) throws IOException {
        // WAV 포맷에 필요한 little endian 포맷으로 다중 바이트의 수를 raw byte로 변환한다.
        byte[] littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(sampleRate)
                .putInt(sampleRate * channels * (bitDepth / 8))
                .putShort((short) (channels * (bitDepth / 8)))
                .putShort(bitDepth)
                .array();
        // 최고를 생성하지는 않겠지만, 적어도 쉽게만 가자.
        out.write(new byte[]{
                'R', 'I', 'F', 'F', // Chunk ID
                0, 0, 0, 0, // Chunk Size (나중에 업데이트 될것)
                'W', 'A', 'V', 'E', // Format
                'f', 'm', 't', ' ', //Chunk ID
                16, 0, 0, 0, // Chunk Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // Num of Channels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // Byte Rate
                littleBytes[10], littleBytes[11], // Block Align
                littleBytes[12], littleBytes[13], // Bits Per Sample
                'd', 'a', 't', 'a', // Chunk ID
                0, 0, 0, 0, //Chunk Size (나중에 업데이트 될 것)
        });
    }

    public static void updateWavHeader(File wav) throws IOException {
        byte[] sizes = ByteBuffer
                .allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                // 아마 이 두 개를 계산할 때 좀 더 좋은 방법이 있을거라 생각하지만..
                .putInt((int) (wav.length() - 8)) // ChunkSize
                .putInt((int) (wav.length() - 44)) // Chunk Size
                .array();
        RandomAccessFile accessWave = null;
        try {
            accessWave = new RandomAccessFile(wav, "rw"); // 읽기-쓰기 모드로 인스턴스 생성
            // ChunkSize
            accessWave.seek(4); // 4바이트 지점으로 가서
            accessWave.write(sizes, 0, 4); // 사이즈 채움
            // Chunk Size
            accessWave.seek(40); // 40바이트 지점으로 가서
            accessWave.write(sizes, 4, 4); // 채움
        } catch (IOException ex) {
            // 예외를 다시 던지나, finally 에서 닫을 수 있음
            throw ex;
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close();
                } catch (IOException ex) {
                    // 무시
                }
            }
        }
    }

    public void modeling(float[] mfcc){
        try{
            MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(this,"model4.tflite");
            Interpreter.Options tfliteOptions = new Interpreter.Options();
            tfliteOptions.setNumThreads(2);
            Interpreter tflite = new Interpreter(tfliteModel,tfliteOptions);
            System.out.println(result);
            int imageTensorIndex = 0;
            int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape();
            DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();
            int probabilityTensorIndex = 0;
            int[] probabilityShape = tflite.getOutputTensor(probabilityTensorIndex).shape();
            DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

            TensorBuffer inBuffer = TensorBuffer.createDynamic(imageDataType);
            inBuffer.loadArray(arr,imageShape);
            ByteBuffer inpBuffer = inBuffer.getBuffer();
            TensorBuffer outputTensorBuffer = TensorBuffer.createFixedSize(probabilityShape,probabilityDataType);
            tflite.run(inpBuffer,outputTensorBuffer.getBuffer());
            //결과출력
            final String ASSOCIATED_AXIS_LABELS = "labels.txt";
            List<String> associatedAxisLabels = null;

            try {
                associatedAxisLabels = FileUtil.loadLabels(this, ASSOCIATED_AXIS_LABELS);
            } catch (IOException e) {
                Log.e("tfliteSupport", "Error reading label file", e);
            }

            // Post-processor which dequantize the result
            TensorProcessor probabilityProcessor =
                    new TensorProcessor.Builder().add(new NormalizeOp(0, 255)).build();

            if (null != associatedAxisLabels) {
                // Map of labels and their corresponding probability
                TensorLabel labels = new TensorLabel(associatedAxisLabels,
                        probabilityProcessor.process(outputTensorBuffer));

                // Create a map to access the result based on label
                floatMap = labels.getMapWithFloatValue();
                Iterator<String> iter = floatMap.keySet().iterator();
                float[] resultArr;
                int index = 0;
                float max = 0;
                while(iter.hasNext()){
                    String key = iter.next();
                    Float value = floatMap.get(key);
                    if(max<value){
                        max = value;
                        result = key;
                    }
                    System.out.println(key+":"+value);
                    //System.out.println("분류 결과 : "+result);

                }
                if(max<0.0035){
                    result = "undefined";
                }
                System.out.println("분류 결과 : "+result);
//                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();



            }



        } catch(IOException e) {
            e.printStackTrace();
        }


    }

    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, 44100); // sample rate
            writeInt(output, 22050 * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }

            output.write(fullyReadFileToBytes(rawFile));
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
    byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis= new FileInputStream(f);
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        }  catch (IOException e){
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }
    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }


    

}