package util;
import model.User;
public class Sesi {
    public static User userLogin;
    public static String role;
    public static void clear() { userLogin = null; role = null; }
    public static boolean isAdmin() {
        return role != null && ("admin".equalsIgnoreCase(role) || "administrator".equalsIgnoreCase(role));
    }
}
