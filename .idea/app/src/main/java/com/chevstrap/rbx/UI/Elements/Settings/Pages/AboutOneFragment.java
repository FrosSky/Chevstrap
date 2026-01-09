package com.chevstrap.rbx.UI.Elements.Settings.Pages;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Objects;

public class AboutOneFragment extends Fragment {
	private final HashMap<Integer, LinearLayout> layoutMap = new HashMap<>();
	ConfigManager manager;
	private LinearLayout linear1;
	private TextView textview_description;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.a_normal_page_fragment, container, false);
		manager = ConfigManager.getInstance();
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
	}

	private void initialize(Bundle savedInstanceState, View view) {
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

	public void addButton(String name, String description, LinearLayout parent, String command, String typeCommand, int indexParented) {
		CustomUIComponents.ButtonResult button = CustomUIComponents.addButton(getContext(), name, description, parent, command, typeCommand);
		if (indexParented > -1) {
			Objects.requireNonNull(layoutMap.get(indexParented)).addView(button.buttonView);
		} else {
			parent.addView(button.buttonView);
		}
	}

	public void addAccordionMenu(String name, String description, Runnable AFunction, int index, LinearLayout parent) {
		CustomUIComponents.AccordionMenuResult getCustomButton =
				CustomUIComponents.addAccordionMenu(getContext(), name, description, parent, AFunction);
		layoutMap.put(index, getCustomButton.expandContainer);
		parent.addView(getCustomButton.buttonView);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		LinearLayout parentLayout = view.findViewById(R.id.linear2);
		AboutOne.addEveryPresets(requireContext(), parentLayout, this);
		textview_description.setText(App.getTextLocale(App.getAppContext(), R.string.about_description));
	}

	private void AstyleButton1(LinearLayout button) {
		GradientDrawable drawable = new GradientDrawable();
		drawable.setCornerRadius(30);
		drawable.setColor(Color.parseColor("#151515"));
		button.setBackground(drawable);
	}
}