package top.catnemo.zcamera.record.audio

/**
 *
 * @author  MatrixJoy
 * @description 音频录制接口
 * @date    2019-11-12   15:27
 */
interface IAudioRecorder {

    fun onAudioRecord(byteArray: ByteArray, size: Int)

    fun onAudiRecordError(errorMsg: String)

    fun onAudioRecordStart(sampleRate: Int, channelCont: Int, bitRate: Int, minBufferSize: Int)

    fun onAudioRecordStop()
}