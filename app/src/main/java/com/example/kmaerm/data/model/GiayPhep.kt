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

/**
 * Response from push blockchain API
 * API returns: { "message": "Đã đẩy h1 và h2 lên blockchain thành công" }
 */
data class BlockchainPushResponse(
    val message: String
)

/**
 * Response from verify blockchain API
 */
data class BlockchainVerifyResponse(
    val giay_phep_id: String,
    val h1_hash_db: String?,
    val h2_hash_db: String?,
    val h1_hash_bc: String?,
    val h2_hash_bc: String?,
    val is_h1_matched: Boolean,
    val is_h2_matched: Boolean,
    val message: String,
    val giay_phep_data: GiayPhep?
)

