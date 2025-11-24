package com.example.kmaerm.data.model

data class GiayPhep(
    val id: String,
    val ho_so_id: String,
    val loai_giay_phep: String,
    val so_giay_phep: String,
    val ngay_hieu_luc: String?,
    val ngay_het_han: String?,
    val trang_thai_giay_phep: String,
    val trang_thai_blockchain: String,
    val created_at: String?,
    val updated_at: String?,
    val h1_hash: String?,
    val h2_hash: String?,
    val transaction_hash: String?,
    val file_duong_dan: String?,
    val ho_so: HoSo
)

data class GiayPhepListResponse(
    val data: List<GiayPhep>,
    val page: Int,
    val page_size: Int,
    val total: Int
)

data class CreateGiayPhepRequest(
    val ho_so_id: String,
    val loai_giay_phep: String,
    val so_giay_phep: String,
    val ngay_hieu_luc: String,
    val ngay_het_han: String,
    val trang_thai_giay_phep: String
)

data class CreateGiayPhepResponse(
    val id: String,
    val ho_so_id: String,
    val loai_giay_phep: String,
    val so_giay_phep: String,
    val ngay_hieu_luc: String,
    val ngay_het_han: String,
    val trang_thai_giay_phep: String,
    val trang_thai_blockchain: String
)

data class UpdateGiayPhepRequest(
    val loai_giay_phep: String,
    val so_giay_phep: String,
    val ngay_hieu_luc: String,
    val ngay_het_han: String,
    val trang_thai_giay_phep: String
)

data class VerifyBlockchainRequest(
    val ho_so_id: String,
    val loai_giay_phep: String,
    val so_giay_phep: String,
    val ngay_hieu_luc: String,
    val ngay_het_han: String,
    val trang_thai_giay_phep: String
)
