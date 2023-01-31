package com.tasks.filedownload

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import com.tasks.filedownload.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL


class MainActivity : AppCompatActivity(),EasyPermissions.PermissionCallbacks {
    private lateinit var binding:ActivityMainBinding
    private var  isCancel=true
    private val dir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + "ffn.pdf")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        requestPermisssions()
        val url = "https://rl8r0h01fo.pdcdn1.top/dl2.php?id=60737129&h=7973d78e5006c3382040e49d7e1aff63&u=cache&ext=pdf&n=1-2-3%20magic%203-step%20discipline%20for%20calm%20effective%20and%20happy%20parenting"

        implementedCustomFlowOperator()

        val downloader=Downloader()
        val scope= CoroutineScope(Dispatchers.IO)
        binding.retry.setOnClickListener {
            requestPermisssions()
            isCancel=!isCancel
            if (!isCancel){
                binding.retry.text="Retry"
                downloader.downloadFile(url,dir,scope,binding.checkdownloading)

            }
            else{
                downloader.cancelDownload()
                downloader.downloadFile(url,dir,scope,binding.checkdownloading)
            }


        }
    }



    class Downloader {
        private var job: Job? = null

        fun downloadFile(
            url: String,
            file: File,
            scope: CoroutineScope,
            progressbar: ProgressBar
        ) {
            job = scope.launch {
                try {
                        val url = URL(url)
                        val connection = url.openConnection()
                        val inputStream = connection.getInputStream()
                        val buffer = BufferedInputStream(inputStream)
                        val totalSize=connection.contentLength
                        val output = FileOutputStream(file)
                        val data = ByteArray(1024)
                        var total: Long = 0
                        var count: Int
                        progressbar.max=totalSize
                        while (buffer.read(data).also { count = it } != -1) {
                            if (isActive) {
                                total += count.toLong()
                                progressbar.progress=total.toInt()
                                output.write(data, 0,count)
                            } else {
                                break
                            }
                        }
                        output.flush()
                        output.close()
                        buffer.close()

                } catch (e: Exception) {
                    Log.e("Downloader", "Download failed", e)
                }
            }
        }

        fun cancelDownload() {
            job?.cancel()
        }
    }



    private fun <A, B, C> Flow<A>.withLatestFrom(other: Flow<B>, transform: suspend (A, B) -> C): Flow<C> {
        return combine(other) { a, b -> transform(a, b) }
    }
    private val flowA = flowOf(1, 2, 3)
    private val flowB = flowOf("A", "B", "C")


    private fun implementedCustomFlowOperator() {
        lifecycleScope.launchWhenCreated {
            flowA.withLatestFrom(flowB) { a, b -> "$a $b" }
                .collect { Log.d("Result",it) }
        }
    }




    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms))
            AppSettingsDialog.Builder(this).build().show()
        else
            requestPermisssions()

    }

    private fun requestPermisssions() {
        if (EasyPermissions.hasPermissions(this,Manifest.permission.WRITE_EXTERNAL_STORAGE))
            return
        EasyPermissions.requestPermissions(
            this,
            "You need to accept storage permission to use this app",
            101,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    }
}

