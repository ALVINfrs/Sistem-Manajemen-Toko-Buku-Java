package model;

public class DetailPembelian {
    private int idDetail;
    private int idPembelian;
    private int idBuku;
    private String kodeBuku;
    private String judul;
    private int qty;
    private double hargaBeli;
    private double subtotal;

    public DetailPembelian() {}

    public DetailPembelian(int idDetail, int idPembelian, int idBuku,
                           String kodeBuku, String judul, int qty,
                           double hargaBeli, double subtotal) {
        this.idDetail = idDetail;
        this.idPembelian = idPembelian;
        this.idBuku = idBuku;
        this.kodeBuku = kodeBuku;
        this.judul = judul;
        this.qty = qty;
        this.hargaBeli = hargaBeli;
        this.subtotal = subtotal;
    }

    public int getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(int idDetail) {
        this.idDetail = idDetail;
    }

    public int getIdPembelian() {
        return idPembelian;
    }

    public void setIdPembelian(int idPembelian) {
        this.idPembelian = idPembelian;
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

    public double getHargaBeli() {
        return hargaBeli;
    }

    public void setHargaBeli(double hargaBeli) {
        this.hargaBeli = hargaBeli;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
