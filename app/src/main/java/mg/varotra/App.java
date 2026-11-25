package mg.varotra;

import android.app.Application;

/** Charge tous les textes (assets/teny.txt) au démarrage de l'application. */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        T.charger(this);
    }
}
