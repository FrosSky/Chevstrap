package com.chevstrap.rbx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.*;
import android.util.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.widget.Toast;
import android.widget.VideoView;

import com.chevstrap.rbx.UI.Elements.Settings.Pages.AboutOneFragment;
import com.chevstrap.rbx.UI.Elements.Settings.Pages.BehaviourFragment;
import com.chevstrap.rbx.UI.Elements.Settings.Pages.IntegrationsFragment;
import com.chevstrap.rbx.UI.Elements.Settings.Pages.LocalPreferencesFragment;
import com.chevstrap.rbx.UI.Frontend;
import com.chevstrap.rbx.UI.Elements.Settings.Pages.ChevstrapFragment;
import com.chevstrap.rbx.Utility.FileTool;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {
	private String currentPage = null;
    private LinearLayout linear20;
	private LinearLayout linear27;
    private TextView textview5;
	private LinearLayout button_save;
	private LinearLayout button_saveandlaunch;
    private LinearLayout button_close;
	private Map<String, Object> oriProp = null;
	private final List<ButtonClickListener> buttonClickListener = new ArrayList<>();

	private interface ButtonClickListener {
		void onQueryChanged(String query);
	}

    @Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		setContentView(R.layout.settings);

		App.setSavedSettingsActivity(this);
		try {
			initialize();
			initializeLogic();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				showMessageBoxUnsavedChanges();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CustomWatcher.getInstance().Dispose();
	}

	@SuppressLint("SourceLockedOrientationActivity")
	private void initialize() {
        LinearLayout linear1 = findViewById(R.id.linear1);
        LinearLayout linear_dark = findViewById(R.id.linear_dark);
		LinearLayout linear_background = findViewById(R.id.linear_background);

        LinearLayout linear24 = findViewById(R.id.linear24);
        LinearLayout linear024 = findViewById(R.id.linear024);
        LinearLayout linear3 = findViewById(R.id.linear3);
        LinearLayout linear25 = findViewById(R.id.linear25);
        LinearLayout linear22 = findViewById(R.id.linear22);
		linear27 = findViewById(R.id.linear27);
        LinearLayout linear10 = findViewById(R.id.linear10);
        TextView textview1 = findViewById(R.id.textview1);
        ScrollView vscroll1 = findViewById(R.id.vscroll1);

        LinearLayout linear26 = findViewById(R.id.linear26);
		linear20 = findViewById(R.id.linear20);
        TextView textview3 = findViewById(R.id.textview3);
		textview5 = findViewById(R.id.textview5);
		button_save = findViewById(R.id.button_save);
		button_saveandlaunch = findViewById(R.id.button_saveandlaunch);
		button_close = findViewById(R.id.button_close);

		Installer installer = new Installer();
		installer.HandleUpgrades();

		GradientDrawable activeBg = new GradientDrawable();
		activeBg.setCornerRadius(20f);

		boolean isDark = Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark");
		String videoUri = App.getChevstrapSettings().getDisplayVideoThemeWithUri();
		boolean useTransparent = !videoUri.isEmpty();

		if (isDark) {
			activeBg.setColor(useTransparent ? Color.parseColor("#4D101010") : Color.parseColor("#101010"));
			activeBg.setStroke(2, useTransparent ? Color.parseColor("#19181818") : Color.parseColor("#181818"));
			linear_background.setBackgroundResource(R.drawable.background_normal);
		} else {
			activeBg.setColor(useTransparent ? Color.parseColor("#4DFFFFFF") : Color.parseColor("#FFFFFF"));
			activeBg.setStroke(2, useTransparent ? Color.parseColor("#19E3E8EC") : Color.parseColor("#E3E8EC"));
			linear_background.setBackgroundResource(R.drawable.background_light);
		}

		linear_dark.setBackground(activeBg);
		if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "light")) {
			textview5.setTextColor(Color.parseColor("#000000"));
			textview1.setTextColor(Color.parseColor("#000000"));
			linear024.setBackgroundColor(Color.parseColor("#E3E8EC"));
		} else if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark")) {
			textview5.setTextColor(Color.parseColor("#FFFFFF"));
			textview1.setTextColor(Color.parseColor("#FFFFFF"));
			linear024.setBackgroundColor(Color.parseColor("#181818"));
		}

		if (App.getChevstrapSettings().isForcePotraitMode()) {
			textview1.setVisibility(View.GONE);
			linear22.setOrientation(LinearLayout.VERTICAL);
		} else {
			textview1.setVisibility(View.VISIBLE);
			linear22.setOrientation(LinearLayout.HORIZONTAL);
		}

		addButton(this, getTextLocale(App.getAppContext(), R.string.menu_integrations_title), R.drawable.integrations, R.drawable.integrations_on, R.drawable.integrations_light, "Integrations", linear27);
