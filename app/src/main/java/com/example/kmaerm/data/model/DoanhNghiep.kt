package com.example.kmaerm.data.model

data class DoanhNghiep(
    val id: String,
    val ten_doanh_nghiep_vi: String,
    val ten_doanh_nghiep_en: String,
    val ten_viet_tat: String,
    val dia_chi: String,
    val ma_so_doanh_nghiep: String,
    val ngay_cap_msdn_lan_dau: String,
    val noi_cap_msdn: String,
    val sdt: String,
    val email: String,
    val website: String?,
    val von_dieu_le: String,
    val nguoi_dai_dien: String,
    val chuc_vu: String,
    val loai_dinh_danh: String,
    val ngay_cap_dinh_danh: String,
    val noi_cap_dinh_danh: String,
    val status: Boolean,
    val created_at: String,
    val updated_at: String,
    val ho_sos: List<HoSo> = emptyList()
)

data class DoanhNghiepResponse(
    val data: DoanhNghiep
)

data class DoanhNghiepListResponse(
    val data: List<DoanhNghiep>,
    val page: Int,
    val page_size: Int,
    val total: Int
)
