package model;

public class DetailPenjualan {
    private int idDetail;
    private int idPenjualan;
    private int idBuku;
    private String kodeBuku;
    private String judul;
    private int qty;
    private double hargaJual;
    private double subtotal;

    public DetailPenjualan() {}

    public DetailPenjualan(int idDetail, int idPenjualan, int idBuku,
                           String kodeBuku, String judul, int qty,
                           double hargaJual, double subtotal) {
        this.idDetail = idDetail;
        this.idPenjualan = idPenjualan;
        this.idBuku = idBuku;
        this.kodeBuku = kodeBuku;
        this.judul = judul;
        this.qty = qty;
        this.hargaJual = hargaJual;
        this.subtotal = subtotal;
    }

    public int getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(int idDetail) {
        this.idDetail = idDetail;
    }

    public int getIdPenjualan() {
        return idPenjualan;
    }

    public void setIdPenjualan(int idPenjualan) {
        this.idPenjualan = idPenjualan;
    }

    public int getIdBuku() {
        return idBuku;
    }

    public void setIdBuku(int idBuku) {
        this.idBuku = idBuku;
    }

    public String getKodeBuku() {
        return kodeBuku;
    }

    public void setKodeBuku(String kodeBuku) {
        this.kodeBuku = kodeBuku;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getHargaJual() {
        return hargaJual;
    }

    public void setHargaJual(double hargaJual) {
        this.hargaJual = hargaJual;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
