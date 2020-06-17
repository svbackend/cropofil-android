package com.svbackend.cropofil.ui.home

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toFile
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.svbackend.cropofil.R
import com.svbackend.cropofil.ui.ScopedFragment
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File
import java.util.zip.ZipFile


class HomeFragment : ScopedFragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val title: TextView = root.findViewById(R.id.title_text)
        homeViewModel.title.observe(viewLifecycleOwner, Observer {
            title.text = it
        })

        val subtitle: TextView = root.findViewById(R.id.subtitle_text)
        homeViewModel.subtitle.observe(viewLifecycleOwner, Observer {
            subtitle.text = it
        })

        val url: EditText = root.findViewById(R.id.url_edit_text)
        homeViewModel.url.observe(viewLifecycleOwner, Observer {
            url.setText(it)
        })

        val uri = activity?.intent?.data;
        homeViewModel.setUrl(uri)

        val downloadBtn = root.findViewById<Button>(R.id.download_btn);
        downloadBtn.setOnClickListener { startDownloading() };

        return root
    }

    private fun startDownloading() {
        val downloadUrl = "${url_edit_text.text}/download"

        if (!URLUtil.isNetworkUrl(downloadUrl)) {
            Toast.makeText(context, "$downloadUrl is not a valid url", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(context, "Downloading started.. Please wait", Toast.LENGTH_LONG).show()
        //val galleryId = homeViewModel.text.value!!.split("/").last()

        val request = DownloadManager
            .Request(Uri.parse(downloadUrl))
            .setTitle("cropofil.zip")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "cropofil.zip")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)

        val dm = activity!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = dm.enqueue(request)

        val receiver = object:BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val id = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val cursor = dm.query(DownloadManager.Query().setFilterById(id));
                    cursor.moveToFirst();

                    var downloadedTo = Uri.parse(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))

                    val file = downloadedTo.toFile()

                    println("=====CHECKS:=======")
                    println(file.isFile)
                    println(file.exists())
                    println(file.canRead())
                    println(file.setReadable(true))

                    try {
                        unzipGallery(file)
                        //downloadGallery(downloadUrl, downloadedFile)
                    } catch (e: Throwable) {
                        println("========ERROR=======")
                        e.printStackTrace()
                        println("========ERROR=======")
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        activity!!.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private fun unzipGallery(zipFile: String) {
        unzipGallery(File(zipFile))
    }

    private fun unzipGallery(zipFile: File) {
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                println(entry.name)
                zip.getInputStream(entry).use { input ->
                    File(entry.name).outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}