package mg.varotra;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Écran principal : le jour choisi, avec l'onglet Entana (produits) et l'onglet Fivarotana (toutes les ventes du jour). */
public class HomeFragment extends Fragment {

    /** Jour affiché (gardé quand on change d'onglet). */
    public static long jour = Utils.debutJour(System.currentTimeMillis());
    private static int onglet = 0;

    private View racine;
    private TextView btnDate, vide, videVentes, btnAujourdhui;
    private RecyclerView rvLots, rvVentes;
    private ExtendedFloatingActionButton fab;
    private LotAdapter adapter;
    private VenteAdapter venteAdapter;
    private final List<Lot> liste = new ArrayList<>();
    private final List<Vente> ventes = new ArrayList<>();
    private final Map<String, Lot> lotDeVente = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle b) {
        racine = inflater.inflate(R.layout.fragment_home, container, false);
        T.habiller(racine);
        btnDate = racine.findViewById(R.id.btn_date);
        vide = racine.findViewById(R.id.vide);
        videVentes = racine.findViewById(R.id.vide_ventes);
        btnAujourdhui = racine.findViewById(R.id.btn_aujourdhui);
        fab = racine.findViewById(R.id.fab);

        rvLots = racine.findViewById(R.id.liste);
        rvLots.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new LotAdapter(liste, lot -> {
            Intent i = new Intent(requireContext(), LotActivity.class);
            i.putExtra("id", lot.id);
            startActivity(i);
        });
        rvLots.setAdapter(adapter);

        rvVentes = racine.findViewById(R.id.liste_ventes);
        rvVentes.setLayoutManager(new LinearLayoutManager(requireContext()));
        venteAdapter = new VenteAdapter(ventes, new VenteAdapter.Ecouteur() {
            @Override
            public void editer(Vente v) {
                Lot l = lotDeVente.get(v.id);
                if (l != null) Dialogues.vente(requireContext(), l, null, v, HomeFragment.this::rafraichir);
            }

            @Override
            public void supprimer(final Vente v) {
                final Lot l = lotDeVente.get(v.id);
                if (l == null) return;
                final int index = l.ventes.indexOf(v);
                if (index < 0) return;
                l.ventes.remove(index);
                Store.sauver(requireContext());
                rafraichir();
                Snackbar.make(racine, T.t("snack_vente_supprimee"), Snackbar.LENGTH_LONG)
                        .setAction(T.t("btn_annuler_action"), x -> {
                            l.ventes.add(Math.min(index, l.ventes.size()), v);
                            Store.sauver(requireContext());
                            rafraichir();
                        }).setAnchorView(fab).show();
            }

            @Override
            public void payer(Vente v) {
                Dialogues.paiement(requireContext(), v, HomeFragment.this::rafraichir);
            }

            @Override
            public void historique(Vente v) {
                Dialogues.historique(requireContext(), v);
            }
        });
        rvVentes.setAdapter(venteAdapter);

        final TabLayout onglets = racine.findViewById(R.id.onglets);
        onglets.addTab(onglets.newTab().setText(T.t("tab_entana")));
        onglets.addTab(onglets.newTab().setText(T.t("tab_ventes_jour")));
        TabLayout.Tab t = onglets.getTabAt(onglet);
        if (t != null) t.select();
        onglets.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onglet = tab.getPosition();
                afficher();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        racine.findViewById(R.id.btn_precedent).setOnClickListener(v -> {
            jour = Utils.ajouterJours(jour, -1);
            rafraichir();
        });
        racine.findViewById(R.id.btn_suivant).setOnClickListener(v -> {
            jour = Utils.ajouterJours(jour, 1);
            rafraichir();
        });
        btnDate.setOnClickListener(v -> choisirDate());
        btnAujourdhui.setOnClickListener(v -> {
            jour = Utils.debutJour(System.currentTimeMillis());
            rafraichir();
        });
        fab.setOnClickListener(v -> {
            if (onglet == 0) {
                Intent i = new Intent(requireContext(), NouveauLotActivity.class);
                i.putExtra("date", jour);
                startActivity(i);
            } else if (liste.isEmpty()) {
                Toast.makeText(requireContext(), T.t("toast_aucun_entana"), Toast.LENGTH_LONG).show();
            } else {
                Dialogues.vente(requireContext(), null, new ArrayList<>(liste), null, this::rafraichir);
            }
        });
        rafraichir();
        return racine;
    }

    @Override
    public void onResume() {
        super.onResume();
        rafraichir();
    }

    private void choisirDate() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(jour);
        new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            Calendar n = Calendar.getInstance();
            n.set(y, m, d, 12, 0, 0);
            jour = Utils.debutJour(n.getTimeInMillis());
            rafraichir();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void afficher() {
        boolean lots = onglet == 0;
        rvLots.setVisibility(lots ? View.VISIBLE : View.GONE);
        rvVentes.setVisibility(lots ? View.GONE : View.VISIBLE);
        vide.setVisibility(lots && liste.isEmpty() ? View.VISIBLE : View.GONE);
        videVentes.setVisibility(!lots && ventes.isEmpty() ? View.VISIBLE : View.GONE);
        fab.setText(T.t(lots ? "fab_nouveau" : "fab_vente"));
    }

    private void rafraichir() {
        if (racine == null) return;
        liste.clear();
        liste.addAll(Store.entre(requireContext(), jour, Utils.ajouterJours(jour, 1)));
        ventes.clear();
        lotDeVente.clear();
        for (Lot l : liste) {
            for (Vente v : l.ventes) {
                v.entana = l.titre();
                ventes.add(v);
                lotDeVente.put(v.id, l);
            }
        }
        Collections.sort(ventes, (a, c) -> Long.compare(c.heure, a.heure));
        adapter.notifyDataSetChanged();
        venteAdapter.notifyDataSetChanged();
        btnDate.setText(Utils.dateLongue(jour));
        btnAujourdhui.setVisibility(Utils.memeJour(jour, System.currentTimeMillis()) ? View.GONE : View.VISIBLE);
        Kpi.afficher(racine.findViewById(R.id.kpi), Bilan.de(liste));
        afficher();
    }
}
