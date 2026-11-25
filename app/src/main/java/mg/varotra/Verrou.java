package mg.varotra;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;

/** Code PIN (4 chiffres) + empreinte digitale pour protéger l'application. */
public class Verrou {
    private static final String PREFS = "verrou";
    /** Délai après lequel l'app se reverrouille quand on la quitte. */
    public static final long DELAI_MS = 30_000;

    /** État de la session (perdu quand le processus est tué => reverrouillage). */
    public static boolean deverrouille = false;
    public static long dernierArret = 0;

    private static SharedPreferences p(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean actif(Context c) {
        return p(c).contains("hash");
    }

    public static boolean bioActive(Context c) {
        return p(c).getBoolean("bio", false);
    }

    public static void setBio(Context c, boolean on) {
        p(c).edit().putBoolean("bio", on).apply();
    }

    private static String hash(String sel, String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest((sel + ":" + pin).getBytes("UTF-8"));
            return Base64.encodeToString(d, Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }

    public static void definir(Context c, String pin) {
        byte[] b = new byte[12];
        new SecureRandom().nextBytes(b);
        String sel = Base64.encodeToString(b, Base64.NO_WRAP);
        p(c).edit().putString("sel", sel).putString("hash", hash(sel, pin)).apply();
        deverrouille = true;
        dernierArret = System.currentTimeMillis();
    }

    public static boolean verifier(Context c, String pin) {
        SharedPreferences sp = p(c);
        String sel = sp.getString("sel", "");
        String h = sp.getString("hash", null);
        return h != null && h.equals(hash(sel, pin));
    }

    public static void retirer(Context c) {
        p(c).edit().clear().apply();
    }

    /** Faut-il afficher l'écran du code ? */
    public static boolean doitVerrouiller(Context c) {
        if (!actif(c)) return false;
        if (!deverrouille) return true;
        return System.currentTimeMillis() - dernierArret > DELAI_MS;
    }
}
