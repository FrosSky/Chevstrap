package com.chevstrap.rbx.UI.Elements.Settings.Pages;

import static com.chevstrap.rbx.App.getTextLocale;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class LocalPreferencesFragment extends Fragment {
    private final HashMap<Integer, LinearLayout> layoutMap = new HashMap<>();
    private final List<SectionGroup> sectionGroups = new ArrayList<>();
    private final List<SearchListener> searchListeners = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    ConfigManager manager;
    private LinearLayout linear2;
    private SectionGroup currentGroup = null;
    private Runnable searchRunnable;
    private TextView textview_description;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.a_normal_page_fragment, container, false);
        manager = ConfigManager.getInstance();
        initialize(view);
        App.setSavedFragmentActivity(this.requireActivity());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        searchRunnable = null;
        searchListeners.clear();

        for (SectionGroup group : sectionGroups) {
            group.sectionTitle = null;
            group.children.clear();
        }
        sectionGroups.clear();
        currentGroup = null;

        for (LinearLayout layout : layoutMap.values()) {
            if (layout != null) {
                layout.removeAllViews();
            }
        }
        layoutMap.clear();
        linear2 = null;
        manager = null;
    }


    private void initialize(View view) {
        linear2 = view.findViewById(R.id.linear2);
        textview_description = view.findViewById(R.id.textview_description);

        boolean isDark = Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark");
        if (isDark) {
            textview_description.setTextColor(Color.parseColor("#D6D6D6"));
        } else {
            textview_description.setTextColor(Color.parseColor("#282828"));
        }
    }

    public void addSection(String text) {
        CustomUIComponents.SectionResult result = CustomUIComponents.addSection(getContext(), text);
        linear2.addView(result.sectionContainer);
        SectionGroup group = new SectionGroup();
        group.sectionTitle = result.sectionContainer;
        sectionGroups.add(group);
        currentGroup = group;
    }

    public void addDivider() {
        CustomUIComponents.DividerResult getCustomDivider = CustomUIComponents.addDivider(getContext());
        if (currentGroup != null) currentGroup.children.add(getCustomDivider.dividerView);
        searchListeners.add(query -> {
            if (!query.isEmpty()) {
                getCustomDivider.dividerView.setVisibility(View.GONE);
            } else {
                getCustomDivider.dividerView.setVisibility(View.VISIBLE);
            }
        });
        linear2.addView(getCustomDivider.dividerView);
        addSection(null);
    }

    public void addToggle(String name, String description, MethodPair selectedMethod, int indexParented) {
        CustomUIComponents.ToggleResult getCustomToggle = CustomUIComponents.addToggle(getContext(), name, description, linear2, selectedMethod);
        searchListeners.add(query -> {
            boolean match = getCustomToggle.nameTextView.getText().toString().toLowerCase().contains(query);
            int newVis = match ? View.VISIBLE : View.GONE;
            if (getCustomToggle.toggleView.getVisibility() != newVis) {
                getCustomToggle.toggleView.setVisibility(newVis);
            }
        });
        if (indexParented > -1) {
            Objects.requireNonNull(layoutMap.get(indexParented)).addView(getCustomToggle.toggleView);
        } else {
            if (currentGroup != null) currentGroup.children.add(getCustomToggle.toggleView);
            linear2.addView(getCustomToggle.toggleView);
        }
    }

    public void addButton(String name, String description, String command, String typeCommand, int indexParented) {
        CustomUIComponents.ButtonResult getCustomButton = CustomUIComponents.addButton(getContext(), name, description, linear2, command, typeCommand);
        searchListeners.add(query -> {
            boolean match = getCustomButton.nameTextView.getText().toString().toLowerCase().contains(query);
            int newVis = match ? View.VISIBLE : View.GONE;
            if (getCustomButton.buttonView.getVisibility() != newVis) {
                getCustomButton.buttonView.setVisibility(newVis);
            }
        });
        if (indexParented > -1) {
            Objects.requireNonNull(layoutMap.get(indexParented)).addView(getCustomButton.buttonView);
        } else {
            if (currentGroup != null) currentGroup.children.add(getCustomButton.buttonView);
            linear2.addView(getCustomButton.buttonView);
        }
    }

    public void addAccordionMenu(String name, String description, Runnable AFunction, int index) {
        CustomUIComponents.AccordionMenuResult getCustomButton =
                CustomUIComponents.addAccordionMenu(getContext(), name, description, linear2, AFunction);

        if (currentGroup != null) {
            currentGroup.children.add(getCustomButton.buttonView);
        }
        layoutMap.put(index, getCustomButton.expandContainer);
        searchListeners.add(query -> {
            Handler handler = new Handler(Looper.getMainLooper());
            if (getCustomButton.expandContainer.getChildCount() == 0 && AFunction != null) {
                handler.postDelayed(() -> {
                    if (getCustomButton.expandContainer.getChildCount() == 0) {
                        AFunction.run();
                    }
                }, 100);
            }

            Handler handler1 = new Handler(Looper.getMainLooper());
            handler1.postDelayed(() -> {
                boolean allGone = true;
                for (int i = 0; i < getCustomButton.expandContainer.getChildCount(); i++) {
                    View child = getCustomButton.expandContainer.getChildAt(i);
                    if (child.getVisibility() != View.GONE) {
                        allGone = false;
                        break;
                    }
                }

                if (allGone) {
                    getCustomButton.buttonView.setVisibility(View.GONE);
                } else {
                    getCustomButton.buttonView.setVisibility(View.VISIBLE);
                    if (query.isEmpty()) {
                        getCustomButton.expandContainer1.setVisibility(View.GONE);
                    } else {
                        getCustomButton.expandContainer1.setVisibility(View.VISIBLE);
                    }
                }
            }, 100);
        });

        linear2.addView(getCustomButton.buttonView);
    }

    public void addTextbox(String name, String description, MethodPair selectedMethod, String TypeIs, int indexParented) {
        CustomUIComponents.TextboxResult getCustomTextbox = CustomUIComponents.addTextbox(getContext(), name, description, linear2, selectedMethod, TypeIs);
        searchListeners.add(query -> {
            boolean match = getCustomTextbox.nameTextView.getText().toString().toLowerCase().contains(query);
            int newVis = match ? View.VISIBLE : View.GONE;
            if (getCustomTextbox.textboxView.getVisibility() != newVis) {
                getCustomTextbox.textboxView.setVisibility(newVis);
            }
        });
        if (indexParented > -1) {
            Objects.requireNonNull(layoutMap.get(indexParented)).addView(getCustomTextbox.textboxView);
        } else {
            if (currentGroup != null) currentGroup.children.add(getCustomTextbox.textboxView);
            linear2.addView(getCustomTextbox.textboxView);
        }
    }

    public void addDropdown(String name, String description, final JSONArray jsonArray, MethodPair selectedMethod, int indexParented) {
        CustomUIComponents.DropdownResult getCustomDropdown = CustomUIComponents.addDropdown(getContext(), name, description, linear2, jsonArray, selectedMethod);
        searchListeners.add(query -> {
            boolean match = getCustomDropdown.nameTextView.getText().toString().toLowerCase().contains(query);
            int newVis = match ? View.VISIBLE : View.GONE;
            if (getCustomDropdown.dropDownView.getVisibility() != newVis) {
                getCustomDropdown.dropDownView.setVisibility(newVis);
            }
        });
        if (indexParented > -1) {
            Objects.requireNonNull(layoutMap.get(indexParented)).addView(getCustomDropdown.dropDownView);
        } else {
            if (currentGroup != null) currentGroup.children.add(getCustomDropdown.dropDownView);
            linear2.addView(getCustomDropdown.dropDownView);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        textview_description.setText(App.getTextLocale(App.getAppContext(), R.string.menu_fastflags_description));
        try {
            CustomUIComponents.TextboxOnlyResult searchBarObjects =
                    createSearchBar(getTextLocale(App.getAppContext(), R.string.menu_fastflags_search), linear2, R.drawable.ic_search_white);

            if (searchBarObjects != null && searchBarObjects.editText != null) {
                searchBarObjects.editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        String query = s.toString().toLowerCase();
                        if (searchRunnable != null) {
                            handler.removeCallbacks(searchRunnable);
                        }
                        searchRunnable = () -> {
                            for (SearchListener listener : searchListeners) {
                                listener.onQueryChanged(query);
                            }
                            for (SectionGroup group : sectionGroups) {
                                group.setVisibility(group.anyChildVisible());
                            }
                        };
                        handler.postDelayed(searchRunnable, 320);
                    }
                });
            }
            LocalPreferences.addEveryPresets(getContext(), this);
        } catch (Exception e) {
            App.getLogger().writeLine("FFlagsSettingsFragment::onViewCreated", e.getMessage());
        }
    }

    public CustomUIComponents.TextboxOnlyResult createSearchBar(String name, LinearLayout parent, int imageResId) {
        try {
            CustomUIComponents.TextboxOnlyResult getCustomTextbox = CustomUIComponents.addTextboxOnly(name, parent, imageResId);
            parent.addView(getCustomTextbox.textboxView);
            return getCustomTextbox;
        } catch (Exception e) {
            App.getLogger().writeLine("FFlagsSettingsFragment::createSearchBar", e.getMessage());
            return null;
        }
    }

    private interface SearchListener {
        void onQueryChanged(String query);
    }

    private static class SectionGroup {
        final List<View> children = new ArrayList<>();
        FrameLayout sectionTitle;

        void setVisibility(boolean visible) {
            int vis = visible ? View.VISIBLE : View.GONE;
            if (sectionTitle != null) {
                sectionTitle.setVisibility(vis);
            }
        }

        boolean anyChildVisible() {
            for (View child : children) {
                if (child.getVisibility() == View.VISIBLE) return true;
            }
            return false;
        }
    }
}