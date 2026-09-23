package report;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ReportCompilationTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "/reports/lap_data_buku.jrxml",
        "/reports/lap_penjualan.jrxml",
        "/reports/lap_pembelian.jrxml",
        "/reports/lap_stok.jrxml",
        "/reports/lap_pendapatan.jrxml",
        "/reports/lap_terlaris.jrxml",
        "/reports/lap_retur.jrxml",
        "/reports/lap_laba_kotor.jrxml",
        "/reports/lap_supplier.jrxml"
    })
    void testCompileAndFillAllReports(String jrxmlPath) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(jrxmlPath)) {
            Assertions.assertNotNull(in, "Template report tidak ditemukan di classpath: " + jrxmlPath);
            JasperReport report = JasperCompileManager.compileReport(in);
            Assertions.assertNotNull(report, "Hasil kompilasi report bernilai null: " + jrxmlPath);

            Map<String, Object> params = new HashMap<>();
            params.put("APP_NAME", "Toko Buku Almira");
            params.put("PERIODE", "01 Januari 2026 s.d. 31 Januari 2026");
            params.put("KOTA", "Bandung");
            params.put("TGL_CETAK", "23 September 2026");
            params.put("PETUGAS", "Admin Test");

            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(Collections.emptyList());
            JasperPrint print = JasperFillManager.fillReport(report, params, ds);
            Assertions.assertNotNull(print, "JasperPrint bernilai null: " + jrxmlPath);

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(print);
            Assertions.assertTrue(pdfBytes.length > 0, "PDF bytes kosong untuk: " + jrxmlPath);
        }
    }
}
