package com.rhezarijaya.stories.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rhezarijaya.stories.ui.activity.create.CreateStoryViewModel
import com.rhezarijaya.stories.ui.activity.login.LoginViewModel
import com.rhezarijaya.stories.ui.activity.main.MainViewModel
import com.rhezarijaya.stories.ui.activity.register.RegisterViewModel

class ViewModelFactory(private val appPreferences: AppPreferences) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateStoryViewModel::class.java)) {
            return CreateStoryViewModel(appPreferences) as T
        } else if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(appPreferences) as T
        } else if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(appPreferences) as T
        } else if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(appPreferences) as T
        }

        throw IllegalArgumentException(modelClass.name + " not found")
    }
}