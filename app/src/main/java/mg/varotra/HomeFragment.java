package mg.varotra;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Écran principal : les produits du jour choisi, avec masonkarena / ventes / tombombarotra. */
public class HomeFragment extends Fragment {

    /** Jour affiché (gardé quand on change d'onglet). */
    public static long jour = Utils.debutJour(System.currentTimeMillis());

    private View racine;
    private TextView btnDate, vide, btnAujourdhui;
    private LotAdapter adapter;
    private final List<Lot> liste = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle b) {
        racine = inflater.inflate(R.layout.fragment_home, container, false);
        T.habiller(racine);
        btnDate = racine.findViewById(R.id.btn_date);
        vide = racine.findViewById(R.id.vide);
        btnAujourdhui = racine.findViewById(R.id.btn_aujourdhui);

        RecyclerView rv = racine.findViewById(R.id.liste);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new LotAdapter(liste, lot -> {
            Intent i = new Intent(requireContext(), LotActivity.class);
            i.putExtra("id", lot.id);
            startActivity(i);
        });
        rv.setAdapter(adapter);

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
        racine.findViewById(R.id.fab).setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), NouveauLotActivity.class);
            i.putExtra("date", jour);
            startActivity(i);
        });
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

    private void rafraichir() {
        if (racine == null) return;
        liste.clear();
        liste.addAll(Store.entre(requireContext(), jour, Utils.ajouterJours(jour, 1)));
        adapter.notifyDataSetChanged();
        btnDate.setText(Utils.dateLongue(jour));
        btnAujourdhui.setVisibility(Utils.memeJour(jour, System.currentTimeMillis()) ? View.GONE : View.VISIBLE);
        Kpi.afficher(racine.findViewById(R.id.kpi), Bilan.de(liste));
        vide.setVisibility(liste.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
