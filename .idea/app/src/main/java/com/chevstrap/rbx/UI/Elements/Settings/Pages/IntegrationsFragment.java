package com.chevstrap.rbx.UI.Elements.Settings.Pages;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.ConfigManager;
import com.chevstrap.rbx.Extensions.CustomUIComponents;
import com.chevstrap.rbx.Models.MethodPair;
import com.chevstrap.rbx.R;
import com.chevstrap.rbx.UI.Elements.CustomDialogs.ServerHistoryFragment;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class IntegrationsFragment extends Fragment {
	ConfigManager manager;
	private LinearLayout linear1;
	private final Map<String, CustomUIComponents.ToggleResult> toggleMap = new HashMap<>();
    private final Map<String, List<CustomUIComponents.ToggleResult>> childMap = new HashMap<>();
	private TextView textview_description;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.a_normal_page_fragment, container, false);
		manager = ConfigManager.getInstance();
		textview_description = view.findViewById(R.id.textview_description);

		boolean isDark = Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark");
		if (isDark) {
			textview_description.setTextColor(Color.parseColor("#D6D6D6"));
		} else {
			textview_description.setTextColor(Color.parseColor("#282828"));
		}

		try {
			initialize(savedInstanceState, view);
			initializeLogic();
		} catch (Exception ignored) {}

		App.setSavedFragmentActivity(this.requireActivity());
		return view;
	}
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (CustomUIComponents.ToggleResult toggle : toggleMap.values()) {
            if (toggle.toggleSwitch != null) {
                toggle.toggleSwitch.setOnClickListener(null);
            }
        }
        toggleMap.clear();
        childMap.clear();
    }

    private void initialize(Bundle savedInstanceState, View view) {
		linear1 = view.findViewById(R.id.linear1);
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

	public void addImportButton(String name, String description, LinearLayout parent, String typeCommand) {
		CustomUIComponents.ButtonResult getCustomButton = CustomUIComponents.addImportButton(getContext(), name, description, parent);
		parent.addView(getCustomButton.buttonView);

		getCustomButton.buttonOne.setOnClickListener(v -> {
			if ("open_server_history".equals(typeCommand)) {
				ServerHistoryFragment fragment = new ServerHistoryFragment();
				fragment.show(requireActivity().getSupportFragmentManager(), "ServerHistoryDialog");
			}
		});
	}

	public void addToggle(String id, String name, String description, LinearLayout parent, MethodPair method, String controlledParentId) {
		CustomUIComponents.ToggleResult toggle = CustomUIComponents.addToggle(getContext(), name, description, parent, method);
		AtomicBoolean isCheckedG = new AtomicBoolean(false);

		if (controlledParentId != null) {
			if (!childMap.containsKey(controlledParentId)) {
				childMap.put(controlledParentId, new ArrayList<>());

				CustomUIComponents.ToggleResult parentToggle = toggleMap.get(controlledParentId);
				if (parentToggle != null) {
					boolean parentChecked = (Boolean) parentToggle.toggleSwitch.getTag();
					if (parentChecked) {
						if (toggle.enable != null) toggle.enable.run();
					} else {
						if (toggle.disable != null) toggle.disable.run();
					}
					isCheckedG = new AtomicBoolean(parentChecked);
				}
			}
			Objects.requireNonNull(childMap.get(controlledParentId)).add(toggle);
		} else {
			toggleMap.put(id, toggle);
		}

		AtomicBoolean finalIsCheckedG1 = isCheckedG;
		Runnable updateChildren = () -> {
			List<CustomUIComponents.ToggleResult> children = childMap.get(id);
			if (children != null) {
				for (CustomUIComponents.ToggleResult child : children) {
					if (finalIsCheckedG1.get()) {
						if (child.enable != null) child.enable.run();
					} else {
						if (child.disable != null) child.disable.run();
					}
				}
			}
		};

		AtomicBoolean finalIsCheckedG = isCheckedG;
		toggle.onToggleClick = (view, isChecked) -> {
			finalIsCheckedG.set(isChecked);
			updateChildren.run();
		};

		updateChildren.run();
		parent.addView(toggle.toggleView);
	}

	public void addButton(String name, String description, LinearLayout parent, String command, String typeCommand) {
		CustomUIComponents.ButtonResult button = CustomUIComponents.addButton(getContext(), name, description, parent, command, typeCommand);
		parent.addView(button.buttonView);
	}

	public void addTextbox(String name, String description, LinearLayout parent, MethodPair method, String typeIs) {
		CustomUIComponents.TextboxResult textbox = CustomUIComponents.addTextbox(getContext(), name, description, parent, method, typeIs);
		parent.addView(textbox.textboxView);
	}

	public void addDropdown(String name, String description, LinearLayout parent, final JSONArray jsonArray, MethodPair method) {
		CustomUIComponents.DropdownResult dropdown = CustomUIComponents.addDropdown(getContext(), name, description, parent, jsonArray, method);
		parent.addView(dropdown.dropDownView);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		LinearLayout parentLayout = view.findViewById(R.id.linear2);
		textview_description.setText(App.getTextLocale(App.getAppContext(), R.string.menu_integrations_description));
		Integrations.addEveryPresets(requireContext(), parentLayout, this);
	}

	private void AstyleButton1(LinearLayout button) {
		GradientDrawable drawable = new GradientDrawable();
		drawable.setCornerRadius(30);
		drawable.setColor(Color.parseColor("#151515"));
		button.setBackground(drawable);
	}
}
