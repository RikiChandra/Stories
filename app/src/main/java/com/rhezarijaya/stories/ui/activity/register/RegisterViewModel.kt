package com.rhezarijaya.stories.ui.activity.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.rhezarijaya.stories.model.RegisterResponse
import com.rhezarijaya.stories.service.UNEXPECTED_DATA_ERROR
import com.rhezarijaya.stories.service.UNEXPECTED_ERROR
import com.rhezarijaya.stories.service.formatResponseCode
import com.rhezarijaya.stories.service.getAPIService
import com.rhezarijaya.stories.util.AppPreferences
import com.rhezarijaya.stories.util.SingleEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel(private val appPreferences: AppPreferences) : ViewModel() {
    private val registerData: MutableLiveData<RegisterResponse> = MutableLiveData()
    private val registerError: MutableLiveData<SingleEvent<String>> = MutableLiveData()
    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    fun getRegisterData(): LiveData<RegisterResponse> {
        return registerData
    }

    fun getRegisterError(): LiveData<SingleEvent<String>> {
        return registerError
    }

    fun isLoading(): LiveData<Boolean> {
        return isLoading
    }

    fun register(name: String, email: String, password: String) {
        isLoading.value = true

        getAPIService().register(name, email, password)
            .enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    isLoading.value = false

                    if (response.isSuccessful) {
                        response.body()?.let {
                            if (it.error as Boolean) {
                                registerError.value = SingleEvent(it.message as String)
                            } else {
                                registerData.value = response.body() as RegisterResponse
                            }
                        } ?: run {
                            registerError.value = SingleEvent(UNEXPECTED_DATA_ERROR)
                        }
                    } else {
                        val body: RegisterResponse? = Gson().fromJson(
                            response.errorBody()?.string(),
                            RegisterResponse::class.java
                        )

                        registerError.value =
                            SingleEvent(body?.message ?: formatResponseCode(response.code()))
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    isLoading.value = false
                    registerError.value = SingleEvent(UNEXPECTED_ERROR)
                }
            })
    }
}