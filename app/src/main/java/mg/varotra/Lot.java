package mg.varotra;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Un produit fabriqué et vendu pendant une journée (ex. « Sandwich »), avec ses achats et ses ventes. */
public class Lot {
    public String id = UUID.randomUUID().toString();
    public long date = Utils.debutJour(System.currentTimeMillis());
    public String nom = "";
    public double prixVente = 0;   // prix de vente d'une unité (0 = non défini)
    public double nbPrepares = 0;  // nombre d'unités fabriquées (0 = non renseigné)
    public List<Achat> achats = new ArrayList<>();
    public List<Vente> ventes = new ArrayList<>();

    public String titre() {
        return nom.trim().isEmpty() ? T.t("lot_sans_nom") : nom.trim();
    }

    public double masonkarena() {
        double t = 0;
        for (Achat a : achats) t += a.total();
        return t;
    }

    public double nbVendus() {
        double t = 0;
        for (Vente v : ventes) t += v.qte;
        return t;
    }

    /** Coût de revient d'une unité (si on connaît le nombre fabriqué, sinon sur le nombre vendu). */
    public double coutRevient() {
        double base = nbPrepares > 0 ? nbPrepares : nbVendus();
        return base > 0 ? masonkarena() / base : 0;
    }

    public Lot copie(long nouvelleDate) {
        Lot l = new Lot();
        l.date = Utils.debutJour(nouvelleDate);
        l.nom = nom;
        l.prixVente = prixVente;
        l.nbPrepares = nbPrepares;
        for (Achat a : achats) {
            Achat b = new Achat();
            b.nom = a.nom;
            b.qte = a.qte;
            b.unite = a.unite;
            b.prix = a.prix;
            l.achats.add(b);
        }
        return l;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("date", date);
        o.put("nom", nom);
        o.put("prixVente", prixVente);
        o.put("nbPrepares", nbPrepares);
        JSONArray a = new JSONArray();
        for (Achat x : achats) a.put(x.toJson());
        o.put("achats", a);
        JSONArray v = new JSONArray();
        for (Vente x : ventes) v.put(x.toJson());
        o.put("ventes", v);
        return o;
    }

    public static Lot fromJson(JSONObject o) {
        Lot l = new Lot();
        l.id = o.optString("id", l.id);
        l.date = Utils.debutJour(o.optLong("date", l.date));
        l.nom = o.optString("nom", "");
        l.prixVente = o.optDouble("prixVente", 0);
        l.nbPrepares = o.optDouble("nbPrepares", 0);
        JSONArray a = o.optJSONArray("achats");
        if (a != null) for (int i = 0; i < a.length(); i++) {
            JSONObject x = a.optJSONObject(i);
            if (x != null) l.achats.add(Achat.fromJson(x));
        }
        JSONArray v = o.optJSONArray("ventes");
        if (v != null) for (int i = 0; i < v.length(); i++) {
            JSONObject x = v.optJSONObject(i);
            if (x != null) l.ventes.add(Vente.fromJson(x));
        }
        return l;
    }
}
