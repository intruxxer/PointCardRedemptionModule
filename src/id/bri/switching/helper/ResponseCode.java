/**
 * Router
 *
 * Class yang memetakan deskripsi kode respon transaksi
 *
 * @package		id.bri.switching.helper
 * @author		PSD Team
 * @copyright           Copyright (c) 2013, PT. Bank Rakyat Indonesia (Persero) Tbk,
 */

// ---------------------------------------------------------------------------------

/*
 * ------------------------------------------------------
 *  Memuat package dan library
 * ------------------------------------------------------
 */

package id.bri.switching.helper;
import java.util.HashMap;
import java.util.Map;


//  Class ResponseCode
public class ResponseCode {
    
    /* 
     * Property
     * ---------------------------------------------------------------------
     */
    
    protected Map<String, String> responseCodeList = new HashMap<String, String>();
    
    /**
     * ResponseCode
     * ------------------------------------------------------------------------
     * 
     * Constructor
     * 
     * @access      public
     * @return      void
     */
    
    public ResponseCode(){
        responseCodeList.put("01", "Transaksi tidak ditemukan di merchant aggregator.");
        responseCodeList.put("02", "Pesanan sudah kadaluarsa.");
        responseCodeList.put("03", "Stok barang telah habis.");
        responseCodeList.put("04", "Validasi gagal pada Merchant Aggregator");
        responseCodeList.put("13", "Nominal transaksi tidak valid");
        responseCodeList.put("14", "Store ID tidak ditemukan");
        responseCodeList.put("30", "Salah format pesan.");
        responseCodeList.put("31", "customer does not have borndate in database");
        responseCodeList.put("99", "unknown response");
                
        responseCodeList.put("51", "Saldo tidak cukup.");
        responseCodeList.put("53", "Rekening tidak ditemukan.");
        responseCodeList.put("61", "Transaksi melebihi limit.");
        responseCodeList.put("93", "Sequence sudah pernah dipakai.");
        responseCodeList.put("91", "unknown response.");
        responseCodeList.put("Q1", "unknown response.");
        
        responseCodeList.put("79", "Transaksi ditolak.");
        responseCodeList.put("82", "Transaksi sedang diproses.");
        responseCodeList.put("90", "Link agregator tidak ditemukan");
        responseCodeList.put("92", "Exception error.");
        responseCodeList.put("E1", "Method pada MoGate tidak ditemukan.");
        responseCodeList.put("E2", "Hasil XML pada MoGate tidak valid");
        responseCodeList.put("E3", "Validasi gagal pada Merchant Aggregator");
        
    }
    
    // ---------------------------------------------------------------------------------
    
    /**
     * getResponseDescription
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk mengambil deskripsi error dari kode respon
     * 
     * @access      public
     * @param       String
     * @return      String
     */
    
    public String getResponseDescription(String responseCode){
        if(responseCodeList.containsKey(responseCode)){
            return responseCodeList.get(responseCode);
        }
        return "";
    }
    
    // ---------------------------------------------------------------------------------
}
