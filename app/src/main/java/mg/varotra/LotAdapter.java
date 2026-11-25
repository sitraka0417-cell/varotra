package mg.varotra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LotAdapter extends RecyclerView.Adapter<LotAdapter.VH> {

    public interface Ecouteur {
        void ouvrir(Lot lot);
    }

    private final List<Lot> items;
    private final Ecouteur ec;

    public LotAdapter(List<Lot> items, Ecouteur ec) {
        this.items = items;
        this.ec = ec;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lot, parent, false);
        T.habiller(v);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final Lot l = items.get(position);
        Bilan b = Bilan.de(java.util.Collections.singletonList(l));
        h.nom.setText(l.titre());
        h.detail.setText(T.t("lot_detail", Utils.qte(b.nbVendus), Utils.argent(b.masonkarena)));
        h.benefice.setText(Utils.argent(b.benefice()));
        h.benefice.setTextColor(androidx.core.content.ContextCompat.getColor(h.itemView.getContext(), b.benefice() >= 0 ? R.color.vert : R.color.rouge));
        if (b.trosa > 0.005) {
            h.badge.setText(T.t("lot_trosa", Utils.argent(b.trosa)));
            h.badge.setVisibility(View.VISIBLE);
        } else {
            h.badge.setVisibility(View.GONE);
        }
        h.itemView.setOnClickListener(v -> ec.ouvrir(l));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView nom, detail, benefice, badge;

        VH(View v) {
            super(v);
            nom = v.findViewById(R.id.nom);
            detail = v.findViewById(R.id.detail);
            benefice = v.findViewById(R.id.benefice);
            badge = v.findViewById(R.id.badge_trosa);
        }
    }
}
