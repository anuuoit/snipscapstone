package com.example.snipskids;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ai.snips.hermes.IntentMessage;
import ai.snips.hermes.SessionEndedMessage;
import ai.snips.hermes.TextCapturedMessage;
import ai.snips.platform.SnipsPlatformClient;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String TAG = "MainActivity";

    private File assistantLocation;
    private Activity activity;
    private TextView textViewInfo;
    private TextView textViewResult;
    private ImageView imageViewIcon;
    private final int ACT_CHECK_TTS_DATA = 1000;
    private MediaPlayer mPlayer;
    private SnipsPlatformClient mClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        textViewInfo = findViewById(R.id.tv_info);
        textViewResult = findViewById(R.id.tv_result);
        imageViewIcon = findViewById(R.id.action_image);

        assistantLocation = new File(getFilesDir(), "snips");
        extractAssistantIfNeeded(assistantLocation);
        if (ensurePermissions()) {
            startSnips(assistantLocation);
        }
        startActivityForResult(TTS.getInstance().checkIfTTSPresent(), ACT_CHECK_TTS_DATA);
    }

    private boolean ensurePermissions() {
        int status = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        if (status != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 0);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSnips(assistantLocation);
        }
    }

    private void startSnips(File snipsDir) {
        mClient = createClient(snipsDir);
        mClient.connect(this.getApplicationContext());
    }

    private void extractAssistantIfNeeded(File assistantLocation) {
        File versionFile = new File(assistantLocation,
                "android_version_" + BuildConfig.VERSION_NAME);

        if (versionFile.exists()) {
            return;
        }

        try {
            assistantLocation.delete();
            MainActivity.unzip(getBaseContext().getAssets().open("assistant.zip"),
                    assistantLocation);
            versionFile.createNewFile();
        } catch (IOException e) {
            return;
        }
    }

    private SnipsPlatformClient createClient(File assistantLocation) {
        File assistantDir  = new File(assistantLocation, "assistant");
        Log.d(TAG, "assistant=>" + assistantLocation);
        final SnipsPlatformClient client = new SnipsPlatformClient.Builder(assistantDir)
                .enableDialogue(true)
                .enableHotword(true)
                .enableSnipsWatchHtml(false)
                .enableLogs(true)
                .withHotwordSensitivity(0.5f)
                .enableStreaming(false)
                .enableInjection(false)
                .build();

        client.setOnPlatformReady(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                Log.d(TAG, "Snips is ready. Say the wake word!");
                textViewInfo.setText(R.string.wake_word);
                imageViewIcon.setImageResource(R.drawable.ready);
                return null;
            }
        });

        client.setOnPlatformError(new Function1<SnipsPlatformClient.SnipsPlatformError, Unit>() {
            @Override
            public Unit invoke(final SnipsPlatformClient.SnipsPlatformError snipsPlatformError) {
                // Handle error
                Log.d(TAG, "Error: " + snipsPlatformError.getMessage());
                return null;
            }
        });

        client.setOnHotwordDetectedListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                // Wake word detected, start a dialog session
                Log.d(TAG, "Wake word detected!");
                textViewInfo.setText(R.string.voice_command);
                imageViewIcon.setImageResource(R.drawable.listening);
                imageViewIcon.setVisibility(View.VISIBLE);
                textViewResult.setVisibility(View.GONE);

                client.startSession(null, new ArrayList<String>(),
                        false, null);
                return null;
            }
        });

        client.setOnIntentDetectedListener(new Function1<IntentMessage, Unit>() {
            @Override
            public Unit invoke(final IntentMessage intentMessage) {
                // Intent detected, so the dialog session here
                client.endSession(intentMessage.getSessionId(), null);
                final String intentName = intentMessage.getIntent().getIntentName();

                textViewInfo.setText("");
                imageViewIcon.setVisibility(View.VISIBLE);
                textViewResult.setVisibility(View.GONE);
                if (mPlayer != null) {
                    mPlayer.stop();
                }
                Response intentResponse = ProcessIntentMessage.getInstance().process(intentMessage);
                if (intentName.equals("anuprakash:add")
                        || intentName.equals("anuprakash:minus")
                        || intentName.equals("anuprakash:multiply")
                        || intentName.equals("anuprakash:divide")) {

                    if (intentResponse != null) {
                        textViewResult.setText(intentResponse.getResponseString());
                        imageViewIcon.setVisibility(View.GONE);
                        textViewResult.setVisibility(View.VISIBLE);
                    }

                } else if (intentName.equals("anuprakash:playitem")) {
                    if (intentMessage != null) {
                        if (intentResponse.getAction() == Response.Action.MUSIC) {
                            mPlayer = MediaPlayer.create(MainActivity.this, R.raw.twinkle);
                            mPlayer.start();
                        }
                    }
                } else if (intentName.equals("anuprakash:tell")) {
                    if (intentMessage != null) {
                        if(intentResponse.getAction() == Response.Action.STORY){
                            mPlayer = MediaPlayer.create(MainActivity.this, R.raw.story);
                            mPlayer.start();
                        }
                    }
                }

                textViewInfo.setText(R.string.wake_word_continue);
                if (intentResponse != null) {
                    imageViewIcon.setImageResource(intentResponse.getIconRes());
                    TTS.getInstance()
                            .saySomething(
                                    intentResponse.getResponseString() == null ? getString(intentResponse.getResponseStringRes()) : intentResponse.getResponseString(),
                                    1);
                } else {
                    imageViewIcon.setImageResource(R.drawable.confused);
                    TTS.getInstance().saySomething(getString(R.string.resp_not_understand),1);
                }
                return null;
            }
        });

        client.setOnSnipsWatchListener(new Function1<String, Unit>() {
            public Unit invoke(final String s) {
                Log.d(TAG, "Log: " + s);
                return null;
            }
        });

        client.setOnSessionEndedListener(new Function1<SessionEndedMessage, Unit>() {
            @Override
            public Unit invoke(SessionEndedMessage sessionEndedMessage) {
                Log.d("Anu", "setOnSessionEndedListener");
                return null;
            }
        });

        client.setOnTextCapturedListener(new Function1<TextCapturedMessage, Unit>() {
            @Override
            public Unit invoke(TextCapturedMessage textCapturedMessage) {
                Log.d("Anu", "setOnTextCapturedListener");
                imageViewIcon.setImageResource(R.drawable.ready);
                textViewInfo.setText(R.string.wake_word);
                return null;
            }
        });

        return client;
    }

    private static void unzip(InputStream zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipFile));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            }
        } finally {
            zis.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            // Data exists, so we instantiate the TTS engine
            TTS.getInstance().initialize(activity, this);
        } else {
            // Data is missing, so we start the TTS
            // installation process
            startActivity(TTS.getInstance().installIntent());
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("Anu", "onDestroy");
        super.onDestroy();
        TTS.getInstance().onDestroy();
        if(mPlayer != null) {
            mPlayer.stop();
        }
        if (mClient != null) {
            mClient.disconnect();
        }
    }

    @Override
    public void onInit(int status) {
        TTS.getInstance().onInit(status, activity);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mClient.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mClient.resume();
    }
}
