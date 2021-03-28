package com.catnemo.avstudy.audo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.catnemo.avstudy.R
import top.catnemo.codec.decode.AudioDecoder

class AudioSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_sample)
    }
}