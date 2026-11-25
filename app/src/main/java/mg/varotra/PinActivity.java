package mg.varotra;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Écran du code PIN (4 chiffres).
 * Modes : VERIFIER (déverrouiller), CREER (nouveau code), CHANGER (ancien puis nouveau), RETIRER (ancien puis supprimer).
 */
public class PinActivity extends AppCompatActivity {
    public static final int VERIFIER = 0, CREER = 1, CHANGER = 2, RETIRER = 3;
    private static final int LONGUEUR = 4;

    private int mode;
    private int etape;            // 0 = saisie, 1 = confirmation (création)
    private boolean ancienOk;     // pour CHANGER / RETIRER
    private String premier = "";
    private final StringBuilder saisie = new StringBuilder();
    private TextView titre, message;
    private final View[] points = new View[LONGUEUR];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getIntent().getIntExtra("mode", VERIFIER);
        setContentView(construire());
        getWindow().setStatusBarColor(0xFF312E81);
        majTextes();
        if (mode == VERIFIER && Verrou.bioActive(this) && Bio.disponible(this)) {
            demanderEmpreinte();
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private View construire() {
        LinearLayout racine = new LinearLayout(this);
        racine.setOrientation(LinearLayout.VERTICAL);
        racine.setGravity(Gravity.CENTER_HORIZONTAL);
        racine.setBackgroundResource(R.drawable.bg_header);
        racine.setPadding(dp(24), dp(56), dp(24), dp(24));
        // le dégradé de bg_header a des coins arrondis en bas : on prend la couleur pleine à la place
        racine.setBackgroundColor(0xFF4F46E5);

        titre = new TextView(this);
        titre.setTextColor(Color.WHITE);
        titre.setTextSize(24);
        titre.setTypeface(titre.getTypeface(), android.graphics.Typeface.BOLD);
        titre.setGravity(Gravity.CENTER);
        racine.addView(titre);

        message = new TextView(this);
        message.setTextColor(0xFFE0E7FF);
        message.setTextSize(14);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, dp(6), 0, dp(26));
        racine.addView(message);

        LinearLayout rangPoints = new LinearLayout(this);
        rangPoints.setGravity(Gravity.CENTER);
        for (int i = 0; i < LONGUEUR; i++) {
            View p = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(16), dp(16));
            lp.setMargins(dp(10), 0, dp(10), 0);
            p.setLayoutParams(lp);
            points[i] = p;
            rangPoints.addView(p);
        }
        racine.addView(rangPoints);

        View espace = new View(this);
        espace.setLayoutParams(new LinearLayout.LayoutParams(1, 0, 1f));
        racine.addView(espace);

        String[][] touches = {{"1", "2", "3"}, {"4", "5", "6"}, {"7", "8", "9"}, {"bio", "0", "<"}};
        for (String[] rang : touches) {
            LinearLayout l = new LinearLayout(this);
            l.setGravity(Gravity.CENTER);
            for (String t : rang) l.addView(touche(t));
            racine.addView(l);
        }

