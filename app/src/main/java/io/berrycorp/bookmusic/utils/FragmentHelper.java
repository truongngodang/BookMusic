package io.berrycorp.bookmusic.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import io.berrycorp.bookmusic.R;

public class FragmentHelper {
    public static void startFragment(FragmentManager manager, Fragment fragment) {
        final FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_body, fragment).commit();
    }
}
