package mg.varotra;

import android.view.View;
import android.widget.TextView;

/** Remplit le panneau de chiffres (view_kpi.xml). */
public class Kpi {

    public static void afficher(View r, Bilan b) {
        set(r, R.id.kpi_mason, Utils.argent(b.masonkarena));
        set(r, R.id.kpi_ventes, Utils.argent(b.ventes));
        TextView benef = r.findViewById(R.id.kpi_benef);
        benef.setText(Utils.argent(b.benefice()));
        benef.setTextColor(b.benefice() >= 0 ? 0xFF6EE7B7 : 0xFFFDA4AF);
        set(r, R.id.kpi_encaisse, Utils.argent(b.encaisse));
        set(r, R.id.kpi_trosa, Utils.argent(b.trosa));
        set(r, R.id.kpi_nb, Utils.qte(b.nbVendus));
        r.findViewById(R.id.kpi_revient).setVisibility(View.GONE);
    }

    public static void revient(View r, String texte) {
        TextView t = r.findViewById(R.id.kpi_revient);
        t.setText(texte);
        t.setVisibility(texte == null || texte.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private static void set(View r, int id, String s) {
        ((TextView) r.findViewById(id)).setText(s);
    }
}
