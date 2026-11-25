package mg.varotra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VenteAdapter extends RecyclerView.Adapter<VenteAdapter.VH> {

    public interface Ecouteur {
        void editer(Vente v);

        void supprimer(Vente v);

        void payer(Vente v);

        void historique(Vente v);
    }

    private final List<Vente> items;
    private final Ecouteur ec;

    public VenteAdapter(List<Vente> items, Ecouteur ec) {
        this.items = items;
        this.ec = ec;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vente, parent, false);
        T.habiller(v);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final Vente x = items.get(position);
        h.client.setText(x.nomClient());
        String detail = T.t("vente_detail", Utils.qte(x.qte), Utils.argent(x.pu));
        if (x.remise() > 0.5) detail += " • " + T.t("vente_remise_court", Utils.argent(x.remise()));
        h.detail.setText(detail);
        h.total.setText(Utils.argent(x.total));

        int st = x.statut();
        if (st == Vente.PAYE) {
            h.badge.setText(T.t("statut_paye"));
            h.badge.setBackgroundResource(R.drawable.bg_badge_vert);
            h.badge.setTextColor(androidx.core.content.ContextCompat.getColor(h.itemView.getContext(), R.color.vert));
        } else if (st == Vente.TROSA) {
            h.badge.setText(T.t("statut_trosa", Utils.argent(x.reste())));
            h.badge.setBackgroundResource(R.drawable.bg_badge_rouge);
            h.badge.setTextColor(androidx.core.content.ContextCompat.getColor(h.itemView.getContext(), R.color.rouge));
        } else {
            h.badge.setText(T.t("statut_partiel", Utils.argent(x.paye), Utils.argent(x.reste())));
            h.badge.setBackgroundResource(R.drawable.bg_badge_ambre);
            h.badge.setTextColor(androidx.core.content.ContextCompat.getColor(h.itemView.getContext(), R.color.ambre));
        }
        h.btnPayer.setVisibility(st == Vente.PAYE ? View.GONE : View.VISIBLE);

        h.itemView.setOnClickListener(v -> ec.editer(x));
        h.btnSuppr.setOnClickListener(v -> ec.supprimer(x));
        h.btnPayer.setOnClickListener(v -> ec.payer(x));
        h.btnHisto.setVisibility(x.paiements.isEmpty() ? View.GONE : View.VISIBLE);
        h.btnHisto.setOnClickListener(v -> ec.historique(x));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView client, detail, total, badge, btnPayer, btnHisto;
        View btnSuppr;

        VH(View v) {
            super(v);
            client = v.findViewById(R.id.client);
            detail = v.findViewById(R.id.detail);
            total = v.findViewById(R.id.total);
            badge = v.findViewById(R.id.badge);
            btnPayer = v.findViewById(R.id.btn_payer);
            btnHisto = v.findViewById(R.id.btn_histo);
            btnSuppr = v.findViewById(R.id.btn_suppr);
        }
    }
}
