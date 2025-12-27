package com.example.kmaerm.utils

import android.view.Window
import android.view.WindowManager

/**
 * SecureScreenManager - Utility để chặn screenshot tại các màn hình nhạy cảm
 *
 * Áp dụng cho
 * - Màn hình nhập OTP
 * - Màn hình xem chi tiết giấy phép (blockchain, transaction hash)
 * - Màn hình chứa dữ liệu nhạy cảm khác
 */
object SecureScreenManager {

    /**
     * Bật chế độ bảo mật - Chặn screenshot và screen recording
     */
    fun enableSecureMode(window: Window?) {
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    /**
     * Tắt chế độ bảo mật - Cho phép screenshot trở lại
     */
    fun disableSecureMode(window: Window?) {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

