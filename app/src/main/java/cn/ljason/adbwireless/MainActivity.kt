package cn.ljason.adbwireless

import android.app.Activity
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.DataOutputStream
import kotlin.system.exitProcess

class MainActivity : Activity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        GlobalScope.launch(Dispatchers.Main) {
            if (execCmd("start"))
            {
                text.text = getString(R.string.success, getIPAddress(this@MainActivity))
            } else
            {
                text.text = resources.getString(R.string.failure)
            }
        }
        
        var status = true
        button.setOnClickListener {
            status = if (status)
            {
                button.text = resources.getString(R.string.start)
                !execCmd("stop")
            } else
            {
                button.text = resources.getString(R.string.stop)
                execCmd("start")
            }
        }
    }
    
    override fun onBackPressed()
    {
        exitProcess(0)
    }
    
    private fun getIPAddress(context: Context): String?
    {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        return intIP2String(wifiInfo.ipAddress)
    }
    
    private fun intIP2String(ip: Int): String
    {
        return (ip and 0xFF).toString() + "." +
            (ip shr 8 and 0xFF) + "." +
            (ip shr 16 and 0xFF) + "." +
            (ip shr 24 and 0xFF)
    }
    
    private fun execCmd(cmd: String): Boolean
    {
        var process: Process? = null
        var os: DataOutputStream? = null
        return try
        {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            with(os) {
                writeBytes(
                    "setprop service.adb.tcp.port 5555\n" +
                        "$cmd adbd\n" +
                        "exit\n"
                )
                flush()
            }
            process.waitFor() == 0
        } catch (t: Throwable)
        {
            false
        } finally
        {
            os?.close()
            process?.destroy()
        }
    }
}
