package mg.varotra;

import android.content.Context;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

/** Empreinte digitale (BiometricPrompt). */
public class Bio {

    public static boolean disponible(Context c) {
        return BiometricManager.from(c).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /** succes : empreinte reconnue ; fin : annulé / erreur (l'utilisateur garde le code PIN). */
    public static void demander(FragmentActivity a, String sousTitre, final Runnable succes, final Runnable fin) {
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(T.t("bio_titre"))
                .setSubtitle(sousTitre)
                .setNegativeButtonText(T.t("bio_utiliser_pin"))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();
        BiometricPrompt prompt = new BiometricPrompt(a, ContextCompat.getMainExecutor(a),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        succes.run();
                    }

                    @Override
                    public void onAuthenticationError(int code, CharSequence msg) {
                        if (fin != null) fin.run();
                    }
                });
        prompt.authenticate(info);
    }
}
