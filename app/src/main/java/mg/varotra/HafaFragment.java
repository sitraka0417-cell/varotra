package mg.varotra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Sauvegarde / restauration (fichier JSON), effacement des données, version. */
public class HafaFragment extends Fragment {

    private static final int REQ_EXPORT = 41;
    private static final int REQ_IMPORT = 42;

    private TextView pinEtat;
    private View btnPin, btnPinRetirer;
    private CompoundButton switchBio;
    private boolean majEnCours = false;

    @Override
    public void onResume() {
        super.onResume();
        majSecurite();
    }

    private void majSecurite() {
        if (pinEtat == null) return;
        boolean actif = Verrou.actif(requireContext());
        pinEtat.setText(T.t(actif ? "hafa_pin_actif" : "hafa_pin_inactif"));
        ((com.google.android.material.button.MaterialButton) btnPin).setText(
                T.t(actif ? "hafa_pin_changer" : "hafa_pin_definir"));
        btnPinRetirer.setVisibility(actif ? View.VISIBLE : View.GONE);
        switchBio.setVisibility(actif ? View.VISIBLE : View.GONE);
        majEnCours = true;
        switchBio.setChecked(Verrou.bioActive(requireContext()));
        majEnCours = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle b) {
        View r = inflater.inflate(R.layout.fragment_hafa, container, false);
        T.habiller(r);

        r.findViewById(R.id.btn_export).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/json");
            i.putExtra(Intent.EXTRA_TITLE, T.t("backup_nom_fichier"));
            startActivityForResult(i, REQ_EXPORT);
        });
        r.findViewById(R.id.btn_import).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(i, REQ_IMPORT);
        });
        r.findViewById(R.id.btn_vider).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(T.t("vider_titre"))
                        .setMessage(T.t("vider_message"))
                        .setPositiveButton(T.t("btn_supprimer"), (d, w) -> {
                            Store.vider(requireContext());
                            Toast.makeText(requireContext(), T.t("toast_vide"), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(T.t("btn_annuler"), null)
                        .show());

        pinEtat = r.findViewById(R.id.pin_etat);
        btnPin = r.findViewById(R.id.btn_pin);
        btnPinRetirer = r.findViewById(R.id.btn_pin_retirer);
        switchBio = r.findViewById(R.id.switch_bio);
        btnPin.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), PinActivity.class);
            i.putExtra("mode", Verrou.actif(requireContext()) ? PinActivity.CHANGER : PinActivity.CREER);
            startActivity(i);
        });
        btnPinRetirer.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), PinActivity.class);
            i.putExtra("mode", PinActivity.RETIRER);
            startActivity(i);
        });
        switchBio.setOnCheckedChangeListener((CompoundButton b2, boolean on) -> {
            if (majEnCours) return;
            if (on && !Bio.disponible(requireContext())) {
                Toast.makeText(requireContext(), T.t("toast_bio_indisponible"), Toast.LENGTH_LONG).show();
                majEnCours = true;
                switchBio.setChecked(false);
                majEnCours = false;
                return;
            }
            Verrou.setBio(requireContext(), on);
        });

        String version = "";
        try {
            version = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }
        ((TextView) r.findViewById(R.id.version)).setText(T.t("hafa_version", version));
        majSecurite();
        return r;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != android.app.Activity.RESULT_OK || data == null || data.getData() == null) return;
        final Uri uri = data.getData();
        if (requestCode == REQ_EXPORT) {
            try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri, "wt")) {
                if (out == null) throw new java.io.IOException();
                out.write(Store.exporter(requireContext()).getBytes(StandardCharsets.UTF_8));
                Toast.makeText(requireContext(), T.t("toast_export_ok"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), T.t("toast_export_echec"), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQ_IMPORT) {
            final String json;
            try (InputStream in = requireContext().getContentResolver().openInputStream(uri)) {
                if (in == null) throw new java.io.IOException();
                json = Store.lire(in);
            } catch (Exception e) {
                Toast.makeText(requireContext(), T.t("toast_import_echec"), Toast.LENGTH_LONG).show();
                return;
            }
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(T.t("import_titre"))
                    .setMessage(T.t("import_message"))
                    .setPositiveButton(T.t("btn_remplacer"), (d, w) -> {
                        boolean ok = Store.importer(requireContext(), json);
                        Toast.makeText(requireContext(),
                                ok ? T.t("toast_import_ok") : T.t("toast_import_echec"), Toast.LENGTH_LONG).show();
                    })
                    .setNegativeButton(T.t("btn_annuler"), null)
                    .show();
        }
    }
}
