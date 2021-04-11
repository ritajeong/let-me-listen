package com.example.letmelisten;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;


public class MediaRecord implements Runnable, Iterable<byte[]> {
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int SAMPLE_RATE = 22050;

    private final int HALF_SECONDS_SAMPLES = SAMPLE_RATE/2;
    private final int RECORD_BUF_SIZE = HALF_SECONDS_SAMPLES * 4;
    private int floatsLeftToRegister = 0;

    private boolean stoppedRecording = false;
    private boolean isrecording = false;
    private LinkedBlockingQueue<byte[]> stream = new LinkedBlockingQueue();
    private AudioRecord recorder;
    //byte[] bytes = new byte[RECORD_BUF_SIZE];
    byte[] bytes = new byte[RECORD_BUF_SIZE/2];
    float[] floats = new float[HALF_SECONDS_SAMPLES];
    //데시벨 관련 변수
    private int level = 25;
    public int decibel_threshold = 10;
    public int meanDecibel = 0;
    private int meanTotal = 0;
    private int[] decibelArray = new int[60];
    private int check = 0;
    private int index = 0;
    private int firstMinuteCheck = 0;
    private final int bfs = AudioRecord.getMinBufferSize(SAMPLE_RATE,CHANNEL_CONFIG,AUDIO_FORMAT);

    //녹음 완료 관련 변수
    public int recordStart = 0;


    MediaRecord() {
    }

    public void run() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                RECORD_BUF_SIZE*5);
        recorder.startRecording();
        isrecording = true;
        decibelThread.start();
        int cnt = 0;
        try{
            Thread.sleep(5000);
        } catch(InterruptedException e){
            e.printStackTrace();
        }

        //while문 한번에 0.5초
        while (!stoppedRecording) {
            if(check == 1) {
                try {
                    if(cnt != 0) {
                        stream.put(bytes);
                        cnt++;
                        System.out.println(cnt + "번째 녹음중");
                    }
                    else {
                        if(level > meanDecibel+decibel_threshold) {
                            stream.put(bytes);
                            cnt++;
                            System.out.println(cnt + "번째 녹음중");
                            recordStart = 1;
                        }
                    }
                    if(cnt==5) {
                        cnt = 0;
                        recordStart = 0;
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    System.out.println(e);
                    stop();
                }
                check = 0;
            }

        }

        stop();
    }

    public void stop() {
        stoppedRecording = true;
    }


    public Iterator<byte[]> iterator() {
        class CustomIterator implements Iterator<byte[]> {
            private boolean nullReturned = false;

            public boolean hasNext() {
                return !nullReturned;
            }

            public byte[] next() {
                if (stoppedRecording) {
                    nullReturned = true;
                    return null;
                }

                try {
                    return stream.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }

        };
        return new CustomIterator();
    }

    Thread decibelThread = new Thread(new Runnable() {
        @Override
        public void run() {


            while(isrecording){
                int ret = recorder.read(bytes, 0, RECORD_BUF_SIZE/2, AudioRecord.READ_BLOCKING);
                int total = 0;
                System.out.println(ret);
                for(byte mbytes : bytes) {
                    total += Math.abs(mbytes);
                }
                level = (int) (total / ret);
                //level = level - 100;
                System.out.println(index+" "+level);
                check = 1;
                int temp = decibelArray[index];
                decibelArray[index] = level;
                meanTotal += level;
                index++;
                if(index == 60) {
                    firstMinuteCheck = 1;
                    index = 0;
                }
                if(firstMinuteCheck == 0) {
                    meanDecibel = meanTotal / index;
                } else {
                    meanTotal -= temp;
                    meanDecibel = meanTotal / 60;
                }
                System.out.println("평균 데시벨 : " + meanDecibel);
            }
        }
    });


}
