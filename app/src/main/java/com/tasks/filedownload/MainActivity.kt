package com.tasks.filedownload

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.tasks.filedownload.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import okhttp3.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class MainActivity : AppCompatActivity(),EasyPermissions.PermissionCallbacks {
    private lateinit var binding:ActivityMainBinding
    private var iscancel =false;
    private var job: Job? = null
    private val viewModel:FileViewModel by viewModels()
    val dir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + "fn.pdf")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        // create the coroutine scope
        val scope = CoroutineScope(Dispatchers.IO)
        requestPermisssions()

        viewModel.state.observe(this, Observer {
            binding.checkdownloading.text = it

        })


        binding.retry.setOnClickListener {
            requestPermisssions()
            iscancel=!iscancel
            if (!iscancel){
                binding.retry.text = "Retry"
                job!!.cancel()
            }
             else{
                binding.retry.text = "Cancel"

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://rl8r0h01fo.pdcdn1.top/dl2.php?id=60737129&h=7973d78e5006c3382040e49d7e1aff63&u=cache&ext=pdf&n=1-2-3%20magic%203-step%20discipline%20for%20calm%20effective%20and%20happy%20parenting")
                    .build()
                job = scope.launch {
                                client.newCall(request).await()
                            }

             }
        }

    }




   private suspend fun Call.await(): Response {
        return suspendCancellableCoroutine { continuation ->
            enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    Log.d("TAG","Downloading")
                    viewModel.downloading()
                    val data = response.body?.bytes()

                    if (data != null) {
                        dir.writeBytes(data)
                    }


                    Log.d("TAG","Downloading Completed")
                    viewModel.completedDownload()
                    continuation.resume(response)

                }

                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isCancelled) return
                    continuation.resumeWithException(e)
                }
            })

            continuation.invokeOnCancellation {
                try {
                    viewModel.canceledDownload()
                    cancel()
                } catch (ex: Throwable) {

                }
            }
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


