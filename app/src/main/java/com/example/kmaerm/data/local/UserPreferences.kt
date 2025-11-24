package com.example.kmaerm.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_ROLE_ID = "role_id"
        private const val KEY_ROLE_NAME = "role_name"
        private const val KEY_DOANH_NGHIEP_ID = "doanh_nghiep_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    fun saveUserData(
        userId: String,
        email: String,
        fullName: String,
        roleId: String,
        roleName: String,
        doanhNghiepId: String?,
        accessToken: String
    ) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_EMAIL, email)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_ROLE_ID, roleId)
            putString(KEY_ROLE_NAME, roleName)
            putString(KEY_DOANH_NGHIEP_ID, doanhNghiepId)
            putString(KEY_ACCESS_TOKEN, accessToken)
            apply()
        }
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)
    fun getRoleId(): String? = prefs.getString(KEY_ROLE_ID, null)
    fun getRoleName(): String? = prefs.getString(KEY_ROLE_NAME, null)
    fun getDoanhNghiepId(): String? = prefs.getString(KEY_DOANH_NGHIEP_ID, null)
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun clearUserData() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null
}

