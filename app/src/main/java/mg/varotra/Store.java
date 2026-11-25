package mg.varotra;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Stockage local (fichier JSON dans la mémoire privée de l'application). */
public class Store {
    private static final String FICHIER = "donnees.json";
    private static List<Lot> lots;

    public static synchronized List<Lot> get(Context c) {
        if (lots == null) {
            lots = new ArrayList<>();
            File f = new File(c.getApplicationContext().getFilesDir(), FICHIER);
            if (f.exists()) {
                try (InputStream in = new FileInputStream(f)) {
                    String json = lire(in);
                    lots.addAll(depuisJson(json));
                } catch (Exception ignored) {
                }
            }
        }
        return lots;
    }

    public static synchronized void sauver(Context c) {
        try {
            File dir = c.getApplicationContext().getFilesDir();
            File tmp = new File(dir, FICHIER + ".tmp");
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                out.write(exporter(c).getBytes(StandardCharsets.UTF_8));
                out.getFD().sync();
            }
            File f = new File(dir, FICHIER);
            if (!tmp.renameTo(f)) {
                try (FileOutputStream out = new FileOutputStream(f)) {
                    out.write(exporter(c).getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static synchronized String exporter(Context c) {
        try {
            JSONObject o = new JSONObject();
            o.put("app", "varotra");
            o.put("version", 1);
            JSONArray arr = new JSONArray();
            for (Lot l : get(c)) arr.put(l.toJson());
            o.put("lots", arr);
            return o.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    /** Remplace toutes les données par celles de la sauvegarde. Retourne faux si le fichier est invalide. */
    public static synchronized boolean importer(Context c, String json) {
        try {
            List<Lot> nouveaux = depuisJson(json);
            List<Lot> l = get(c);
            l.clear();
            l.addAll(nouveaux);
            sauver(c);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static synchronized void vider(Context c) {
        get(c).clear();
        sauver(c);
    }

    private static List<Lot> depuisJson(String json) throws JSONException {
        JSONObject o = new JSONObject(json);
        if (!o.has("lots")) throw new JSONException("invalide");
        JSONArray arr = o.getJSONArray("lots");
        List<Lot> res = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) res.add(Lot.fromJson(arr.getJSONObject(i)));
        return res;
    }

    public static String lire(InputStream in) throws java.io.IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
        return new String(bo.toByteArray(), StandardCharsets.UTF_8);
    }

    public static Lot parId(Context c, String id) {
        if (id == null) return null;
        for (Lot l : get(c)) if (l.id.equals(id)) return l;
        return null;
    }

    /** Lots dont la date est dans [debut, fin[ , les plus récents d'abord. */
    public static List<Lot> entre(Context c, long debut, long fin) {
        List<Lot> res = new ArrayList<>();
        for (Lot l : get(c)) if (l.date >= debut && l.date < fin) res.add(l);
        return res;
    }

    /** Dernier lot portant ce nom (sans tenir compte des majuscules) ; nom vide = dernier lot tout court. */
    public static Lot dernierLot(Context c, String nom) {
        Lot best = null;
        String n = nom == null ? "" : nom.trim();
        for (Lot l : get(c)) {
            if (!n.isEmpty() && !l.nom.trim().equalsIgnoreCase(n)) continue;
            if (l.achats.isEmpty()) continue;
            if (best == null || l.date > best.date) best = l;
        }
        return best;
    }

    /** Ingrédients déjà utilisés (dernier prix connu gagne), clé = nom en minuscules. */
    public static Map<String, Achat> connusAchats(Context c) {
        List<Lot> copie = new ArrayList<>(get(c));
        Collections.sort(copie, new Comparator<Lot>() {
            @Override
            public int compare(Lot a, Lot b) {
                return Long.compare(a.date, b.date);
            }
        });
        Map<String, Achat> m = new LinkedHashMap<>();
        for (Lot l : copie) {
            for (Achat a : l.achats) {
                if (!a.nom.trim().isEmpty()) m.put(a.nom.trim().toLowerCase(), a);
            }
        }
        return m;
    }

    public static List<String> nomsClients(Context c) {
        List<String> res = new ArrayList<>();
        for (Lot l : get(c)) for (Vente v : l.ventes) {
            String n = v.client == null ? "" : v.client.trim();
            if (n.isEmpty()) continue;
            boolean existe = false;
            for (String x : res) if (x.equalsIgnoreCase(n)) { existe = true; break; }
            if (!existe) res.add(n);
        }
        return res;
    }

    public static List<String> nomsProduits(Context c) {
        List<String> res = new ArrayList<>();
        for (Lot l : get(c)) {
            String n = l.nom.trim();
            if (n.isEmpty()) continue;
            boolean existe = false;
            for (String x : res) if (x.equalsIgnoreCase(n)) { existe = true; break; }
            if (!existe) res.add(n);
        }
        return res;
    }
}
