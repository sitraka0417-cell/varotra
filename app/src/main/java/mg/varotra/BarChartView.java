package mg.varotra;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** Petit graphique en barres (tombombarotra par période) ; barres rouges si négatif. */
public class BarChartView extends View {
    private String[] labels = new String[0];
    private double[] valeurs = new double[0];
    private final Paint pPos = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pNeg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pAxe = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTexte = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float d;

    public BarChartView(Context c, AttributeSet a) {
        super(c, a);
        d = getResources().getDisplayMetrics().density;
        pPos.setColor(0xFF4F46E5);
        pNeg.setColor(0xFFE11D48);
        pAxe.setColor(0xFFE5E7EB);
        pAxe.setStrokeWidth(d);
        pTexte.setColor(0xFF6B7280);
        pTexte.setTextSize(10 * d);
        pTexte.setTextAlign(Paint.Align.CENTER);
    }

    public void setDonnees(String[] labels, double[] valeurs) {
        this.labels = labels;
        this.valeurs = valeurs;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = valeurs.length;
        if (n == 0) return;
        float w = getWidth(), h = getHeight();
        float basLabels = 18 * d;
        float haut = 6 * d;
        float zoneH = h - basLabels - haut;
        double max = 0, min = 0;
        for (double v : valeurs) {
            if (v > max) max = v;
            if (v < min) min = v;
        }
        double plage = max - min;
        if (plage <= 0) plage = 1;
        float yZero = haut + (float) (max / plage) * zoneH;
        canvas.drawLine(0, yZero, w, yZero, pAxe);

        float slot = w / n;
        float bw = Math.min(slot * 0.62f, 40 * d);
        int pas = Math.max(1, (int) Math.ceil(n / 8.0));
        for (int i = 0; i < n; i++) {
            float cx = slot * i + slot / 2;
            float y = haut + (float) ((max - valeurs[i]) / plage) * zoneH;
            RectF r = new RectF(cx - bw / 2, Math.min(y, yZero), cx + bw / 2, Math.max(y, yZero));
            if (Math.abs(valeurs[i]) > 0.0001 && r.height() < 2 * d) r.set(r.left, yZero - d, r.right, yZero + d);
            canvas.drawRoundRect(r, 3 * d, 3 * d, valeurs[i] >= 0 ? pPos : pNeg);
            if (i % pas == 0 && i < labels.length) {
                canvas.drawText(labels[i], cx, h - 4 * d, pTexte);
            }
        }
    }
}
