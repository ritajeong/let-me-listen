package com.example.letmelisten;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class MediaRecord implements Runnable, Iterable<byte[]> {
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    public static final int SAMPLE_RATE = 22050;

    private final int HALF_SECONDS_SAMPLES = SAMPLE_RATE/2;
    private final int RECORD_BUF_SIZE = HALF_SECONDS_SAMPLES * 4;
    private int floatsLeftToRegister = 0;

    private boolean stoppedRecording = false;
    private LinkedBlockingQueue<byte[]> stream = new LinkedBlockingQueue();

    MediaRecord() {
    }

    public void run() {
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                RECORD_BUF_SIZE * 10);
        recorder.startRecording();

        byte[] bytes = new byte[RECORD_BUF_SIZE];
        float[] floats = new float[HALF_SECONDS_SAMPLES];
        while (!stoppedRecording) {
            recorder.read(floats, 0, HALF_SECONDS_SAMPLES, AudioRecord.READ_BLOCKING);

            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(floats);

            try {
                stream.put(bytes);
            } catch (InterruptedException e) {
//                System.out.println(e);
                stop();
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
}



/* 실행 방법

SenseMediaRecorder recorder = new MediaRecorder(30);
Thread recorderThread  = new Thread(recorder);
recorderThread.start();

*/


/*
        public class AudioRecordPlugin implements EventChannel.StreamHandler {
            // Audio recorder + initial values
            private static volatile AudioRecord recorder;
            private int AUDIO_SOURCE = MediaRecorder.AudioSource.DEFAULT;
            private int SAMPLE_RATE = 22050;
            private int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
            private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
            private int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 4;
            private float[] pre_data_f = null;
            private float[] new_data_f = null;
            private final byte[] data_b = new byte[SAMPLE_RATE * 4];

            // Runnable management
            private volatile boolean record = false;
            private volatile boolean isRecording = false;
            private final int RECORD_SIZE = SAMPLE_RATE/2;

            private final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    isRecording = true;

                    // Record audio (for 1 second) first
                    new_data_f = new float[SAMPLE_RATE];
                    recorder.read(new_data_f, 0, SAMPLE_RATE, AudioRecord.READ_BLOCKING);
                    ByteBuffer.wrap(data_b).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(new_data_f);


                    pre_data_f = new_data_f;

                    // Repeatedly push audio samples to stream
                    while (record) {
                        new_data_f = new float[SAMPLE_RATE];
                        // Copy pre 0.5 second audio data to new buffer -- A
                        System.arraycopy(pre_data_f, RECORD_SIZE, new_data_f, 0, RECORD_SIZE);

                        // Record audio (for 0.5 second) -- B
                        recorder.read(new_data_f, RECORD_SIZE, RECORD_SIZE, AudioRecord.READ_BLOCKING);

                        // A + B = 1 second audio data
                        ByteBuffer.wrap(data_b).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(new_data_f);

                        pre_data_f = new_data_f;
                    }
                    isRecording = false;
                }
            };

            /// Bug fix by https://github.com/Lokhozt
            /// following https://github.com/flutter/flutter/issues/34993
            private static class MainThreadEventSink implements EventChannel.EventSink {
                private final EventChannel.EventSink eventSink;
                private final Handler handler;

                MainThreadEventSink(final EventChannel.EventSink eventSink) {
                    this.eventSink = eventSink;
                    handler = new Handler(Looper.getMainLooper());
                }

                @Override
                public void success(final Object o) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            eventSink.success(o);
                        }
                    });
                }

                @Override
                public void error(final String s, final String s1, final Object o) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            eventSink.error(s, s1, o);
                        }
                    });
                }

                @Override
                public void endOfStream() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            eventSink.endOfStream();
                        }
                    });
                }
            }
            /// End


                recorder = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
                recorder.startRecording();

                // Start runnable
                record = true;
                new Thread(runnable).start();
            }
            @Override
            public void onCancel(final Object o) {
                // Stop runnable
                record = false;

                // Stop and reset audio recorder
                recorder.stop();
                recorder.release();
                recorder = null;
            }
        }

*/



/*
private final Iterable<byte[]> streamer;
ByteArrayOutputStream buffer = new ByteArrayOutputStream(halfSecondBytesNumber);

for (byte[] bytes : streamer) {
    if (bytes == null) {
        break;
    }
    buffer.write(bytes);
    if(buffer.size() >= halfSecondBytesNumber) {
        byte[] toBeSent = buffer.toByteArray();
        int startIndex = 0;
        while(startIndex < toBeSent.length) {
            int endIndex = Math.min(toBeSent.length, startIndex + Constants.MAX_DATA_SIZE);

            ByteString data = ByteString.copyFrom(toBeSent, startIndex, endIndex);

            // data 사용

            startIndex = endIndex;
        }
        buffer.reset();
    }
}
 */