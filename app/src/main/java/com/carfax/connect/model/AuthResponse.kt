package com.carfax.connect.model

import com.google.gson.annotations.SerializedName
import java.util.*

class AuthResponse{
    @SerializedName("access_token")
    var accessToken: String = ""
    @SerializedName("refresh_token")
    var refreshToken: String = ""
    @SerializedName("expires_in")
    var expiresIn: Int = 0
    var expiresAt: Date? = null

    fun calculateExpiresAt(){
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, expiresIn)
        expiresAt = calendar.time
    }
}