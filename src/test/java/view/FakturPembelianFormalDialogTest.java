package view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import model.DetailPembelian;
import model.Pembelian;
import model.Supplier;
import org.junit.jupiter.api.Test;

class FakturPembelianFormalDialogTest {

    private DetailPembelian detail(String kode, String judul, int qty, double harga) {
        DetailPembelian d = new DetailPembelian();
        d.setKodeBuku(kode);
        d.setJudul(judul);
        d.setQty(qty);
        d.setHargaBeli(harga);
        d.setSubtotal(qty * harga);
        return d;
    }

    @Test
    void buildReportParamsMengisiHeaderTotalDanToko() {
        Pembelian h = new Pembelian();
        h.setNoFaktur("FB-20261111-0001");
        h.setTanggal(LocalDateTime.of(2026, 11, 11, 9, 30));
        h.setNamaSupplier("PT Fallback");
        h.setTotal(30000);

        Supplier s = new Supplier(1, "PT Supplier", "Jl. Supplier 1", "021-123");
        List<DetailPembelian> items = Arrays.asList(
                detail("BK-001", "Buku A", 2, 10000),
                detail("BK-002", "Buku B", 1, 10000));

        Map<String, Object> p = FakturPembelianFormalDialog
                .buildReportParams(h, s, items, "Admin Test");

        assertEquals("FB-20261111-0001", p.get("NO_FAKTUR"));
        assertEquals("11 November 2026 09:30", p.get("TANGGAL"));
        assertEquals("PT Supplier", p.get("SUPPLIER"));
        assertEquals("Jl. Supplier 1", p.get("ALAMAT_SUPPLIER"));
        assertEquals("021-123", p.get("TELP_SUPPLIER"));
        assertEquals("Admin Test", p.get("PENERIMA"));
        assertEquals(30000.0, (Double) p.get("TOTAL"), 0.001);
        assertEquals(3, p.get("TOTAL_QTY"));
        assertEquals(2, p.get("TOTAL_JENIS"));
        assertEquals("Bekasi", p.get("KOTA"));
        assertEquals(LocalDate.now().format(java.time.format.DateTimeFormatter
                .ofPattern("dd MMMM yyyy", new java.util.Locale("id", "ID"))),
                p.get("TGL_CETAK"));
    }

    @Test
    void buildReportParamsMemakaiFallbackSupplierDanTotalDetail() {
        Pembelian h = new Pembelian();
        h.setNoFaktur("FB-2");
        h.setNamaSupplier("Supplier Header");
        h.setTotal(0);

        List<DetailPembelian> items = Collections.singletonList(
                detail("BK-003", "Buku C", 2, 15000));

        Map<String, Object> p = FakturPembelianFormalDialog
                .buildReportParams(h, null, items, null);

        assertEquals("Supplier Header", p.get("SUPPLIER"));
        assertEquals("-", p.get("ALAMAT_SUPPLIER"));
        assertEquals(30000.0, (Double) p.get("TOTAL"), 0.001);
        assertEquals("-", p.get("PENERIMA"));
    }

    @Test
    void buildItemRowsMenomoriDanMemformatRupiah() {
        Object[][] rows = FakturPembelianFormalDialog.buildItemRows(Arrays.asList(
                detail("BK-001", "Buku A", 2, 10000),
                detail("BK-002", "Buku B", 1, 5000)));

        assertEquals(2, rows.length);
        assertArrayEquals(new Object[]{1, "BK-001", "Buku A", 2,
                "Rp 10.000", "Rp 20.000"}, rows[0]);
        assertArrayEquals(new Object[]{2, "BK-002", "Buku B", 1,
                "Rp 5.000", "Rp 5.000"}, rows[1]);
    }

    @Test
    void formatTanggalDanSafeMenanganiNull() {
        assertEquals("-", FakturPembelianFormalDialog.formatTanggal(null));
        assertEquals("-", FakturPembelianFormalDialog.safe(null));
        assertEquals("-", FakturPembelianFormalDialog.safe("  "));
        assertEquals("Admin", FakturPembelianFormalDialog.safe(" Admin "));
        assertEquals("11 November 2026 09:05",
                FakturPembelianFormalDialog.formatTanggal(
                        LocalDateTime.of(2026, 11, 11, 9, 5)));
    }
}
