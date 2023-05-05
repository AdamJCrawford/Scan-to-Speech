package com.example.myscantospeech.ui.settings

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _volume = MutableLiveData<String>().apply {
        value = "Volume Slider"
    }
    private val _option2 = MutableLiveData<String>().apply {
        value = "Option 2"
    }

    val volume: LiveData<String> = _volume
    val option2: LiveData<String> = _option2

}