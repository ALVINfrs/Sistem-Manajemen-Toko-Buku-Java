package model;

public class Penerbit {
    private int idPenerbit;
    private String namaPenerbit;
    private String alamat;
    private String noTelp;

    public Penerbit() {}

    public Penerbit(int idPenerbit, String namaPenerbit, String alamat, String noTelp) {
        this.idPenerbit = idPenerbit;
        this.namaPenerbit = namaPenerbit;
        this.alamat = alamat;
        this.noTelp = noTelp;
    }

    public int getIdPenerbit() {
        return idPenerbit;
    }

    public void setIdPenerbit(int idPenerbit) {
        this.idPenerbit = idPenerbit;
    }

    public String getNamaPenerbit() {
        return namaPenerbit;
    }

    public void setNamaPenerbit(String namaPenerbit) {
        this.namaPenerbit = namaPenerbit;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getNoTelp() {
        return noTelp;
    }

    public void setNoTelp(String noTelp) {
        this.noTelp = noTelp;
    }
}
