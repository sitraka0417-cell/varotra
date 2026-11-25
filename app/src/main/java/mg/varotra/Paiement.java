package mg.varotra;

import org.json.JSONException;
import org.json.JSONObject;

/** Un versement d'argent reçu pour une vente (date + montant). */
public class Paiement {
    public long date;
    public double montant;

    public Paiement(long date, double montant) {
        this.date = date;
        this.montant = montant;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("date", date);
        o.put("montant", montant);
        return o;
    }

    public static Paiement fromJson(JSONObject o) {
        return new Paiement(o.optLong("date", System.currentTimeMillis()), o.optDouble("montant", 0));
    }
}
