package com.example.shop.dto;

public class CartItem {
    private Long idSanPham;
    private String tenSanPham;
    private String hinhAnh;
    private Double gia;
    private int soLuong;
    private String kichThuoc;
    private String mauSac;
    private Long idBienThe;

    // Getter và Setter
    public Long getIdSanPham() { return idSanPham; }
    public void setIdSanPham(Long idSanPham) { this.idSanPham = idSanPham; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public Double getGia() { return gia; }
    public void setGia(Double gia) { this.gia = gia; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getKichThuoc() { return kichThuoc; }
    public void setKichThuoc(String kichThuoc) { this.kichThuoc = kichThuoc; }
    
    public String getMauSac() { return mauSac; }
    public void setMauSac(String mauSac) { this.mauSac = mauSac; }
    
    public Long getIdBienThe() { return idBienThe; }
    public void setIdBienThe(Long idBienThe) { this.idBienThe = idBienThe; }
}
