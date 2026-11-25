package mg.varotra;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.Collections;

/** Un produit du jour : onglet Fividianana (achats d'ingrédients) et onglet Fivarotana (ventes). */
public class LotActivity extends BaseActivity implements AchatAdapter.Ecouteur, VenteAdapter.Ecouteur {

    private Lot lot;
    private AchatAdapter achatAdapter;
    private VenteAdapter venteAdapter;
    private RecyclerView rvAchats, rvVentes;
    private TextView titre, sousTitre, vide, infoPrix;
    private ExtendedFloatingActionButton fab;
    private TabLayout onglets;
    private View kpi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lot);
        T.habiller(findViewById(android.R.id.content));

        lot = Store.parId(this, getIntent().getStringExtra("id"));
        if (lot == null) {
            finish();
            return;
        }

        titre = findViewById(R.id.titre);
        sousTitre = findViewById(R.id.sous_titre);
        vide = findViewById(R.id.vide);
        infoPrix = findViewById(R.id.info_prix);
        fab = findViewById(R.id.fab);
        onglets = findViewById(R.id.onglets);
        kpi = findViewById(R.id.kpi);
        rvAchats = findViewById(R.id.liste_achats);
        rvVentes = findViewById(R.id.liste_ventes);

        rvAchats.setLayoutManager(new LinearLayoutManager(this));
        rvVentes.setLayoutManager(new LinearLayoutManager(this));
        achatAdapter = new AchatAdapter(lot.achats, this);
        venteAdapter = new VenteAdapter(lot.ventes, this);
        rvAchats.setAdapter(achatAdapter);
        rvVentes.setAdapter(venteAdapter);

        onglets.addTab(onglets.newTab().setText(T.t("onglet_achats")));
        onglets.addTab(onglets.newTab().setText(T.t("onglet_ventes")));
        onglets.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                afficherOnglet();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        findViewById(R.id.btn_retour).setOnClickListener(v -> finish());
        findViewById(R.id.btn_menu).setOnClickListener(this::menu);
        infoPrix.setOnClickListener(v -> Dialogues.produit(this, lot, this::rafraichir));
        fab.setOnClickListener(v -> {
            if (onglets.getSelectedTabPosition() == 0) {
                Dialogues.achat(this, lot, null, this::rafraichir);
            } else {
                Dialogues.vente(this, lot, null, this::rafraichir);
            }
        });

        // Ouvre directement l'onglet demandé (ex. après la création du lot : Fivarotana)
        int tab = getIntent().getIntExtra("onglet", 0);
        TabLayout.Tab t = onglets.getTabAt(tab);
        if (t != null) t.select();
        afficherOnglet();
        rafraichir();
    }

    private boolean ongletAchats() {
        return onglets.getSelectedTabPosition() == 0;
    }

    private void afficherOnglet() {
        boolean achats = ongletAchats();
        rvAchats.setVisibility(achats ? View.VISIBLE : View.GONE);
        rvVentes.setVisibility(achats ? View.GONE : View.VISIBLE);
        fab.setText(achats ? T.t("fab_achat") : T.t("fab_vente"));
        majVide();
    }

    private void majVide() {
        boolean achats = ongletAchats();
        int n = achats ? lot.achats.size() : lot.ventes.size();
        vide.setText(achats ? T.t("vide_achats") : T.t("vide_ventes"));
        vide.setVisibility(n == 0 ? View.VISIBLE : View.GONE);
    }

    private void rafraichir() {
        achatAdapter.notifyDataSetChanged();
        venteAdapter.notifyDataSetChanged();
        titre.setText(lot.titre());
        sousTitre.setText(Utils.dateLongue(lot.date));
        infoPrix.setText(lot.prixVente > 0
                ? T.t("info_prix", Utils.argent(lot.prixVente))
                : T.t("info_prix_vide"));
        Bilan b = Bilan.de(Collections.singletonList(lot));
        Kpi.afficher(kpi, b);
        StringBuilder sb = new StringBuilder();
        if (lot.masonkarena() > 0) {
            sb.append(T.t("revient", Utils.argent(lot.coutRevient())));
        }
        if (lot.nbPrepares > 0) {
            if (sb.length() > 0) sb.append("   •   ");
            sb.append(T.t("sisa", Utils.qte(Math.max(0, lot.nbPrepares - lot.nbVendus()))));
        }
        Kpi.revient(kpi, sb.toString());
        majVide();
    }

    // ----- Achats -----
    @Override
    public void editer(final Achat a) {
        Dialogues.achat(this, lot, a, this::rafraichir);
    }

    @Override
    public void supprimer(final Achat a) {
        final int index = lot.achats.indexOf(a);
        if (index < 0) return;
        lot.achats.remove(index);
        Store.sauver(this);
        rafraichir();
        Snackbar.make(findViewById(R.id.fab), T.t("snack_achat_supprime"), Snackbar.LENGTH_LONG)
                .setAction(T.t("btn_annuler_action"), v -> {
                    lot.achats.add(Math.min(index, lot.achats.size()), a);
                    Store.sauver(this);
                    rafraichir();
                }).setAnchorView(fab).show();
    }

    // ----- Ventes -----
    @Override
    public void editer(final Vente x) {
        Dialogues.vente(this, lot, x, this::rafraichir);
    }

    @Override
    public void supprimer(final Vente x) {
        final int index = lot.ventes.indexOf(x);
        if (index < 0) return;
        lot.ventes.remove(index);
        Store.sauver(this);
        rafraichir();
        Snackbar.make(findViewById(R.id.fab), T.t("snack_vente_supprimee"), Snackbar.LENGTH_LONG)
                .setAction(T.t("btn_annuler_action"), v -> {
                    lot.ventes.add(Math.min(index, lot.ventes.size()), x);
                    Store.sauver(this);
                    rafraichir();
                }).setAnchorView(fab).show();
    }

    @Override
    public void historique(Vente x) {
        Dialogues.historique(this, x);
    }

    @Override
    public void payer(Vente x) {
        Dialogues.paiement(this, x, this::rafraichir);
    }

    // ----- Menu -----
    private void menu(View ancre) {
        PopupMenu pm = new PopupMenu(this, ancre);
        pm.getMenu().add(0, 1, 0, T.t("menu_modifier_produit"));
        pm.getMenu().add(0, 2, 1, T.t("menu_dupliquer"));
        pm.getMenu().add(0, 3, 2, T.t("menu_partager"));
        pm.getMenu().add(0, 4, 3, T.t("menu_supprimer_produit"));
        pm.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                Dialogues.produit(this, lot, this::rafraichir);
            } else if (id == 2) {
                dupliquer();
            } else if (id == 3) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, Utils.texteLot(lot));
                startActivity(Intent.createChooser(i, T.t("partage_choix")));
            } else if (id == 4) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(T.t("supprimer_produit_titre"))
                        .setMessage(T.t("supprimer_produit_message", lot.titre()))
                        .setPositiveButton(T.t("btn_supprimer"), (d, w) -> {
                            Store.get(this).remove(lot);
                            Store.sauver(this);
                            Toast.makeText(this, T.t("toast_produit_supprime"), Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton(T.t("btn_annuler"), null)
                        .show();
            }
            return true;
        });
        pm.show();
    }

    /** Copie le produit et ses fangaro vers un autre jour (sans les ventes). */
    private void dupliquer() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Utils.ajouterJours(lot.date, 1));
        new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar n = Calendar.getInstance();
            n.set(y, m, d, 12, 0, 0);
            Lot copie = lot.copie(n.getTimeInMillis());
            Store.get(this).add(copie);
            Store.sauver(this);
            Toast.makeText(this, T.t("toast_duplique", Utils.dateLongue(copie.date)), Toast.LENGTH_LONG).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }
}
