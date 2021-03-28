package catnemo.top.airhockey.util

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 从文件中读取字符串
 * @author matrixJoy
 * @version V1.0
 * @since 2018/11/30
 *
 */
object TextResourceReader {
    private var TAG = "ZCamera"
    fun readTextFileFromResource(context: Context, resId: Int): String {
        val body = StringBuilder()
        try {
            val ins = context.resources.openRawResource(resId)
            val insReader = InputStreamReader(ins)
            val bufReader = BufferedReader(insReader)
            var line = bufReader.readLine()
            while (line != null) {
                body.append(line)
                body.append("\n")
                line = bufReader.readLine()
            }
        } catch (e: Exception) {
            Log.e(TAG, "exception $e")
        }
        return body.toString()
    }
}