package mg.varotra;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Tous les textes de l'application viennent du fichier assets/teny.txt (format  cle = texte).
 * Dans les layouts XML, un texte est lié avec  android:tag="t:cle".
 */
public class T {
    private static final Map<String, String> M = new HashMap<>();

    public static synchronized void charger(Context c) {
        M.clear();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(c.getAssets().open("teny.txt"), StandardCharsets.UTF_8))) {
            String l;
            while ((l = r.readLine()) != null) {
                if (l.startsWith("\uFEFF")) l = l.substring(1);
                String s = l.trim();
                if (s.isEmpty() || s.startsWith("#")) continue;
                int i = s.indexOf('=');
                if (i <= 0) continue;
                String k = s.substring(0, i).trim();
                String v = s.substring(i + 1).trim().replace("\\n", "\n");
                M.put(k, v);
            }
        } catch (Exception ignored) {
        }
    }

    public static String t(String cle) {
        String v = M.get(cle);
        return v != null ? v : cle;
    }

    /** Remplace {0}, {1}... par les valeurs données. */
    public static String t(String cle, Object... args) {
        String v = t(cle);
        for (int i = 0; i < args.length; i++) {
            v = v.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return v;
    }

    /** Liste séparée par des virgules (ex. unités, noms des jours). */
    public static String[] liste(String cle) {
        String v = t(cle).trim();
        if (v.isEmpty()) return new String[0];
        return v.split("\\s*,\\s*");
    }

    /** Applique les textes (tag "t:cle") à toute la hiérarchie de vues. */
    public static void habiller(View v) {
        Object tag = v.getTag();
        if (tag instanceof String && ((String) tag).startsWith("t:")) {
            String txt = t(((String) tag).substring(2));
            if (v instanceof TextInputLayout) {
                ((TextInputLayout) v).setHint(txt);
            } else if (v instanceof TextView) {
                ((TextView) v).setText(txt);
            } else {
                v.setContentDescription(txt);
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) habiller(g.getChildAt(i));
        }
    }
}
