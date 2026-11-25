package mg.varotra;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Toutes les dettes (trosa) non soldées, de tous les jours. */
public class TrosaFragment extends Fragment {

    private View racine;
    private TrosaAdapter adapter;
    private final List<TrosaAdapter.Dette> liste = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle b) {
        racine = inflater.inflate(R.layout.fragment_trosa, container, false);
        T.habiller(racine);
        RecyclerView rv = racine.findViewById(R.id.liste);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TrosaAdapter(liste, new TrosaAdapter.Ecouteur() {
            @Override
            public void payer(TrosaAdapter.Dette d) {
                Dialogues.paiement(requireContext(), d.vente, TrosaFragment.this::rafraichir);
            }

            @Override
            public void historique(TrosaAdapter.Dette d) {
                Dialogues.historique(requireContext(), d.vente);
            }
        });
        rv.setAdapter(adapter);
        rafraichir();
        return racine;
    }

    @Override
    public void onResume() {
        super.onResume();
        rafraichir();
    }

    private void rafraichir() {
        if (racine == null) return;
        liste.clear();
        double total = 0;
        for (Lot l : Store.get(requireContext())) {
            for (Vente v : l.ventes) {
                if (v.reste() > 0.005) {
                    liste.add(new TrosaAdapter.Dette(l, v));
                    total += v.reste();
                }
            }
        }
        // les plus anciennes dettes d'abord
        Collections.sort(liste, new Comparator<TrosaAdapter.Dette>() {
            @Override
            public int compare(TrosaAdapter.Dette a, TrosaAdapter.Dette b) {
                return Long.compare(a.lot.date, b.lot.date);
            }
        });
        adapter.notifyDataSetChanged();
        ((TextView) racine.findViewById(R.id.trosa_total)).setText(Utils.argent(total));
        ((TextView) racine.findViewById(R.id.trosa_nb)).setText(T.t("trosa_nb", liste.size()));
        racine.findViewById(R.id.vide).setVisibility(liste.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
