package mg.varotra;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

/** Toutes les pages héritent de celle-ci : elle affiche l'écran du code PIN si nécessaire. */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
        if (Verrou.doitVerrouiller(this)) {
            Verrou.deverrouille = false;
            Intent i = new Intent(this, PinActivity.class);
            i.putExtra("mode", PinActivity.VERIFIER);
            startActivity(i);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isChangingConfigurations() && Verrou.deverrouille) {
            Verrou.dernierArret = System.currentTimeMillis();
        }
    }
}
