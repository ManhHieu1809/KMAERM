package com.example.kmaerm.data.model

data class HoSo(
    val id: String,
    val doanh_nghiep_id: String,
    val ten_doanh_nghiep_vi: String,
    val ma_ho_so: String,
    val loai_thu_tuc: String,
    val ngay_dang_ky: String,
    val ngay_tiep_nhan: String? = null,
    val ngay_hen_tra: String? = null,
    val trang_thai_ho_so: String,
    val so_giay_phep_theo_ho_so: String? = null,
    val ho_so_tai_lieus: List<HoSoTaiLieu>? = null
)

data class HoSoTaiLieu(
    val id: String,
    val loai_tai_lieu: LoaiTaiLieu,
    val tai_lieus: List<TaiLieu>? = null
)

data class HoSoListResponse(
    val data: List<HoSo>,
    val page: Int,
    val page_size: Int,
    val total: Int
)

data class HoSoDetailResponse(
    val data: HoSo? = null,
    val id: String? = null,
    val doanh_nghiep_id: String? = null,
    val ten_doanh_nghiep_vi: String? = null,
    val ma_ho_so: String? = null,
    val loai_thu_tuc: String? = null,
    val ngay_dang_ky: String? = null,
    val ngay_tiep_nhan: String? = null,
    val ngay_hen_tra: String? = null,
    val trang_thai_ho_so: String? = null,
    val so_giay_phep_theo_ho_so: String? = null,
    val ho_so_tai_lieus: List<HoSoTaiLieu>? = null
) {
    fun toHoSo(): HoSo {
        return data ?: HoSo(
            id = id!!,
            doanh_nghiep_id = doanh_nghiep_id!!,
            ten_doanh_nghiep_vi = ten_doanh_nghiep_vi!!,
            ma_ho_so = ma_ho_so!!,
            loai_thu_tuc = loai_thu_tuc!!,
            ngay_dang_ky = ngay_dang_ky!!,
            ngay_tiep_nhan = ngay_tiep_nhan,
            ngay_hen_tra = ngay_hen_tra,
            trang_thai_ho_so = trang_thai_ho_so!!,
            so_giay_phep_theo_ho_so = so_giay_phep_theo_ho_so,
            ho_so_tai_lieus = ho_so_tai_lieus
        )
    }
}

data class CreateHoSoRequest(
    val doanh_nghiep_id: String,
    val loai_thu_tuc: String,
    val ngay_dang_ky: String
)

data class CreateHoSoResponse(
    val id: String,
    val doanh_nghiep_id: String,
    val ten_doanh_nghiep_vi: String,
    val ma_ho_so: String,
    val loai_thu_tuc: String,
    val ngay_dang_ky: String,
    val trang_thai_ho_so: String
)

data class UpdateHoSoRequest(
    val ngay_dang_ky: String? = null,
    val ngay_tiep_nhan: String? = null,
    val ngay_hen_tra: String? = null,
    val trang_thai_ho_so: String? = null
)
