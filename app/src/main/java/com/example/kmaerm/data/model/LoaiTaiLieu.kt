package com.example.kmaerm.data.model

data class LoaiTaiLieu(
    val id: String,
    val ten: String,
    val mo_ta: String? = null
)

data class ThuTuc(
    val ten_thu_tuc: String,
    val tai_lieus: List<LoaiTaiLieu>
)

data class LoaiTaiLieuResponse(
    val data: List<ThuTuc>
)
