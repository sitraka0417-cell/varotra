package mg.varotra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TrosaAdapter extends RecyclerView.Adapter<TrosaAdapter.VH> {

    /** Une dette = une vente non soldée + le lot auquel elle appartient. */
    public static class Dette {
        public final Lot lot;
        public final Vente vente;

        public Dette(Lot lot, Vente vente) {
            this.lot = lot;
            this.vente = vente;
        }
    }

    public interface Ecouteur {
        void payer(Dette d);

        void historique(Dette d);
    }

    private final List<Dette> items;
    private final Ecouteur ec;

    public TrosaAdapter(List<Dette> items, Ecouteur ec) {
        this.items = items;
        this.ec = ec;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trosa, parent, false);
        T.habiller(v);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final Dette d = items.get(position);
        h.client.setText(d.vente.nomClient());
        h.detail.setText(T.t("trosa_detail", d.lot.titre(), Utils.jourMois(d.lot.date),
                Utils.qte(d.vente.qte), Utils.argent(d.vente.total)));
        h.reste.setText(T.t("trosa_reste", Utils.argent(d.vente.reste())));
        h.btnPayer.setOnClickListener(v -> ec.payer(d));
        h.itemView.setOnClickListener(v -> ec.historique(d));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView client, detail, reste, btnPayer;

        VH(View v) {
            super(v);
            client = v.findViewById(R.id.client);
            detail = v.findViewById(R.id.detail);
            reste = v.findViewById(R.id.reste);
            btnPayer = v.findViewById(R.id.btn_payer);
        }
    }
}
