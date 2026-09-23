package report;

import java.io.File;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.view.JasperViewer;

public class ReportHelper {
    public static void show(String jrxmlPath, List<?> data, Map<String, Object> params) throws JRException {
        JasperPrint jp = fill(jrxmlPath, data, params);
        JasperViewer viewer = new JasperViewer(jp, false);
        viewer.setTitle("Laporan - " + jrxmlPath);
        viewer.setVisible(true);
    }

    public static void exportPdf(String jrxmlPath, List<?> data, Map<String, Object> params, File out) throws JRException {
        JasperPrint jp = fill(jrxmlPath, data, params);
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jp));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        exporter.exportReport();
    }

    private static JasperPrint fill(String jrxmlPath, List<?> data, Map<String, Object> params) throws JRException {
        Map<String, Object> p = (params != null) ? new java.util.HashMap<>(params) : new java.util.HashMap<>();
        if (!p.containsKey("APP_NAME")) {
            p.put("APP_NAME", util.AppConfig.APP_NAME);
        }
        if (!p.containsKey("KOTA")) {
            p.put("KOTA", "Jakarta");
        }
        if (!p.containsKey("TGL_CETAK")) {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", new java.util.Locale("id", "ID"));
            p.put("TGL_CETAK", java.time.LocalDate.now().format(fmt));
        }
        if (!p.containsKey("PETUGAS")) {
            String nama = (util.Sesi.userLogin != null && util.Sesi.userLogin.getNamaLengkap() != null)
                    ? util.Sesi.userLogin.getNamaLengkap() : "Petugas Kasir / Admin";
            p.put("PETUGAS", nama);
        }
        JasperReport jr = JasperCompileManager.compileReport(ReportHelper.class.getResourceAsStream(jrxmlPath));
        return JasperFillManager.fillReport(jr, p, new JRBeanCollectionDataSource(data));
    }
}
