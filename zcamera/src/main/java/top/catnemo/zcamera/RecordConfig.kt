package top.catnemo.zcamera

/**
 * @description 录制配置类 一些必要的录制参数
 * @author  MatrixJoy
 * @date    2019-11-07   19:19
 */
data class RecordConfig(var recordWith: Int,
                        var recordHeight: Int,
                        var outPutPath: String) {
    var isMute = false
    var speed = 1.0f


    override fun toString(): String {
        return "RecordConfig(recordWith=$recordWith, recordHeight=$recordHeight, outPutPath='$outPutPath', isMute=$isMute, speed=$speed)"
    }

}