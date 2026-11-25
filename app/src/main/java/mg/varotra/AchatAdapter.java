package mg.varotra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AchatAdapter extends RecyclerView.Adapter<AchatAdapter.VH> {

    public interface Ecouteur {
        void editer(Achat a);

        void supprimer(Achat a);
    }

    private final List<Achat> items;
    private final Ecouteur ec;

    public AchatAdapter(List<Achat> items, Ecouteur ec) {
        this.items = items;
        this.ec = ec;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achat, parent, false);
        T.habiller(v);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final Achat a = items.get(position);
        h.nom.setText(a.nom);
        h.detail.setText(T.t("achat_detail", Utils.qte(a.qte), a.unite, Utils.argent(a.prix)));
        h.sousTotal.setText(Utils.argent(a.total()));
        h.itemView.setOnClickListener(v -> ec.editer(a));
        h.btnSuppr.setOnClickListener(v -> ec.supprimer(a));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView nom, detail, sousTotal;
        View btnSuppr;

        VH(View v) {
            super(v);
            nom = v.findViewById(R.id.nom);
            detail = v.findViewById(R.id.detail);
            sousTotal = v.findViewById(R.id.sous_total);
            btnSuppr = v.findViewById(R.id.btn_suppr);
        }
    }
}
