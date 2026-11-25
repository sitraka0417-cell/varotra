package mg.varotra;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/** Rapports : masonkarena, ventes, trosa et tombombarotra par jour, par mois ou par année. */
public class TatitraFragment extends Fragment {

    private static final int JOUR = 0, MOIS = 1, AN = 2;

    /** Période affichée (gardée quand on change d'onglet). */
    private static int mode = MOIS;
    private static long repere = System.currentTimeMillis();

    private View racine;
    private TextView segJour, segMois, segAn, periode, detailsTitre, graphiqueVide;
    private BarChartView graphique;
    private LinearLayout details;
    private String libellePeriode = "";
    private Bilan bilanTotal = new Bilan();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle b) {
        racine = inflater.inflate(R.layout.fragment_tatitra, container, false);
        T.habiller(racine);
        segJour = racine.findViewById(R.id.seg_jour);
        segMois = racine.findViewById(R.id.seg_mois);
        segAn = racine.findViewById(R.id.seg_an);
        periode = racine.findViewById(R.id.periode);
        detailsTitre = racine.findViewById(R.id.details_titre);
        graphiqueVide = racine.findViewById(R.id.graphique_vide);
        graphique = racine.findViewById(R.id.graphique);
        details = racine.findViewById(R.id.details);

        segJour.setOnClickListener(v -> changerMode(JOUR));
        segMois.setOnClickListener(v -> changerMode(MOIS));
        segAn.setOnClickListener(v -> changerMode(AN));
        racine.findViewById(R.id.btn_precedent).setOnClickListener(v -> deplacer(-1));
        racine.findViewById(R.id.btn_suivant).setOnClickListener(v -> deplacer(1));
        racine.findViewById(R.id.btn_partager).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, Utils.texteBilan(libellePeriode, bilanTotal));
            startActivity(Intent.createChooser(i, T.t("partage_choix")));
        });
        rafraichir();
        return racine;
    }

    @Override
    public void onResume() {
        super.onResume();
        rafraichir();
    }

    private void changerMode(int m) {
        mode = m;
        rafraichir();
    }

    private void deplacer(int sens) {
        if (mode == JOUR) repere = Utils.ajouterJours(repere, sens);
        else if (mode == MOIS) repere = Utils.ajouterMois(repere, sens);
        else repere = Utils.ajouterAns(repere, sens);
        rafraichir();
    }

    private void style(TextView t, boolean actif) {
        t.setBackgroundResource(actif ? R.drawable.bg_seg_on : R.drawable.bg_seg_off);
        t.setTextColor(actif ? ContextCompat.getColor(requireContext(), R.color.indigo) : 0xFFFFFFFF);
    }

    private static List<Lot> filtrer(List<Lot> tous, long debut, long fin) {
        List<Lot> res = new ArrayList<>();
        for (Lot l : tous) if (l.date >= debut && l.date < fin) res.add(l);
        return res;
    }

    private void rafraichir() {
        if (racine == null) return;
        style(segJour, mode == JOUR);
        style(segMois, mode == MOIS);
        style(segAn, mode == AN);

        long debut, fin;
        if (mode == JOUR) {
            debut = Utils.debutJour(repere);
            fin = Utils.ajouterJours(debut, 1);
            libellePeriode = Utils.dateLongue(debut);
        } else if (mode == MOIS) {
            debut = Utils.debutMois(repere);
            fin = Utils.ajouterMois(debut, 1);
            libellePeriode = Utils.moisAn(debut);
        } else {
            debut = Utils.debutAn(repere);
            fin = Utils.ajouterAns(debut, 1);
            libellePeriode = Utils.annee(debut);
        }
        periode.setText(libellePeriode);

        List<Lot> lots = Store.entre(requireContext(), debut, fin);
        bilanTotal = Bilan.de(lots);
        Kpi.afficher(racine.findViewById(R.id.kpi), bilanTotal);

        // Lignes de détail + données du graphique
        List<String> labels = new ArrayList<>();
        List<Double> valeurs = new ArrayList<>();
        details.removeAllViews();

        if (mode == JOUR) {
            detailsTitre.setText(T.t("tatitra_par_produit"));
            for (Lot l : lots) {
                Bilan x = Bilan.de(java.util.Collections.singletonList(l));
                ajouterLigne(l.titre(), x);
                labels.add(l.titre().length() > 7 ? l.titre().substring(0, 7) : l.titre());
                valeurs.add(x.benefice());
            }
        } else if (mode == MOIS) {
            detailsTitre.setText(T.t("tatitra_par_jour"));
            long j = debut;
            while (j < fin) {
                List<Lot> duJour = filtrer(lots, j, Utils.ajouterJours(j, 1));
                Bilan x = Bilan.de(duJour);
                labels.add(String.valueOf(Utils.jourDuMois(j)));
                valeurs.add(x.benefice());
                if (!duJour.isEmpty()) ajouterLigne(Utils.dateLongue(j), x);
                j = Utils.ajouterJours(j, 1);
            }
        } else {
            detailsTitre.setText(T.t("tatitra_par_mois"));
            long m = debut;
            while (m < fin) {
                long suivant = Utils.ajouterMois(m, 1);
                List<Lot> duMois = filtrer(lots, m, suivant);
                Bilan x = Bilan.de(duMois);
                labels.add(Utils.nomMoisCourt(m));
                valeurs.add(x.benefice());
                if (!duMois.isEmpty()) ajouterLigne(Utils.moisAn(m), x);
                m = suivant;
            }
        }

        double[] vals = new double[valeurs.size()];
        for (int i = 0; i < vals.length; i++) vals[i] = valeurs.get(i);
        graphique.setDonnees(labels.toArray(new String[0]), vals);

        boolean vide = lots.isEmpty();
        graphique.setVisibility(vide ? View.GONE : View.VISIBLE);
        graphiqueVide.setVisibility(vide ? View.VISIBLE : View.GONE);
        if (vide) {
            TextView t = new TextView(requireContext());
            t.setText(T.t("tatitra_vide"));
            t.setTextColor(ContextCompat.getColor(requireContext(), R.color.texte2));
            t.setPadding(0, 12, 0, 4);
            details.addView(t);
        }
    }

    private void ajouterLigne(String label, Bilan b) {
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_ligne_bilan, details, false);
        ((TextView) row.findViewById(R.id.b_label)).setText(label);
        ((TextView) row.findViewById(R.id.b_detail)).setText(
                T.t("tatitra_ligne_detail", Utils.argent(b.masonkarena), Utils.argent(b.ventes), Utils.argent(b.trosa)));
        TextView val = row.findViewById(R.id.b_valeur);
        val.setText(Utils.argent(b.benefice()));
        val.setTextColor(ContextCompat.getColor(requireContext(), b.benefice() >= 0 ? R.color.vert : R.color.rouge));
        details.addView(row);
    }
}
