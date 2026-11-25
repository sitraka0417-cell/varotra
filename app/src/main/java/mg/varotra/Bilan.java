package mg.varotra;

import java.util.List;

/** Totaux d'un ensemble de lots (jour, mois, année...). */
public class Bilan {
    public double masonkarena;  // argent dépensé en achats (fond)
    public double ventes;       // total des ventes (payées ou non)
    public double encaisse;     // argent déjà reçu
    public double trosa;        // dettes des clients
    public double nbVendus;
    public int nbLots, nbVentes;

    /** Tombombarotra = total des ventes - achats. */
    public double benefice() {
        return ventes - masonkarena;
    }

    /** Bénéfice réellement en main (argent reçu - achats). */
    public double beneficeEnMain() {
        return encaisse - masonkarena;
    }

    public double marge() {
        return ventes > 0 ? benefice() * 100.0 / ventes : 0;
    }

    public static Bilan de(List<Lot> lots) {
        Bilan b = new Bilan();
        for (Lot l : lots) {
            b.nbLots++;
            b.masonkarena += l.masonkarena();
            for (Vente v : l.ventes) {
                b.nbVentes++;
                b.ventes += v.total;
                b.encaisse += Math.min(v.paye, v.total);
                b.trosa += v.reste();
                b.nbVendus += v.qte;
            }
        }
        return b;
    }
}
