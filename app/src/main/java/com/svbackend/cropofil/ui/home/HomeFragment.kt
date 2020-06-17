package com.svbackend.cropofil.ui.home

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.svbackend.cropofil.R
import com.svbackend.cropofil.data.Photo
import com.svbackend.cropofil.ui.ScopedFragment
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


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
        downloadBtn.setOnClickListener {
            val galleryUrl = url_edit_text.text.toString();
            if (!URLUtil.isNetworkUrl(galleryUrl)) {
                Toast.makeText(context, "$galleryUrl is not a valid url", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val galleryShortcut = galleryUrl.split("/g/").last()
            val photosUrl = "https://cropofil.com/api/g/$galleryShortcut"
            FetchGalleryTask().execute(photosUrl)
        };

        return root
    }

    private fun startDownloading(photo: Photo) {
        val downloadToDir = File(Environment.DIRECTORY_PICTURES.toString() + File.separator + "Cropofil");

        if (!downloadToDir.exists()) {
            downloadToDir.mkdirs()
        }

        val request = DownloadManager
            .Request(Uri.parse(photo.url))
            .setTitle(photo.clientFilename)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Cropofil${File.separator}${photo.clientFilename}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val dm = activity!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }

    inner class FetchGalleryTask : AsyncTask<String, String, Unit>() {
        override fun doInBackground(vararg url: String?) {
            val json : String;
            val galleryUrl = url[0];
            val conn = URL(galleryUrl).openConnection() as HttpURLConnection
            try {
                conn.connect()
                json = conn.inputStream.use { it.reader().use { reader -> reader.readText() } }
            } finally {
                conn.disconnect()
            }

            val jsonResponse = JSONObject(json)
            val photos = jsonResponse.getJSONArray("photos")

            for(i in 0 until photos.length()) {
                val photo = photos.getJSONObject(i)
                startDownloading(Photo(
                    photo.getString("url"),
                    photo.getString("client_filename")
                ))
            }
        }
    }
}