package mg.varotra;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/** Formulaire d'achat : le produit du jour + tous les fangaro à acheter (quantité x prix unitaire = total auto). */
public class NouveauLotActivity extends BaseActivity {

    private long date;
    private LinearLayout conteneur;
    private TextView totalGeneral, btnDate;
    private AutoCompleteTextView champNom;
    private EditText champPrixVente, champPrepares;
    private String[] unites;
    private Map<String, Achat> connus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nouveau_lot);
        T.habiller(findViewById(android.R.id.content));

        date = Utils.debutJour(getIntent().getLongExtra("date", System.currentTimeMillis()));
        unites = T.liste("unites");
        connus = Store.connusAchats(this);

        conteneur = findViewById(R.id.conteneur_lignes);
        totalGeneral = findViewById(R.id.total_general);
        btnDate = findViewById(R.id.btn_date);
        champNom = findViewById(R.id.champ_nom);
        champPrixVente = findViewById(R.id.champ_prix_vente);
        champPrepares = findViewById(R.id.champ_prepares);
        ((TextInputLayout) findViewById(R.id.til_prix_vente)).setSuffixText(T.t("devise"));

        List<String> produits = Store.nomsProduits(this);
        for (String d : T.liste("produit_suggestions")) {
            boolean existe = false;
            for (String p : produits) if (p.equalsIgnoreCase(d)) existe = true;
            if (!existe) produits.add(d);
        }
        champNom.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, produits));

        findViewById(R.id.btn_retour).setOnClickListener(v -> finish());
        findViewById(R.id.btn_ligne).setOnClickListener(v -> {
            View r = ajouterLigne();
            r.findViewById(R.id.l_nom).requestFocus();
        });
        findViewById(R.id.btn_copie).setOnClickListener(v -> copierPrecedent());
        findViewById(R.id.btn_enregistrer).setOnClickListener(v -> enregistrer());
        btnDate.setOnClickListener(v -> choisirDate());

        majDate();
        ajouterLigne();
        majTotal();
    }

    private void majDate() {
        btnDate.setText(Utils.dateLongue(date));
    }

    private void choisirDate() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar n = Calendar.getInstance();
            n.set(y, m, d, 12, 0, 0);
            date = Utils.debutJour(n.getTimeInMillis());
            majDate();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ----- Lignes de fangaro -----
    private View ajouterLigne() {
        final View row = getLayoutInflater().inflate(R.layout.item_ligne_achat, conteneur, false);
        T.habiller(row);
        final AutoCompleteTextView nom = row.findViewById(R.id.l_nom);
        final EditText qte = row.findViewById(R.id.l_qte);
        final EditText prix = row.findViewById(R.id.l_prix);
        final Spinner unite = row.findViewById(R.id.l_unite);
        ((TextInputLayout) row.findViewById(R.id.l_til_prix)).setSuffixText(T.t("devise"));
        unite.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unites));
        qte.setText("1");

        List<String> noms = new ArrayList<>();
        for (Achat a : connus.values()) noms.add(a.nom);
        for (String d : T.liste("achat_suggestions")) {
            if (!connus.containsKey(d.toLowerCase())) noms.add(d);
        }
        nom.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, noms));
        nom.setOnItemClickListener((parent, view, pos, id) -> {
            Achat k = connus.get(String.valueOf(parent.getItemAtPosition(pos)).trim().toLowerCase());
            if (k != null && prix.getText().toString().trim().isEmpty()) {
                prix.setText(Utils.nombre(k.prix));
                unite.setSelection(Math.max(Arrays.asList(unites).indexOf(k.unite), 0));
            }
        });

        Dialogues.Obs obs = new Dialogues.Obs(this::majTotal);
        qte.addTextChangedListener(obs);
        prix.addTextChangedListener(obs);

        row.findViewById(R.id.l_suppr).setOnClickListener(v -> {
            conteneur.removeView(row);
            if (conteneur.getChildCount() == 0) ajouterLigne();
            majTotal();
        });
        conteneur.addView(row);
        majLigne(row);
        return row;
    }

    private double totalLigne(View row) {
        double q = Utils.nz(Utils.lire(((EditText) row.findViewById(R.id.l_qte)).getText()));
        double p = Utils.nz(Utils.lire(((EditText) row.findViewById(R.id.l_prix)).getText()));
        return q * p;
    }

    private void majLigne(View row) {
        ((TextView) row.findViewById(R.id.l_total)).setText(T.t("achat_total", Utils.argent(totalLigne(row))));
    }

    private void majTotal() {
        double t = 0;
        for (int i = 0; i < conteneur.getChildCount(); i++) {
            View row = conteneur.getChildAt(i);
            majLigne(row);
            t += totalLigne(row);
        }
        totalGeneral.setText(Utils.argent(t));
    }

    /** Reprend les fangaro du dernier lot du même nom (ou du tout dernier lot si le nom est vide). */
    private void copierPrecedent() {
        Lot src = Store.dernierLot(this, champNom.getText().toString());
        if (src == null) {
            Toast.makeText(this, T.t("toast_aucun_precedent"), Toast.LENGTH_SHORT).show();
            return;
        }
        if (champNom.getText().toString().trim().isEmpty()) champNom.setText(src.nom);
        if (champPrixVente.getText().toString().trim().isEmpty() && src.prixVente > 0) {
            champPrixVente.setText(Utils.nombre(src.prixVente));
        }
        if (champPrepares.getText().toString().trim().isEmpty() && src.nbPrepares > 0) {
            champPrepares.setText(Utils.nombre(src.nbPrepares));
        }
        conteneur.removeAllViews();
        for (Achat a : src.achats) {
            View row = ajouterLigne();
            ((AutoCompleteTextView) row.findViewById(R.id.l_nom)).setText(a.nom, false);
            ((EditText) row.findViewById(R.id.l_qte)).setText(Utils.nombre(a.qte));
            ((EditText) row.findViewById(R.id.l_prix)).setText(Utils.nombre(a.prix));
            ((Spinner) row.findViewById(R.id.l_unite)).setSelection(Math.max(Arrays.asList(unites).indexOf(a.unite), 0));
        }
        majTotal();
        Toast.makeText(this, T.t("toast_copie_ok", src.achats.size()), Toast.LENGTH_SHORT).show();
    }

    private void enregistrer() {
        String n = champNom.getText().toString().trim();
        if (n.isEmpty()) {
            champNom.setError(T.t("erreur_nom_produit"));
            champNom.requestFocus();
            return;
        }
        List<Achat> achats = new ArrayList<>();
        boolean ok = true;
        for (int i = 0; i < conteneur.getChildCount(); i++) {
            View row = conteneur.getChildAt(i);
            AutoCompleteTextView nom = row.findViewById(R.id.l_nom);
            EditText qte = row.findViewById(R.id.l_qte);
            EditText prix = row.findViewById(R.id.l_prix);
            Spinner unite = row.findViewById(R.id.l_unite);
            String nm = nom.getText().toString().trim();
            String ps = prix.getText().toString().trim();
            if (nm.isEmpty() && ps.isEmpty()) continue; // ligne vide ignorée
            if (nm.isEmpty()) {
                nom.setError(T.t("erreur_nom_achat"));
                ok = false;
                continue;
            }
            double q = Utils.lire(qte.getText());
            if (Double.isNaN(q)) q = 1;
            if (q <= 0) {
                qte.setError(T.t("erreur_qte"));
                ok = false;
                continue;
            }
            double p = Utils.lire(prix.getText());
            if (Double.isNaN(p) || p < 0) {
                prix.setError(T.t("erreur_prix"));
                ok = false;
                continue;
            }
            Achat a = new Achat();
            a.nom = nm;
            a.qte = q;
            a.unite = String.valueOf(unite.getSelectedItem());
            a.prix = p;
            achats.add(a);
        }
        if (!ok) {
            Toast.makeText(this, T.t("toast_corriger"), Toast.LENGTH_SHORT).show();
            return;
        }
        Lot lot = new Lot();
        lot.nom = n;
        lot.date = date;
        lot.prixVente = Math.max(0, Utils.nz(Utils.lire(champPrixVente.getText())));
        lot.nbPrepares = Math.max(0, Utils.nz(Utils.lire(champPrepares.getText())));
        lot.achats = achats;
        Store.get(this).add(lot);
        Store.sauver(this);
        Toast.makeText(this, T.t("toast_lot_cree"), Toast.LENGTH_SHORT).show();

        Intent i = new Intent(this, LotActivity.class);
        i.putExtra("id", lot.id);
        i.putExtra("onglet", 1); // directement sur Fivarotana
        startActivity(i);
        finish();
    }
}
