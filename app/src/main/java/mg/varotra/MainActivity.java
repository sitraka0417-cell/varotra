package mg.varotra;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Les 4 pages (Andro, Tatitra, Trosa, Hafa) : on peut glisser à gauche / droite ou toucher le menu. */
public class MainActivity extends BaseActivity {

    private static final int[] IDS = {R.id.nav_jour, R.id.nav_tatitra, R.id.nav_trosa, R.id.nav_hafa};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ViewPager2 pager = findViewById(R.id.pager);
        final BottomNavigationView nav = findViewById(R.id.nav);
        Menu m = nav.getMenu();
        m.add(Menu.NONE, R.id.nav_jour, 0, T.t("nav_jour")).setIcon(R.drawable.ic_calendar);
        m.add(Menu.NONE, R.id.nav_tatitra, 1, T.t("nav_tatitra")).setIcon(R.drawable.ic_chart);
        m.add(Menu.NONE, R.id.nav_trosa, 2, T.t("nav_trosa")).setIcon(R.drawable.ic_person);
        m.add(Menu.NONE, R.id.nav_hafa, 3, T.t("nav_hafa")).setIcon(R.drawable.ic_more);

        pager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) return new HomeFragment();
                if (position == 1) return new TatitraFragment();
                if (position == 2) return new TrosaFragment();
                return new HafaFragment();
            }

            @Override
            public int getItemCount() {
                return 4;
            }
        });

        nav.setOnItemSelectedListener(item -> {
            for (int i = 0; i < IDS.length; i++) {
                if (IDS[i] == item.getItemId() && pager.getCurrentItem() != i) {
                    pager.setCurrentItem(i, true);
                }
            }
            return true;
        });
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                nav.setSelectedItemId(IDS[position]);
            }
        });
    }
}