//		addButton(this, getTextLocale(App.getAppContext(), R.string.menu_fastflags_title), R.drawable.fastflag_icon, R.drawable.fastflag_icon_on, R.drawable.fastflag_icon_light, "FlagsSettings", linear27);
//		addButton(this, getTextLocale(App.getAppContext(), R.string.menu_fastflageditor_title), R.drawable.editor_icon, R.drawable.editor_icon_on, R.drawable.editor_icon_light,  "FlagsEditor", linear27);
		addButton(this, getTextLocale(App.getAppContext(), R.string.menu_devicepreferences_title), R.drawable.deviceprefs, R.drawable.deviceprefs_on, R.drawable.deviceprefs_light,  "LocalPreferences", linear27);
		addButton(this, getTextLocale(App.getAppContext(), R.string.menu_behaviour_title), R.drawable.bootstrapper_icon, R.drawable.bootstrapper_icon_on, R.drawable.bootstrapper_icon_light,  "Launcher", linear27);
		addDivider(this, linear27);
		addButton(this, getTextLocale(App.getAppContext(), R.string.common_chevstrap), R.drawable.setting_icon, R.drawable.setting_icon_on, R.drawable.setting_icon_light, "Settings", linear27);
		addButton(this, getTextLocale(App.getAppContext(), R.string.about_title), R.drawable.about_icon, R.drawable.about_icon_on, R.drawable.about_icon_light, "About", linear27);

		button_close.setOnClickListener(_view -> {
			showMessageBoxUnsavedChanges();
		});

		button_saveandlaunch.setOnClickListener(_view -> {
			ChevstrapLauncher chevstrapLauncher = new ChevstrapLauncher();
			chevstrapLauncher.setFragmentManager(getSupportFragmentManager());
			try {
				App.getConfig().save();
				chevstrapLauncher.Run();
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
        });

		button_save.setOnClickListener(_view -> {
			App.getConfig().save();
		});

		String savedUriString = App.getChevstrapSettings().getDisplayVideoThemeWithUri();
		if (!savedUriString.isEmpty()) {
			Uri savedUri = Uri.parse(savedUriString);
			playVideo(savedUri);
		}
		//if (savedUriString.isEmpty()) {
			//Toast.makeText(this, "No URI saved", Toast.LENGTH_SHORT).show();
			//return;
		//}

		//Toast.makeText(this, "Saved URI: " + savedUriString, Toast.LENGTH_SHORT).show();
	}

	@SuppressLint("SourceLockedOrientationActivity")
	private void initializeLogic() {
		AstyleButtonBTeal1(button_save);
		AstyleButtonBlack1(button_close);
		AstyleButtonBTeal1(button_saveandlaunch);

		movePage("Integrations");
		oriProp = ConfigManager.getInstance().getProp();

		ViewGroup.LayoutParams params = linear27.getLayoutParams();
		if (App.getChevstrapSettings().isForcePotraitMode()) {
			params.width = 200;
		} else {
			params.width = 400;
		}
		linear27.setLayoutParams(params);

		if (App.getChevstrapSettings().isForcePotraitMode()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}
	}

	public static void addDivider(Context context, LinearLayout parent) {
		View divider = new View(context);

		int heightPx = (int) (1 * context.getResources().getDisplayMetrics().density + 0.5f);
		LinearLayout.LayoutParams layoutParams = getLayoutParams(context, heightPx);
		divider.setLayoutParams(layoutParams);

		if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "light")) {
			divider.setBackgroundColor(Color.parseColor("#BCC9D3"));
		} else if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark")) {
			divider.setBackgroundColor(Color.parseColor("#323232"));
		}
		parent.addView(divider);
	}

	private static LinearLayout.LayoutParams getLayoutParams(Context context, int heightPx) {
		int marginLeft = (int) (10 * context.getResources().getDisplayMetrics().density + 0.5f);
		int marginTop = (int) (10 * context.getResources().getDisplayMetrics().density + 0.5f);
		int marginRight = (int) (10 * context.getResources().getDisplayMetrics().density + 0.5f);
		int marginBottom = (int) (10 * context.getResources().getDisplayMetrics().density + 0.5f);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				heightPx
		);
		layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		return layoutParams;
	}

	public void addButton(Context context, String name, int IconResId, int IconResId1, int IconResId_light, String MovePageTo, LinearLayout parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View buttonView = inflater.inflate(R.layout.button_menu_settingspage, parent, false);

		LinearLayout container = buttonView.findViewById(R.id.button_option);
		TextView nameView = buttonView.findViewById(R.id.textview_name);
		ImageView imageView = buttonView.findViewById(R.id.imageview);

		imageView.setBackgroundResource(IconResId);

		nameView.setText(name);
		container.setOnClickListener(v -> {
			movePage(MovePageTo);
		});

		if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "light")) {
			nameView.setTextColor(Color.parseColor("#000000"));
		} else if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark")) {
			nameView.setTextColor(Color.parseColor("#FFFFFF"));
		}

		nameView.setVisibility(
				App.getChevstrapSettings().isForcePotraitMode()
						? View.GONE
						: View.VISIBLE
		);

		boolean isDark = Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark");
		imageView.setBackgroundResource(isDark ? IconResId : IconResId_light);

		AstyleButtonTRANSPARENT(container);
		buttonClickListener.add(query -> {
			if (!Objects.equals(query, MovePageTo)) {
				AstyleButtonTRANSPARENT(container);
				if (!App.getChevstrapSettings().isForcePotraitMode()) {
					nameView.setVisibility(View.VISIBLE);
				}

				if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "light")) {
					nameView.setTextColor(Color.parseColor("#000000"));
					imageView.setBackgroundResource(IconResId_light);
				} else if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark")) {
					nameView.setTextColor(Color.parseColor("#FFFFFF"));
					imageView.setBackgroundResource(IconResId);
				}

				container.setPadding(0, 15, 0, 15);
			} else {
				nameView.setVisibility(View.GONE);
				GradientDrawable activeBg = new GradientDrawable();
				activeBg.setCornerRadius(20);
				activeBg.setStroke(0, Color.TRANSPARENT);

				if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark")) {
					activeBg.setColor(Color.parseColor("#151515"));
				} else if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "light")) {
					activeBg.setColor(Color.parseColor("#FFFFFF"));
				}

				imageView.setBackgroundResource(IconResId1);
				container.setBackground(activeBg);
				container.setPadding(0, 53, 0, 53);
			}
		});

		parent.addView(buttonView);
	}

	public void movePage(String whatpage) {
		if (whatpage.equals(currentPage)) {
			return;
		}

		for (ButtonClickListener listener : buttonClickListener) {
			listener.onQueryChanged(whatpage);
		}

		Fragment fragment = null;
		String title = null;

		try {
			switch (whatpage) {
				case "Integrations":
					fragment = new IntegrationsFragment();
					title = App.getTextLocale(App.getAppContext(), R.string.menu_integrations_title);
					break;
				case "Launcher":
					fragment = new BehaviourFragment();
					title = App.getTextLocale(App.getAppContext(), R.string.menu_behaviour_title);
					break;
				case "Settings":
					fragment = new ChevstrapFragment();
					title = App.getTextLocale(App.getAppContext(), R.string.common_chevstrap);
					break;
				case "LocalPreferences":
					fragment = new LocalPreferencesFragment();
					title = App.getTextLocale(App.getAppContext(), R.string.menu_devicepreferences_title);
					break;
				case "About":
					fragment = new AboutOneFragment();
					title = App.getTextLocale(App.getAppContext(), R.string.about_title);
					break;
				default:
					throw new IllegalArgumentException("Unknown page: " + whatpage);
			}
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		if (fragment != null) {
			currentPage = whatpage;
			textview5.setText(title);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.linear20, fragment)
					.commit();
			fadeIn(linear20);
			animateTranslationY(linear20);
		}
	}

	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}

	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}

	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}

	private void animateTranslationY(View view) {
		view.setTranslationY(50f);
		view.animate()
				.translationY(0f)
				.setDuration(300)
				.start();
	}

	public void fadeIn(View view) {
		if (view != null) {
			view.setAlpha(0f);
			view.setVisibility(View.VISIBLE);
			view.animate()
					.alpha(1f)
					.setDuration(300)
					.start();
		}
	}

	public void showMessageBoxUnsavedChanges() {
		File clientSettingsDirLocation = new File(String.valueOf(App.getConfig().getFileLocation()));
		JSONObject savedConfigJson = null;

		try {
			if (!FileTool.isExist(String.valueOf(clientSettingsDirLocation))) {
				savedConfigJson = new JSONObject(oriProp);
			} else {
				String fileContent = FileTool.read(clientSettingsDirLocation);
                savedConfigJson = new JSONObject(fileContent);
			}
		} catch (Exception ignored) {}

		String currentConfig = null;
		String savedConfig = savedConfigJson != null ? savedConfigJson.toString() : "";

		try {
			JSONObject root = new JSONObject(App.getConfig().getProp());
			currentConfig = root.toString();
		} catch (Exception e) {
			App.getLogger().writeException("SettingsActivity::showMessageBoxUnsavedChanges", e);
		}

		assert currentConfig != null;
		if (!FileTool.isExist(String.valueOf(clientSettingsDirLocation)) || !currentConfig.equals(savedConfig)) {
			Frontend.ShowMessageBoxWithRunnable(
					App.getSavedFragmentActivity(),
					App.getTextLocale(App.getAppContext(), R.string.dialog_unsaved_changes),
					true,
					this::finish,
					() -> {}
			);
		} else {
			finish();
		}
	}

	public void playVideo(Uri uri) {
		VideoView videoView = findViewById(R.id.video_view);
		LinearLayout linearBackground = findViewById(R.id.linear_background);
		if (videoView == null) return;

		Drawable bg = linearBackground.getBackground();
		if (bg != null) {
			bg.mutate().setAlpha(51);
		}

		int width = 1280;
		int height = 720;

		try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
			retriever.setDataSource(this, uri);
			String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
			String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
			if (widthStr != null && heightStr != null) {
				width = Integer.parseInt(widthStr);
				height = Integer.parseInt(heightStr);
			}
		} catch (Exception ignored) {}


		int maxWidth = 1920;
		int maxHeight = 1080;
		if (width > maxWidth || height > maxHeight) {
			float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
			width = Math.round(width * scale);
			height = Math.round(height * scale);
		}

		ViewGroup.LayoutParams params = videoView.getLayoutParams();
		params.width = width;
		params.height = height;
		videoView.setLayoutParams(params);

		videoView.setVideoURI(uri);
		videoView.setOnPreparedListener(mp -> {
			mp.setLooping(true);
			videoView.start();
		});

		App.getChevstrapSettings().setDisplayVideoThemeWithUri(uri.toString());
	}

	public void AstyleButtonBTeal1(LinearLayout button) {
		GradientDrawable drawable = new GradientDrawable();
		drawable.setCornerRadius(30);
		drawable.setColor(Color.parseColor("#38A181"));
		button.setBackground(drawable);
	}

	public void AstyleButtonBlack1(LinearLayout button) {
		GradientDrawable drawable = new GradientDrawable();
		drawable.setCornerRadius(30);
		drawable.setColor(Color.parseColor("#1B1B1B"));
		button.setBackground(drawable);
	}

	public void AstyleButtonTRANSPARENT(LinearLayout button) {
		GradientDrawable drawable = new GradientDrawable();
		drawable.setCornerRadius(5);
		drawable.setColor(Color.TRANSPARENT);
		button.setBackground(drawable);
	}

	private static String getTextLocale(Context context, int resId) {
		return context.getString(resId);
	}
}