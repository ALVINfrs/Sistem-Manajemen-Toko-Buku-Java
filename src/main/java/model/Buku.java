package model;

public class Buku {
    private int idBuku;
    private String kodeBuku;
    private String judul;
    private String penulis;
    private Integer idPenerbit;
    private Integer idKategori;
    private String namaPenerbit;
    private String namaKategori;
    private double hargaBeli;
    private double hargaJual;
    private int stok;
    private String deskripsi;

    public Buku() {}

    public Buku(int idBuku, String kodeBuku, String judul, String penulis,
                Integer idPenerbit, Integer idKategori,
                String namaPenerbit, String namaKategori,
                double hargaBeli, double hargaJual, int stok) {
        this.idBuku = idBuku;
        this.kodeBuku = kodeBuku;
        this.judul = judul;
        this.penulis = penulis;
        this.idPenerbit = idPenerbit;
        this.idKategori = idKategori;
        this.namaPenerbit = namaPenerbit;
        this.namaKategori = namaKategori;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.stok = stok;
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

    public String getPenulis() {
        return penulis;
    }

    public void setPenulis(String penulis) {
        this.penulis = penulis;
    }

    public Integer getIdPenerbit() {
        return idPenerbit;
    }

    public void setIdPenerbit(Integer idPenerbit) {
        this.idPenerbit = idPenerbit;
    }

    public Integer getIdKategori() {
        return idKategori;
    }

    public void setIdKategori(Integer idKategori) {
        this.idKategori = idKategori;
    }

    public String getNamaPenerbit() {
        return namaPenerbit;
    }

    public void setNamaPenerbit(String namaPenerbit) {
        this.namaPenerbit = namaPenerbit;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public void setNamaKategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }

    public double getHargaBeli() {
        return hargaBeli;
    }

    public void setHargaBeli(double hargaBeli) {
        this.hargaBeli = hargaBeli;
    }

    public double getHargaJual() {
        return hargaJual;
    }

    public void setHargaJual(double hargaJual) {
        this.hargaJual = hargaJual;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
}
