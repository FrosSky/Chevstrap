package com.chevstrap.rbx.UI.Elements.Settings.Pages;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.ConfigManager;
import com.chevstrap.rbx.Extensions.CustomUIComponents;
import com.chevstrap.rbx.Models.MethodPair;
import com.chevstrap.rbx.R;
import com.chevstrap.rbx.SettingsActivity;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Objects;

public class ChevstrapFragment extends Fragment {
	private final HashMap<Integer, LinearLayout> layoutMap = new HashMap<>();
	ConfigManager manager;
	private LinearLayout linear1;
	private LinearLayout linear2;
	private ActivityResultLauncher<String> videoPickerLauncher;
	private TextView textview_description;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.a_normal_page_fragment, container, false);
		manager = ConfigManager.getInstance();

		try {
			initialize(savedInstanceState, view);
			initializeLogic();
		} catch (Exception e) {
			App.getLogger().writeException("ChevstrapFragment::initialize", e);
		}

		videoPickerLauncher = registerForActivityResult(
				new ActivityResultContracts.GetContent(),
				uri -> {
					if (uri == null) {
						App.getLogger().writeLine("ChevstrapFragment::PersistUriPermission", "No video selected");
						return;
					}

					try {
						requireContext().getContentResolver().takePersistableUriPermission(
								uri,
								Intent.FLAG_GRANT_READ_URI_PERMISSION
						);
						App.getLogger().writeLine("ChevstrapFragment::PersistUriPermission", "Persisted URI permission: " + uri);
					} catch (SecurityException e) {
						App.getLogger().writeException("ChevstrapFragment::PersistUriPermission", e);
					}

					App.getChevstrapSettings().setDisplayVideoThemeWithUri(uri.toString());
					App.getLogger().writeLine("ChevstrapFragment::PersistUriPermission", "Saved video URI: " + uri);

					Activity settingsActivity = App.getSavedSettingsActivity();
					if (settingsActivity instanceof SettingsActivity) {
						((SettingsActivity) settingsActivity).playVideo(uri);
						App.getLogger().writeLine("ChevstrapFragment::PersistUriPermission", "Playing video on SettingsActivity");
					} else {
						App.getLogger().writeLine("ChevstrapFragment::PersistUriPermission", "Settings activity not found");
					}
				}
		);

		App.setSavedFragmentActivity(this.requireActivity());
		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		for (LinearLayout layout : layoutMap.values()) {
			if (layout != null) {
				layout.removeAllViews();
			}
		}
		layoutMap.clear();
		linear2 = null;
		manager = null;
	}

	private void initialize(Bundle savedInstanceState, View view) {
		linear2 = view.findViewById(R.id.linear2);
		linear1 = view.findViewById(R.id.linear1);
		textview_description = view.findViewById(R.id.textview_description);

		boolean isDark = Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark");
		if (isDark) {
			textview_description.setTextColor(Color.parseColor("#D6D6D6"));
		} else {
			textview_description.setTextColor(Color.parseColor("#282828"));
		}
	}

	private void initializeLogic() {
		AstyleButton1(linear1);
	}

	public void addSection(ViewGroup parent, String text) {
		CustomUIComponents.SectionResult section = CustomUIComponents.addSection(getContext(), text);
		parent.addView(section.sectionContainer);
	}

	public void addDivider(ViewGroup parent) {
		CustomUIComponents.DividerResult divider = CustomUIComponents.addDivider(getContext());
		parent.addView(divider.dividerView);
	}

	public void addToggle(String name, String description, MethodPair selectedMethod, int indexParented) {
		CustomUIComponents.ToggleResult getCustomToggle = CustomUIComponents.addToggle(getContext(), name, description, linear2, selectedMethod);
		if (indexParented > -1) {
			Objects.requireNonNull(layoutMap.get(indexParented)).addView(getCustomToggle.toggleView);
		} else {
			linear2.addView(getCustomToggle.toggleView);
		}
	}

	public void addImportButton(String name, String description, String command, String typeCommand, int indexParented) {
		CustomUIComponents.ButtonResult getCustomButton = CustomUIComponents.addImportButton(getContext(), name, description, linear2);
		if (indexParented > -1) {
			Objects.requireNonNull(layoutMap.get(indexParented)).addView(getCustomButton.buttonView);
		} else {
			linear2.addView(getCustomButton.buttonView);
		}

		getCustomButton.buttonOne.setOnClickListener(v -> {
			if ("set_animated_theme".equals(typeCommand)) {
				videoPickerLauncher.launch("video/*");
			}
		});
	}

	public void addAccordionMenu(String name, String description, Runnable AFunction, int index) {
		CustomUIComponents.AccordionMenuResult getCustomButton =
				CustomUIComponents.addAccordionMenu(getContext(), name, description, linear2, AFunction);

		layoutMap.put(index, getCustomButton.expandContainer);
		linear2.addView(getCustomButton.buttonView);
	}

	public void addTextbox(String name, String description, MethodPair selectedMethod, String TypeIs, int indexParented) {
		CustomUIComponents.TextboxResult getCustomTextbox = CustomUIComponents.addTextbox(getContext(), name, description, linear2, selectedMethod, TypeIs);
		if (indexParented > -1) {
			Objects.requireNonNull(layoutMap.get(indexParented)).addView(getCustomTextbox.textboxView);
		} else {
			linear2.addView(getCustomTextbox.textboxView);
		}
	}

	public void addDropdown(String name, String description, final JSONArray jsonArray, MethodPair selectedMethod, int indexParented) {
		CustomUIComponents.DropdownResult getCustomDropdown = CustomUIComponents.addDropdown(getContext(), name, description, linear2, jsonArray, selectedMethod);
		if (indexParented > -1) {
			Objects.requireNonNull(layoutMap.get(indexParented)).addView(getCustomDropdown.dropDownView);
		} else {
			linear2.addView(getCustomDropdown.dropDownView);
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		LinearLayout parentLayout = view.findViewById(R.id.linear2);
		textview_description.setText(App.getTextLocale(App.getAppContext(), R.string.menu_settings_description));
		try {
			Chevstrap.addEveryPresets(requireContext(), parentLayout, this);
		} catch (Exception e) {
			App.getLogger().writeException("ChevstrapFragment::onViewCreated", e);
		}
	}

	private void AstyleButton1(LinearLayout button) {
		GradientDrawable drawable = new GradientDrawable();
		drawable.setCornerRadius(30);
		drawable.setColor(Color.parseColor("#151515"));
		button.setBackground(drawable);
	}

    public void setLinear1(LinearLayout linear1) {
        this.linear1 = linear1;
    }
}