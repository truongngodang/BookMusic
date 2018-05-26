package io.berrycorp.bookmusic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Field;

import io.berrycorp.bookmusic.adapter.SongAdapter;
import io.berrycorp.bookmusic.utils.Constant;
import io.berrycorp.bookmusic.utils.FragmentHelper;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = StartActivity.class.getSimpleName();


    // Controls
    private BottomNavigationView bnvMenu;
    private int mContextBefore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        addControls();
        addEvents();
    }


    private void addControls() {
        bnvMenu = findViewById(R.id.bnv_menu);
        disableShiftMode(bnvMenu);

        // Process variable Intent
        mContextBefore = getIntent().getIntExtra("KEY_ACTIVITY", 0);
        if (mContextBefore == Constant.BOOK_LINE_ACTIVITY || mContextBefore == Constant.BOOK_SINGER_ACTIVITY || mContextBefore == Constant.NOTIFICATION_ACTIVITY) {
            FragmentHelper.startFragment(getSupportFragmentManager(), PlayerFragment.newInstance("PLAYER"));
            bnvMenu.setSelectedItemId(R.id.action_player);
        } else {
            FragmentHelper.startFragment(getSupportFragmentManager(), HomeFragment.newInstance("HOME"));
            bnvMenu.setSelectedItemId(R.id.action_home);
        }
    }

    private void addEvents() {

        bnvMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_home : {
                        FragmentHelper.startFragment(getSupportFragmentManager(), HomeFragment.newInstance("HOME"));
                        break;
                    }
                    case R.id.action_player : {
                        FragmentHelper.startFragment(getSupportFragmentManager(), PlayerFragment.newInstance("PLAYER"));
                        break;
                    }
                }

                return true;
            }
        });

    }

    @SuppressLint("RestrictedApi")
    private void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Unable to get shift mode field");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unable to change value of shift mode");
        }
    }
}
