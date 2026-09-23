package util;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
public class HashUtil {
    public static String sha256(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : h) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException("SHA-256 gagal", e); }
    }
}
