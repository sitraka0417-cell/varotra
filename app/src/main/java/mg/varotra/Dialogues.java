package mg.varotra;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Les formulaires (popups) : achat d'un fangaro, vente, paiement d'une dette, modification du produit. */
public class Dialogues {

    /** Petit TextWatcher qui exécute une action après chaque changement. */
    static class Obs implements TextWatcher {
        private final Runnable action;

        Obs(Runnable action) {
            this.action = action;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int a, int b, int c) {
        }

        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            action.run();
        }
    }

    // =====================================================================
    //  ACHAT D'UN FANGARO (ingrédient)
    // =====================================================================
    public static void achat(final Context c, final Lot lot, final Achat existant, final Runnable apres) {
        View v = LayoutInflater.from(c).inflate(R.layout.dialog_achat, null);
        T.habiller(v);
        final AutoCompleteTextView nom = v.findViewById(R.id.d_nom);
        final EditText qte = v.findViewById(R.id.d_qte);
        final EditText prix = v.findViewById(R.id.d_prix);
        final Spinner unite = v.findViewById(R.id.d_unite);
        final TextView total = v.findViewById(R.id.d_total);
        ((TextInputLayout) v.findViewById(R.id.til_prix)).setSuffixText(T.t("devise"));

        final String[] unites = T.liste("unites");
        unite.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, unites));

        final Map<String, Achat> connus = Store.connusAchats(c);
        List<String> noms = new ArrayList<>();
        for (Achat a : connus.values()) noms.add(a.nom);
        for (String d : T.liste("achat_suggestions")) {
            if (!connus.containsKey(d.toLowerCase())) noms.add(d);
        }
        nom.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_dropdown_item_1line, noms));
        nom.setOnItemClickListener((parent, view, pos, id) -> {
            Achat k = connus.get(String.valueOf(parent.getItemAtPosition(pos)).trim().toLowerCase());
            if (k != null && prix.getText().toString().trim().isEmpty()) {
                prix.setText(Utils.nombre(k.prix));
                int ui = Arrays.asList(unites).indexOf(k.unite);
                unite.setSelection(Math.max(ui, 0));
            }
        });

        final Runnable maj = () -> {
            double q = Utils.nz(Utils.lire(qte.getText()));
            double p = Utils.nz(Utils.lire(prix.getText()));
            total.setText(T.t("achat_total", Utils.argent(q * p)));
        };
        qte.addTextChangedListener(new Obs(maj));
        prix.addTextChangedListener(new Obs(maj));

        if (existant != null) {
            nom.setText(existant.nom);
            qte.setText(Utils.nombre(existant.qte));
            unite.setSelection(Math.max(Arrays.asList(unites).indexOf(existant.unite), 0));
            prix.setText(Utils.nombre(existant.prix));
        } else {
            qte.setText("1");
        }
        maj.run();

        MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(c)
                .setTitle(existant == null ? T.t("achat_titre_ajout") : T.t("achat_titre_modif"))
                .setView(v)
                .setPositiveButton(existant == null ? T.t("btn_ajouter") : T.t("btn_enregistrer"), null)
                .setNegativeButton(T.t("btn_annuler"), null);
        if (existant == null) b.setNeutralButton(T.t("btn_ajouter_suivant"), null);
        final AlertDialog d = b.create();
        d.setOnShowListener(x -> {
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(bt -> {
                if (enregistrerAchat(c, lot, existant, nom, qte, unite, prix)) {
                    d.dismiss();
                    if (apres != null) apres.run();
                }
            });
            if (existant == null) {
                d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(bt -> {
                    if (enregistrerAchat(c, lot, null, nom, qte, unite, prix)) {
                        Toast.makeText(c, T.t("toast_achat_ajoute"), Toast.LENGTH_SHORT).show();
                        nom.setText("");
                        qte.setText("1");
                        unite.setSelection(0);
                        prix.setText("");
                        nom.requestFocus();
                        if (apres != null) apres.run();
                    }
                });
            }
        });
        d.show();
        nom.requestFocus();
    }

    private static boolean enregistrerAchat(Context c, Lot lot, Achat existant, AutoCompleteTextView nom,
                                            EditText qte, Spinner unite, EditText prix) {
        String n = nom.getText().toString().trim();
        if (n.isEmpty()) {
            nom.setError(T.t("erreur_nom_achat"));
            return false;
        }
        double q = Utils.lire(qte.getText());
        if (Double.isNaN(q)) q = 1;
        if (q <= 0) {
            qte.setError(T.t("erreur_qte"));
            return false;
        }
        double p = Utils.lire(prix.getText());
        if (Double.isNaN(p) || p < 0) {
            prix.setError(T.t("erreur_prix"));
            return false;
        }
        Achat a = existant != null ? existant : new Achat();
        a.nom = n;
        a.qte = q;
        a.unite = String.valueOf(unite.getSelectedItem());
        a.prix = p;
        if (existant == null) lot.achats.add(0, a);
        Store.sauver(c);
        return true;
    }

    // =====================================================================
    //  VENTE
    // =====================================================================
    public static void vente(Context c, Lot lot, Vente existant, Runnable apres) {
        vente(c, lot, null, existant, apres);
    }

    /** Si « choix » n'est pas vide, un sélecteur d'entana apparaît en haut du formulaire. */
    public static void vente(final Context c, final Lot lotInit, final List<Lot> choix, final Vente existant,
                             final Runnable apres) {
        final Lot[] cur = {lotInit != null ? lotInit : choix.get(0)};
        View v = LayoutInflater.from(c).inflate(R.layout.dialog_vente, null);
        T.habiller(v);
        final AutoCompleteTextView client = v.findViewById(R.id.d_client);
        final EditText qte = v.findViewById(R.id.d_qte);
        final EditText pu = v.findViewById(R.id.d_pu);
        final EditText total = v.findViewById(R.id.d_total);
        final EditText paye = v.findViewById(R.id.d_paye);
        final TextView info = v.findViewById(R.id.d_info);
        final TextView reste = v.findViewById(R.id.d_reste);
        final View tilPaye = v.findViewById(R.id.til_paye);
        final ChipGroup mode = v.findViewById(R.id.d_mode);
        ((TextInputLayout) v.findViewById(R.id.til_pu)).setSuffixText(T.t("devise"));
        ((TextInputLayout) v.findViewById(R.id.til_total)).setSuffixText(T.t("devise"));
        ((TextInputLayout) v.findViewById(R.id.til_paye)).setSuffixText(T.t("devise"));

        client.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_dropdown_item_1line, Store.nomsClients(c)));

        final boolean[] bloque = {true};
        final Runnable[] maj = new Runnable[1];

        maj[0] = () -> {
            double q = Utils.nz(Utils.lire(qte.getText()));
            double p = Utils.nz(Utils.lire(pu.getText()));
            double t = Utils.nz(Utils.lire(total.getText()));
            double auto = q * p;
            if (auto - t > 0.5) {
                info.setText(T.t("vente_reduction", Utils.argent(auto - t)));
                info.setVisibility(View.VISIBLE);
            } else if (t - auto > 0.5 && p > 0) {
                info.setText(T.t("vente_majoration", Utils.argent(t - auto)));
                info.setVisibility(View.VISIBLE);
            } else {
                info.setVisibility(View.GONE);
            }
            int id = mode.getCheckedChipId();
            tilPaye.setVisibility(id == R.id.c_partiel ? View.VISIBLE : View.GONE);
            double pa = id == R.id.c_paye ? t : (id == R.id.c_trosa ? 0 : Utils.nz(Utils.lire(paye.getText())));
            double rest = Math.max(0, t - pa);
            reste.setText(T.t("vente_reste", Utils.argent(rest)));
            reste.setTextColor(androidx.core.content.ContextCompat.getColor(c, rest > 0.005 ? R.color.rouge : R.color.vert));
        };

        // Quantité ou prix unitaire changé => total recalculé automatiquement
        final Runnable recalcul = () -> {
            if (bloque[0]) return;
            double q = Utils.nz(Utils.lire(qte.getText()));
            double p = Utils.nz(Utils.lire(pu.getText()));
            bloque[0] = true;
            total.setText(Utils.nombre(q * p));
            bloque[0] = false;
            maj[0].run();
        };

        // Remplissage initial
        if (existant != null) {
            client.setText(existant.client);
            qte.setText(Utils.nombre(existant.qte));
            pu.setText(Utils.nombre(existant.pu));
            total.setText(Utils.nombre(existant.total));
            int st = existant.statut();
            mode.check(st == Vente.PAYE ? R.id.c_paye : (st == Vente.TROSA ? R.id.c_trosa : R.id.c_partiel));
            if (st == Vente.PARTIEL) paye.setText(Utils.nombre(existant.paye));
        } else {
            qte.setText("1");
            pu.setText(cur[0].prixVente > 0 ? Utils.nombre(cur[0].prixVente) : "");
            total.setText(cur[0].prixVente > 0 ? Utils.nombre(cur[0].prixVente) : "");
            mode.check(R.id.c_paye);
        }
        bloque[0] = false;

        qte.addTextChangedListener(new Obs(recalcul));
        pu.addTextChangedListener(new Obs(recalcul));
        total.addTextChangedListener(new Obs(() -> {
            if (bloque[0]) return;
            maj[0].run();
        }));
        paye.addTextChangedListener(new Obs(() -> maj[0].run()));
        mode.setOnCheckedStateChangeListener((g, ids) -> maj[0].run());

        v.findViewById(R.id.d_plus).setOnClickListener(x -> {
            double q = Utils.nz(Utils.lire(qte.getText()));
            qte.setText(Utils.nombre(q + 1));
        });
        v.findViewById(R.id.d_moins).setOnClickListener(x -> {
            double q = Utils.nz(Utils.lire(qte.getText()));
            qte.setText(Utils.nombre(Math.max(1, q - 1)));
        });
        maj[0].run();

        if (choix != null && !choix.isEmpty()) {
            v.findViewById(R.id.d_entana_box).setVisibility(View.VISIBLE);
            Spinner sp = v.findViewById(R.id.d_entana);
            List<String> noms = new ArrayList<>();
            for (Lot l : choix) noms.add(l.titre());
            sp.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, noms));
            sp.setSelection(Math.max(0, choix.indexOf(cur[0])));
            sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    cur[0] = choix.get(pos);
                    if (existant == null) {
                        pu.setText(cur[0].prixVente > 0 ? Utils.nombre(cur[0].prixVente) : "");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(c)
                .setTitle(existant == null ? T.t("vente_titre_ajout") : T.t("vente_titre_modif"))
                .setView(v)
                .setPositiveButton(existant == null ? T.t("btn_ajouter") : T.t("btn_enregistrer"), null)
                .setNegativeButton(T.t("btn_annuler"), null);
        final AlertDialog d = b.create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(bt -> {
            if (enregistrerVente(c, cur[0], existant, client, qte, pu, total, paye, mode)) {
                d.dismiss();
                if (apres != null) apres.run();
            }
        }));
        d.show();
    }

    private static boolean enregistrerVente(Context c, Lot lot, Vente existant, EditText client, EditText qte,
                                            EditText pu, EditText total, EditText paye, ChipGroup mode) {
        double q = Utils.lire(qte.getText());
        if (Double.isNaN(q) || q <= 0) {
            qte.setError(T.t("erreur_qte"));
            return false;
        }
        double p = Utils.nz(Utils.lire(pu.getText()));
        if (p < 0) p = 0;
        double t = Utils.lire(total.getText());
        if (Double.isNaN(t) || t < 0) {
            total.setError(T.t("erreur_total"));
            return false;
        }
        int id = mode.getCheckedChipId();
        double pa;
        if (id == R.id.c_paye) {
            pa = t;
        } else if (id == R.id.c_trosa) {
            pa = 0;
        } else {
            pa = Utils.lire(paye.getText());
            if (Double.isNaN(pa) || pa <= 0 || pa >= t) {
                paye.setError(T.t("erreur_paye_partiel"));
                return false;
            }
        }
        Vente x = existant != null ? existant : new Vente();
        x.client = client.getText().toString().trim();
        x.qte = q;
        x.pu = p;
        x.total = t;
        x.manuel = Math.abs(q * p - t) > 0.5;
        if (existant == null) {
            x.heure = System.currentTimeMillis();
            lot.ventes.add(0, x);
        }
        x.definirPaye(Math.min(pa, t), System.currentTimeMillis());
        if (lot.prixVente <= 0 && p > 0) lot.prixVente = p;
        Store.sauver(c);
        if (lot.nbPrepares > 0 && lot.nbVendus() > lot.nbPrepares + 0.0001) {
            Toast.makeText(c, T.t("toast_stock_depasse", Utils.qte(lot.nbPrepares)), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(c, T.t("toast_vente_ok"), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    // =====================================================================
    //  PAIEMENT D'UNE DETTE
    // =====================================================================
    public static void paiement(final Context c, final Vente vente, final Runnable apres) {
        View v = LayoutInflater.from(c).inflate(R.layout.dialog_paiement, null);
        T.habiller(v);
        final EditText montant = v.findViewById(R.id.d_montant);
        ((TextInputLayout) v.findViewById(R.id.til_montant)).setSuffixText(T.t("devise"));
        ((TextView) v.findViewById(R.id.d_resume)).setText(
                T.t("paiement_resume", vente.nomClient(), Utils.argent(vente.reste())));
        montant.setText(Utils.nombre(vente.reste()));
        v.findViewById(R.id.d_tout).setOnClickListener(x -> montant.setText(Utils.nombre(vente.reste())));

        final AlertDialog d = new MaterialAlertDialogBuilder(c)
                .setTitle(T.t("paiement_titre"))
                .setView(v)
                .setPositiveButton(T.t("btn_valider"), null)
                .setNegativeButton(T.t("btn_annuler"), null)
                .create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(bt -> {
            double m = Utils.lire(montant.getText());
            if (Double.isNaN(m) || m <= 0) {
                montant.setError(T.t("erreur_montant"));
                return;
            }
            vente.ajouterPaiement(m, System.currentTimeMillis());
            Store.sauver(c);
            Toast.makeText(c, T.t("toast_paiement_ok"), Toast.LENGTH_SHORT).show();
            d.dismiss();
            if (apres != null) apres.run();
        }));
        d.show();
    }

    // =====================================================================
    //  MODIFIER LE PRODUIT (nom, prix, nombre fabriqué)
    // =====================================================================
    public static void produit(final Context c, final Lot lot, final Runnable apres) {
        View v = LayoutInflater.from(c).inflate(R.layout.dialog_produit, null);
        T.habiller(v);
        final EditText nom = v.findViewById(R.id.d_nom);
        final EditText prix = v.findViewById(R.id.d_prix);
        final EditText prep = v.findViewById(R.id.d_prepares);
        ((TextInputLayout) v.findViewById(R.id.til_prix)).setSuffixText(T.t("devise"));
        nom.setText(lot.nom);
        if (lot.prixVente > 0) prix.setText(Utils.nombre(lot.prixVente));
        if (lot.nbPrepares > 0) prep.setText(Utils.nombre(lot.nbPrepares));

        final AlertDialog d = new MaterialAlertDialogBuilder(c)
                .setTitle(T.t("produit_titre"))
                .setView(v)
                .setPositiveButton(T.t("btn_enregistrer"), null)
                .setNegativeButton(T.t("btn_annuler"), null)
                .create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(bt -> {
            String n = nom.getText().toString().trim();
            if (n.isEmpty()) {
                nom.setError(T.t("erreur_nom_produit"));
                return;
            }
            lot.nom = n;
            lot.prixVente = Math.max(0, Utils.nz(Utils.lire(prix.getText())));
            lot.nbPrepares = Math.max(0, Utils.nz(Utils.lire(prep.getText())));
            Store.sauver(c);
            d.dismiss();
            if (apres != null) apres.run();
        }));
        d.show();
    }

    // =====================================================================
    //  HISTORIQUE DES PAIEMENTS D'UNE VENTE
    // =====================================================================
    public static void historique(final Context c, final Vente vente) {
        StringBuilder sb = new StringBuilder();
        sb.append(vente.nomClient()).append(" — ").append(T.t("histo_total", Utils.argent(vente.total))).append("\n\n");
        if (vente.paiements.isEmpty()) {
            sb.append(T.t("histo_vide")).append("\n");
        } else {
            for (Paiement p : vente.paiements) {
                sb.append("• ").append(Utils.dateLongue(p.date)).append(" : ").append(Utils.argent(p.montant)).append("\n");
            }
        }
        sb.append("\n").append(T.t("histo_voaray", Utils.argent(vente.paye)));
        sb.append("\n").append(T.t("histo_reste", Utils.argent(vente.reste())));
        new MaterialAlertDialogBuilder(c)
                .setTitle(T.t("histo_titre"))
                .setMessage(sb.toString())
                .setPositiveButton(T.t("btn_fermer"), null)
                .show();
    }
}
