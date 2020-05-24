package com.example.practicaltouch.ui.main;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.practicaltouch.MainActivity;
import com.example.practicaltouch.R;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SecondFragment extends Fragment {

    private LinearLayout appTray;
    private PackageManager packageManager;
    private Set<ResolveInfo> addedApp = new LinkedHashSet<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_createnew_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button launchButton = Objects.requireNonNull(getView()).findViewById(R.id.launchButton);
        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!addedApp.isEmpty()) {
                    appTray.removeAllViews();
                    ((MainActivity) Objects.requireNonNull(getActivity())).start_stop(addedApp);
                    addedApp.clear();
                    Toast.makeText(getActivity(), "App launched!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Please select at least an application.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        appTray = Objects.requireNonNull(getView()).findViewById(R.id.myLinearLayout);
        GridView appDrawer = getView().findViewById(R.id.myGrid);
        packageManager = Objects.requireNonNull(getActivity()).getPackageManager();

        final List<ResolveInfo> installedAppsList = getLaunchableApps();
        appDrawer.setAdapter(new AppAdapter(getActivity(), installedAppsList, packageManager));

        Point screenSize = new Point();
        appDrawer.getDisplay().getSize(screenSize);
        appDrawer.setNumColumns(screenSize.x/162);

        appDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ResolveInfo current = installedAppsList.get(position);
                if(!addedApp.contains(current)) {
                    Drawable icon = packageManager.getApplicationIcon(current.activityInfo.applicationInfo);
                    LayoutInflater inflater = getLayoutInflater();
                    final ImageView view2 = (ImageView) inflater.inflate(R.layout.appicon, parent, false);
                    view2.setImageDrawable(icon);
                    view2.setPadding(16, 8, 16, 8);
                    appTray.addView(view2);
                    view2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            appTray.removeView(view2);
                            addedApp.remove(current);
                        }
                    });
                    addedApp.add(current);
                }
            }
        });
    }

    private List<ResolveInfo> getLaunchableApps() {
        return packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    private List<PackageInfo> getInstalledApps() {
        List<PackageInfo> packageList = packageManager
                .getInstalledPackages(PackageManager.GET_PERMISSIONS);

        final List<PackageInfo> packageList1 = new ArrayList<>();

        /*To filter out System apps*/
        for(PackageInfo pi : packageList) {
            if(!isSystemPackage(pi)){
                packageList1.add(pi);
            }
        }

        return packageList1;
    }

    private boolean isListEmpty() {
        return appTray.getChildCount() == 0;
    }
}
