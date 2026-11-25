package mg.varotra;

import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.nav);
        Menu m = nav.getMenu();
        m.add(Menu.NONE, R.id.nav_jour, 0, T.t("nav_jour")).setIcon(R.drawable.ic_calendar);
        m.add(Menu.NONE, R.id.nav_tatitra, 1, T.t("nav_tatitra")).setIcon(R.drawable.ic_chart);
        m.add(Menu.NONE, R.id.nav_trosa, 2, T.t("nav_trosa")).setIcon(R.drawable.ic_person);
        m.add(Menu.NONE, R.id.nav_hafa, 3, T.t("nav_hafa")).setIcon(R.drawable.ic_more);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f;
            if (id == R.id.nav_jour) f = new HomeFragment();
            else if (id == R.id.nav_tatitra) f = new TatitraFragment();
            else if (id == R.id.nav_trosa) f = new TrosaFragment();
            else f = new HafaFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.conteneur, f).commit();
            return true;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_jour);
        }
    }
}
