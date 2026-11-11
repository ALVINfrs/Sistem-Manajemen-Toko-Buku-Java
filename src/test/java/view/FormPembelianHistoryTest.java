package view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import model.LapPembelian;
import org.junit.jupiter.api.Test;

class FormPembelianHistoryTest {

    private LapPembelian lap(String no, String supplier, String kasir, double total) {
        return new LapPembelian(no, LocalDateTime.of(2026, 11, 11, 9, 30),
                supplier, kasir, total);
    }

    @Test
    void filterRiwayatMencocokkanNoFakturDanSupplierTanpaBedaHurufBesar() {
        List<LapPembelian> data = Arrays.asList(
                lap("FB-20261111-0001", "PT Alpha", "Admin", 10000),
                lap("FB-20261110-0002", "CV Beta", "Kasir", 20000));

        assertEquals(1, FormPembelian.filterRiwayat(data, "fb-20261111").size());
        assertEquals("FB-20261111-0001",
                FormPembelian.filterRiwayat(data, "fb-20261111").get(0).getNoFaktur());
        assertEquals(1, FormPembelian.filterRiwayat(data, "beta").size());
        assertEquals(2, FormPembelian.filterRiwayat(data, "  ").size());
        assertEquals(0, FormPembelian.filterRiwayat(data, "tidak-ada").size());
    }

    @Test
    void filterRiwayatAmanUntukListNull() {
        assertTrue(FormPembelian.filterRiwayat(null, "apa").isEmpty());
    }

    @Test
    void riwayatRowMenghasilkanLimaKolomTerformat() {
        Object[] row = FormPembelian.riwayatRow(
                lap("FB-20261111-0001", "PT Alpha", "Admin Almira", 1500000));

        assertArrayEquals(new Object[]{
                "FB-20261111-0001",
                "11-11-2026 09:30",
                "PT Alpha",
                "Admin Almira",
                "Rp 1.500.000"
        }, row);
    }
}
