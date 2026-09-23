package model;

public class Member {
    private int idMember;
    private String kodeMember;
    private String nama;
    private String alamat;
    private String noTelp;

    public Member() {}

    public Member(int idMember, String kodeMember, String nama, String alamat, String noTelp) {
        this.idMember = idMember;
        this.kodeMember = kodeMember;
        this.nama = nama;
        this.alamat = alamat;
        this.noTelp = noTelp;
    }

    public int getIdMember() {
        return idMember;
    }

    public void setIdMember(int idMember) {
        this.idMember = idMember;
    }

    public String getKodeMember() {
        return kodeMember;
    }

    public void setKodeMember(String kodeMember) {
        this.kodeMember = kodeMember;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
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