        if (mode == VERIFIER) {
            TextView oubli = new TextView(this);
            oubli.setText(T.t("pin_oublie"));
            oubli.setTextColor(0xFFE0E7FF);
            oubli.setTextSize(14);
            oubli.setGravity(Gravity.CENTER);
            oubli.setPadding(dp(16), dp(14), dp(16), dp(4));
            oubli.setOnClickListener(v -> oublie());
            racine.addView(oubli);
        } else {
            TextView annuler = new TextView(this);
            annuler.setText(T.t("btn_annuler"));
            annuler.setTextColor(0xFFE0E7FF);
            annuler.setTextSize(15);
            annuler.setGravity(Gravity.CENTER);
            annuler.setPadding(dp(16), dp(14), dp(16), dp(4));
            annuler.setOnClickListener(v -> finish());
            racine.addView(annuler);
        }
        return racine;
    }

    private View touche(final String t) {
        TextView v = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(72), dp(72));
        lp.setMargins(dp(10), dp(6), dp(10), dp(6));
        v.setLayoutParams(lp);
        v.setGravity(Gravity.CENTER);
        v.setTextColor(Color.WHITE);
        v.setTextSize(26);
        if (t.equals("bio")) {
            boolean dispo = mode == VERIFIER && Verrou.bioActive(this) && Bio.disponible(this);
            if (dispo) {
                v.setText("☝");
                v.setOnClickListener(x -> demanderEmpreinte());
            } else {
                v.setVisibility(View.INVISIBLE);
            }
            return v;
        }
        v.setBackgroundResource(R.drawable.bg_btn_header);
        v.setClickable(true);
        v.setFocusable(true);
        if (t.equals("<")) {
            v.setText("⌫");
            v.setOnClickListener(x -> {
                if (saisie.length() > 0) saisie.deleteCharAt(saisie.length() - 1);
                majPoints();
            });
        } else {
            v.setText(t);
            v.setOnClickListener(x -> ajouter(t));
        }
        return v;
    }

    private void majTextes() {
        boolean saisieAncien = (mode == CHANGER || mode == RETIRER) && !ancienOk;
        if (mode == VERIFIER || saisieAncien) {
            titre.setText(T.t(mode == VERIFIER ? "pin_titre_verifier" : "pin_titre_ancien"));
            message.setText(T.t(mode == VERIFIER ? "pin_msg_verifier" : "pin_msg_ancien"));
        } else if (etape == 0) {
            titre.setText(T.t("pin_titre_creer"));
            message.setText(T.t("pin_msg_creer"));
        } else {
            titre.setText(T.t("pin_titre_confirmer"));
            message.setText(T.t("pin_msg_confirmer"));
        }
        majPoints();
    }

    private void majPoints() {
        for (int i = 0; i < LONGUEUR; i++) {
            View p = points[i];
            android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
            g.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            if (i < saisie.length()) {
                g.setColor(Color.WHITE);
            } else {
                g.setColor(0x00000000);
                g.setStroke(dp(2), Color.WHITE);
            }
            p.setBackground(g);
        }
    }

    private void ajouter(String c) {
        if (saisie.length() >= LONGUEUR) return;
        saisie.append(c);
        majPoints();
        if (saisie.length() == LONGUEUR) {
            final String code = saisie.toString();
            saisie.setLength(0);
            getWindow().getDecorView().postDelayed(() -> valider(code), 120);
        }
    }

    private void erreur(String cle) {
        Toast.makeText(this, T.t(cle), Toast.LENGTH_SHORT).show();
        titre.animate().translationX(dp(14)).setDuration(60).withEndAction(() ->
                titre.animate().translationX(0).setDuration(60).start()).start();
        majPoints();
    }

    private void valider(String code) {
        boolean saisieAncien = (mode == CHANGER || mode == RETIRER) && !ancienOk;
        if (mode == VERIFIER || saisieAncien) {
            if (!Verrou.verifier(this, code)) {
                erreur("pin_incorrect");
                return;
            }
            if (mode == VERIFIER) {
                reussi();
            } else if (mode == RETIRER) {
                Verrou.retirer(this);
                Toast.makeText(this, T.t("toast_pin_retire"), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                ancienOk = true;
                majTextes();
            }
            return;
        }
        // création / confirmation
        if (etape == 0) {
            premier = code;
            etape = 1;
            majTextes();
        } else if (premier.equals(code)) {
            Verrou.definir(this, code);
            Toast.makeText(this, T.t("toast_pin_defini"), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            premier = "";
            etape = 0;
            erreur("pin_different");
            majTextes();
        }
    }

    private void reussi() {
        Verrou.deverrouille = true;
        Verrou.dernierArret = System.currentTimeMillis();
        finish();
    }

    private void demanderEmpreinte() {
        Bio.demander(this, T.t("bio_sous_titre"), this::reussi, null);
    }

    private void oublie() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(T.t("pin_oublie_titre"))
                .setMessage(T.t("pin_oublie_message"))
                .setPositiveButton(T.t("btn_supprimer"), (d, w) -> {
                    Store.vider(this);
                    Verrou.retirer(this);
                    Toast.makeText(this, T.t("toast_vide"), Toast.LENGTH_SHORT).show();
                    reussi();
                })
                .setNegativeButton(T.t("btn_annuler"), null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (mode == VERIFIER) {
            moveTaskToBack(true); // pas de contournement du code
        } else {
            super.onBackPressed();
        }
    }
}
