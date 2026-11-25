package mg.varotra;

import org.json.JSONException;
import org.json.JSONObject;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Une vente à un mpividy (client), éventuellement à crédit (trosa). */
public class Vente {
    public static final int PAYE = 0, TROSA = 1, PARTIEL = 2;

    public String id = UUID.randomUUID().toString();
    public long heure = System.currentTimeMillis();
    public String client = "";
    public double qte = 1;
    public double pu = 0;       // prix unitaire
    public double total = 0;    // total final (modifiable à la main : remise)
    public boolean manuel = false;
    public double paye = 0;     // argent déjà reçu (= somme des paiements)
    public List<Paiement> paiements = new ArrayList<>();

    private void recalculer() {
        double t = 0;
        for (Paiement p : paiements) t += p.montant;
        paye = t;
    }

    /** Ajoute un versement (plafonné au reste à payer). Retourne le montant réellement enregistré. */
    public double ajouterPaiement(double montant, long date) {
        double m = Math.min(montant, reste());
        if (m <= 0.0049) return 0;
        paiements.add(new Paiement(date, m));
        recalculer();
        return m;
    }

    /** Ajuste le total reçu (édition de la vente) en gardant l'historique cohérent. */
    public void definirPaye(double nouveau, long date) {
        recalculer();
        double diff = nouveau - paye;
        if (diff > 0.0049) {
            paiements.add(new Paiement(date, diff));
        } else if (diff < -0.0049) {
            double reste = -diff;
            while (reste > 0.0049 && !paiements.isEmpty()) {
                Paiement dernier = paiements.get(paiements.size() - 1);
                if (dernier.montant <= reste + 0.0049) {
                    reste -= dernier.montant;
                    paiements.remove(paiements.size() - 1);
                } else {
                    dernier.montant -= reste;
                    reste = 0;
                }
            }
        }
        recalculer();
    }

    public double reste() {
        return Math.max(0, total - paye);
    }

    public double remise() {
        return Math.max(0, qte * pu - total);
    }

    public int statut() {
        if (reste() < 0.005) return PAYE;
        if (paye < 0.005) return TROSA;
        return PARTIEL;
    }

    public String nomClient() {
        return client == null || client.trim().isEmpty() ? T.t("client_anonyme") : client.trim();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("heure", heure);
        o.put("client", client);
        o.put("qte", qte);
        o.put("pu", pu);
        o.put("total", total);
        o.put("manuel", manuel);
        o.put("paye", paye);
        JSONArray a = new JSONArray();
        for (Paiement p : paiements) a.put(p.toJson());
        o.put("paiements", a);
        return o;
    }

    public static Vente fromJson(JSONObject o) {
        Vente v = new Vente();
        v.id = o.optString("id", v.id);
        v.heure = o.optLong("heure", v.heure);
        v.client = o.optString("client", "");
        v.qte = o.optDouble("qte", 1);
        v.pu = o.optDouble("pu", 0);
        v.total = o.optDouble("total", v.qte * v.pu);
        v.manuel = o.optBoolean("manuel", false);
        JSONArray a = o.optJSONArray("paiements");
        if (a != null) {
            for (int i = 0; i < a.length(); i++) {
                JSONObject x = a.optJSONObject(i);
                if (x != null) v.paiements.add(Paiement.fromJson(x));
            }
            v.recalculer();
        } else {
            // anciennes données : un seul versement à la date de la vente
            double pa = o.optDouble("paye", v.total);
            if (pa > 0.0049) v.paiements.add(new Paiement(v.heure, pa));
            v.recalculer();
        }
        return v;
    }
}
