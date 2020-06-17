package com.svbackend.cropofil.ui.home

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _title = MutableLiveData<String>().apply {
        value = "Cropofil"
    }
    val title: LiveData<String> = _title

    private val _subtitle = MutableLiveData<String>().apply {
        value = "Share photos in original quality and resolution"
    }
    val subtitle: LiveData<String> = _subtitle

    private val _url = MutableLiveData<String>().apply {
        value = ""
    }
    val url: LiveData<String> = _url

    fun setUrl(url: Uri?) {
        if (url == null) {
            return
        }

        setUrl(url.toString())
    }

    fun setUrl(url: String?) {
        if (url == null) {
            return
        }

        _url.apply { value = url }
    }
}