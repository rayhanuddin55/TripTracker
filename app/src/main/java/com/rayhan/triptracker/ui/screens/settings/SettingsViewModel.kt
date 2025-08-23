package com.rayhan.triptracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhan.triptracker.data.repo.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repo: SettingsRepository) : ViewModel() {

    val interval =
        repo.intervalSeconds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val background =
        repo.backgroundEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val darkMode =
        repo.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setInterval(sec: Int) = viewModelScope.launch { repo.setInterval(sec) }
    fun setBackground(enabled: Boolean) = viewModelScope.launch { repo.setBackground(enabled) }
    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { repo.setDarkMode(enabled) }

}
