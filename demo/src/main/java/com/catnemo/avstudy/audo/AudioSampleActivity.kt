package com.catnemo.avstudy.audo

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.catnemo.avstudy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.catnemo.codec.decode.AudioDecoder
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class AudioSampleActivity : AppCompatActivity() {
    companion object{
        private const val TEST_FILE = "001JTjCp0Xvv9D.m4a"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_sample)
        val audioDecode = AudioDecoder()
        val btnDecode = findViewById<Button>(R.id.btn_decode)
        val destFile = coyTestFile(btnDecode)
        btnDecode.setOnClickListener {
            startDecode(audioDecode, destFile)
        }

        findViewById<Button>(R.id.btn_decode_stop).setOnClickListener {
            audioDecode.stop()
        }
    }

    private fun startDecode(audioDecode: AudioDecoder, destFile: File) {
        var audioTrack: AudioTrack? = null
        audioDecode.initAudioDecode(destFile.path, object : AudioDecoder.AudioDecodeCallBack {
            override fun startDecode(sampleRate: Int, audioFormat: Int, channelCount: Int) {
                val audioChannelConfig = if (channelCount == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
                val bufferSize = AudioTrack.getMinBufferSize(sampleRate, audioChannelConfig, audioFormat)
                audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, audioChannelConfig,
                        audioFormat, bufferSize, AudioTrack.MODE_STREAM)
            }

            override fun decoding(byteBuffer: ByteBuffer) {
                val shortBuffer = byteBuffer.asShortBuffer()
                val length = shortBuffer.limit()
                val samples = ShortArray(length)
                shortBuffer.get(samples, 0, length)
                if (AudioTrack.SUCCESS == audioTrack?.write(samples, 0, length)) {
                    audioTrack?.play()
                }
            }

            override fun decodeEnd() {
                if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack?.stop()
                }
                audioTrack?.release()
            }
        })
        audioDecode.startDecode()
    }

    private fun coyTestFile(btnDecode: Button): File {
        val destFile = File(filesDir.path + File.separator + TEST_FILE)
        if (!destFile.exists()) {
            btnDecode.isEnabled = false
            CoroutineScope(Dispatchers.IO).launch {
                assets.open(TEST_FILE).use { input ->
                    FileOutputStream(destFile).use { outPut ->
                        val byteArray = ByteArray(2048)
                        while (input.read(byteArray) != -1) {
                            outPut.write(byteArray)
                            withContext(Dispatchers.Main) {
                                btnDecode.text = "转码中..."
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    btnDecode.isEnabled = true
                    btnDecode.text = getString(R.string.start_decode)
                }
            }
        }
        return destFile
    }
}