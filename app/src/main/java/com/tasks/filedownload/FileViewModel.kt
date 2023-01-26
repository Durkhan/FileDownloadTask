package com.tasks.filedownload

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FileViewModel :ViewModel() {
    var state= MutableLiveData<String>()
    init {
        state.value="Tab below button to download file"
    }
    fun completedDownload(){
        viewModelScope.launch {
            state.value="Completed Download"
        }
    }
    fun downloading(){
        viewModelScope.launch {
            state.value="File Downloading"
        }
    }
    fun canceledDownload(){
        viewModelScope.launch {
            state.value="Canceled Download"
        }
    }
}