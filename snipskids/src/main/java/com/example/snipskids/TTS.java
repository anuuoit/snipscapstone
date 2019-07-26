package com.example.snipskids;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

public class TTS {

    private static TTS tts = null;
    private TextToSpeech mTTS = null;

    private TTS(){

    }
    public static TTS getInstance()
    {
        if (tts == null)
            tts = new TTS();

        return tts;
    }

    public Intent checkIfTTSPresent(){
        // Check to see if we have TTS voice data
        Intent ttsIntent = new Intent();
        ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        return ttsIntent;
    }

    public Intent installIntent() {
        // Data is missing, so we start the TTS
        // installation process
        Intent installIntent = new Intent();
        installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        return installIntent;
    }

    public void saySomething(String text, int qmode) {
        if (qmode == 1)
            mTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
        else
            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void initialize(Activity activity, TextToSpeech.OnInitListener listener) {
        // Data exists, so we instantiate the TTS engine
        mTTS = new TextToSpeech(activity, listener);
    }

    public void onInit(int status, Activity activity) {
        if (status == TextToSpeech.SUCCESS) {
            if (mTTS != null) {
                int result = mTTS.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(activity, "TTS language is not supported", Toast.LENGTH_LONG).show();
                } else {
//                    saySomething("TTS is ready", 0);
                }
            }
        } else {
            Toast.makeText(activity, "TTS initialization failed",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
    }

}
