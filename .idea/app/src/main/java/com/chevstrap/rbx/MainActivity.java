package com.chevstrap.rbx;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chevstrap.rbx.AppDirectories.RobloxClientData;
import com.chevstrap.rbx.Extensions.CustomUIComponents;
import com.chevstrap.rbx.UI.Frontend;
import com.chevstrap.rbx.Utility.FileTool;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private TextView textview_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        try {
            setContentView(R.layout.main);
            initialize();
            initializeLogic();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CustomWatcher.getInstance().Dispose();
    }

    private void initialize() {
        App.onStartup();

        LinearLayout linear5 = findViewById(R.id.linear5);
        CustomUIComponents.ButtonResult buttonLaunchrbx = CustomUIComponents.addButton(getApplicationContext(), App.getTextLocale(getApplicationContext(), R.string.launchmenu_launchroblox), "", linear5, null, null);
        CustomUIComponents.ButtonResult buttonConfiguresettings = CustomUIComponents.addButton(getApplicationContext(), App.getTextLocale(getApplicationContext(), R.string.launchmenu_configuresettings), "", linear5, null, null);
        CustomUIComponents.ButtonResult buttonWiki = CustomUIComponents.addButton(getApplicationContext(), App.getTextLocale(getApplicationContext(), R.string.launchmenu_wiki), "", linear5, "https://github.com/FrosSky/Chevstrap/wiki", "link");

        linear5.addView(buttonLaunchrbx.buttonView);
        linear5.addView(buttonConfiguresettings.buttonView);
        linear5.addView(buttonWiki.buttonView);
        textview_version = findViewById(R.id.textview_version);

        if (buttonLaunchrbx.buttonOne != null) {
            buttonLaunchrbx.buttonOne.setOnClickListener(v -> {
                ChevstrapLauncher chevstrapLauncher = new ChevstrapLauncher();
                chevstrapLauncher.setFragmentManager(getSupportFragmentManager());
                try {
                    chevstrapLauncher.Run();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        if (buttonConfiguresettings.buttonOne != null) {
            buttonConfiguresettings.buttonOne.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void initializeLogic() {
        try {
            textview_version.setText(getApplicationContext().getPackageManager()
                    .getPackageInfo(getApplicationContext().getPackageName(), 0).versionName);
        } catch (Exception ignored) {}
        checkFirst();
    }

    public void checkFirst() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long blockSize = stat.getBlockSizeLong();
        double freeGB = (stat.getAvailableBlocksLong() * blockSize) / (1024.0 * 1024.0 * 1024.0);

        if (freeGB < 0.6) {
            Frontend.ShowMessageBoxWithRunnable(
                    this,
                    App.getTextLocale(App.getAppContext(), R.string.dialog_device_full_storage),
                    false,
                    this::finish,
                    null
            );
            return;
        }

        RobloxClientData robloxData;
        String rbxPath;
        boolean success = false;

        String[] appTypes = {"global", "vng"};

        for (String type : appTypes) {
            App.getChevstrapSettings().setPreferredRobloxApp(type);
            robloxData = new RobloxClientData();
            rbxPath = robloxData.getRobloxDirectory();

            try {
                File[] logsList = FileTool.listFiles(new File(rbxPath));
                if (logsList.length > 0) {
                    success = true;
                    break;
                }
            } catch (Exception ignored) {}
        }

        if (!success) {
            Frontend.ShowMessageBoxWithRunnable(
                    this,
                    App.getTextLocale(App.getAppContext(), R.string.dialog_access_to_roblox_directories_is_blocked)
                            + " https://github.com/FrosSky/Chevstrap/wiki/Access-to-Roblox-directories-is-blocked",
                    false,
                    this::finish,
                    null
            );
        }
    }
}
