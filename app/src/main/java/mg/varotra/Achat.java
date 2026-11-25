package mg.varotra;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/** Un fangaro (ingrédient) acheté pour fabriquer un produit. */
public class Achat {
    public String id = UUID.randomUUID().toString();
    public String nom = "";
    public double qte = 1;
    public String unite = "";
    public double prix = 0; // prix unitaire

    public double total() {
        return qte * prix;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("nom", nom);
        o.put("qte", qte);
        o.put("unite", unite);
        o.put("prix", prix);
        return o;
    }

    public static Achat fromJson(JSONObject o) {
        Achat a = new Achat();
        a.id = o.optString("id", a.id);
        a.nom = o.optString("nom", "");
        a.qte = o.optDouble("qte", 1);
        a.unite = o.optString("unite", "");
        a.prix = o.optDouble("prix", 0);
        return a;
    }
}
