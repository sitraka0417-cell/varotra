package mg.varotra;

import java.util.Calendar;
import java.util.Locale;

public class Utils {

    // ----- Nombres -----
    public static String argent(double v) {
        if (Math.abs(v) < 0.005) v = 0;
        String s;
        if (Math.abs(v - Math.rint(v)) < 0.005) {
            s = String.format(Locale.FRANCE, "%,.0f", Math.rint(v));
        } else {
            s = String.format(Locale.FRANCE, "%,.2f", v);
        }
        return s.replace('\u202f', ' ').replace('\u00a0', ' ') + " " + T.t("devise");
    }

    /** 2 -> "2" ; 0.5 -> "0,5" */
    public static String qte(double q) {
        if (Math.abs(q - Math.rint(q)) < 0.0005) return String.valueOf((long) Math.rint(q));
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.###",
                new java.text.DecimalFormatSymbols(Locale.FRANCE));
        return df.format(q);
    }

    /** Valeur à remettre dans un champ de saisie (sans séparateur de milliers). */
    public static String nombre(double v) {
        if (Math.abs(v - Math.rint(v)) < 0.005) return String.valueOf((long) Math.rint(v));
        return String.valueOf(Math.round(v * 100) / 100.0);
    }

    /** Lit un nombre saisi ; retourne NaN si vide ou invalide. */
    public static double lire(CharSequence cs) {
        if (cs == null) return Double.NaN;
        String s = cs.toString().trim().replace(" ", "").replace(',', '.');
        if (s.isEmpty()) return Double.NaN;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    public static double nz(double d) {
        return Double.isNaN(d) ? 0 : d;
    }

    // ----- Dates (noms malgaches dans teny.txt) -----
    private static Calendar cal(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        return c;
    }

    public static long debutJour(long ms) {
        Calendar c = cal(ms);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long ajouterJours(long ms, int n) {
        Calendar c = cal(ms);
        c.add(Calendar.DAY_OF_MONTH, n);
        return debutJour(c.getTimeInMillis());
    }

    public static long debutMois(long ms) {
        Calendar c = cal(debutJour(ms));
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTimeInMillis();
    }

    public static long ajouterMois(long ms, int n) {
        Calendar c = cal(debutMois(ms));
        c.add(Calendar.MONTH, n);
        return c.getTimeInMillis();
    }

    public static long debutAn(long ms) {
        Calendar c = cal(debutJour(ms));
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTimeInMillis();
    }

    public static long ajouterAns(long ms, int n) {
        Calendar c = cal(debutAn(ms));
        c.add(Calendar.YEAR, n);
        return c.getTimeInMillis();
    }

    public static boolean memeJour(long a, long b) {
        return debutJour(a) == debutJour(b);
    }

    private static String nomMois(int index0) {
        String[] m = T.liste("mois");
        return index0 >= 0 && index0 < m.length ? m[index0] : String.valueOf(index0 + 1);
    }

    /** « Alatsinainy 25 Novambra 2026 » */
    public static String dateLongue(long ms) {
        Calendar c = cal(ms);
        String[] j = T.liste("jours");
        int idx = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7; // lundi = 0
        String jour = idx < j.length ? j[idx] + " " : "";
        return jour + c.get(Calendar.DAY_OF_MONTH) + " " + nomMois(c.get(Calendar.MONTH)) + " " + c.get(Calendar.YEAR);
    }

    /** « 25 Novambra » */
    public static String jourMois(long ms) {
        Calendar c = cal(ms);
        return c.get(Calendar.DAY_OF_MONTH) + " " + nomMois(c.get(Calendar.MONTH));
    }

    /** « Novambra 2026 » */
    public static String moisAn(long ms) {
        Calendar c = cal(ms);
        return nomMois(c.get(Calendar.MONTH)) + " " + c.get(Calendar.YEAR);
    }

    public static String nomMoisCourt(long ms) {
        String m = nomMois(cal(ms).get(Calendar.MONTH));
        return m.length() > 3 ? m.substring(0, 3) : m;
    }

    public static int jourDuMois(long ms) {
        return cal(ms).get(Calendar.DAY_OF_MONTH);
    }

    public static String annee(long ms) {
        return String.valueOf(cal(ms).get(Calendar.YEAR));
    }

    // ----- Textes à partager -----
    public static String texteBilan(String periode, Bilan b) {
        StringBuilder sb = new StringBuilder();
        sb.append(T.t("partage_titre", periode)).append("\n\n");
        sb.append(T.t("kpi_mason")).append(" : ").append(argent(b.masonkarena)).append("\n");
        sb.append(T.t("kpi_ventes")).append(" : ").append(argent(b.ventes)).append("\n");
        sb.append(T.t("kpi_encaisse")).append(" : ").append(argent(b.encaisse)).append("\n");
        sb.append(T.t("kpi_trosa")).append(" : ").append(argent(b.trosa)).append("\n");
        sb.append(T.t("kpi_benef")).append(" : ").append(argent(b.benefice()));
        return sb.toString();
    }

    public static String texteLot(Lot l) {
        StringBuilder sb = new StringBuilder();
        sb.append(l.titre()).append(" — ").append(dateLongue(l.date)).append("\n\n");
        sb.append(T.t("onglet_achats")).append(" :\n");
        for (Achat a : l.achats) {
            sb.append("• ").append(a.nom).append(" ").append(qte(a.qte)).append(" ").append(a.unite)
                    .append(" × ").append(argent(a.prix)).append(" = ").append(argent(a.total())).append("\n");
        }
        sb.append("\n").append(texteBilan(l.titre(), Bilan.de(java.util.Collections.singletonList(l))));
        return sb.toString();
    }
}
