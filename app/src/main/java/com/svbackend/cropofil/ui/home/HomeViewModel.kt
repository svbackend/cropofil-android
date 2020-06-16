package com.svbackend.cropofil.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

    fun setUrl(url: String?) {
        if (url == null) {
            return
        }

        _text.apply { value = url }
    }
}