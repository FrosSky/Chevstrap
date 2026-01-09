package com.chevstrap.rbx.UI.Elements.CustomDialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.Extensions.CustomUIComponents;
import com.chevstrap.rbx.R;
import com.chevstrap.rbx.UI.ViewModels.GlobalViewModel;

import java.util.Objects;

public class MessageboxFragment extends DialogFragment {
    private MessageboxListener listener;
    private String messageText = "Default message";
    private LinearLayout linear1;
    private TextView textview3;
    private boolean cancelDisabled = false;
    private boolean okToYes = false;
    private boolean reportExceptionDisabled = false;
    private String moreInformation = null;

    public void setMessageboxListener(MessageboxListener listener) {
        this.listener = listener;
    }

    public void setMessageText(String text) {
        this.messageText = text;
    }
    public void disableCancel() {
        this.cancelDisabled = true;
    }
    public void replaceOKWithYes() {
        this.okToYes = true;
    }
    public void disableReportException() {
        this.reportExceptionDisabled = true;
    }
    public void setMoreInformation(String text) {
        this.moreInformation = text;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.messagebox_fragment, container, false);
        initialize(view);
        initializeLogic();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false); // fragment-level, very important

        requireActivity().getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (!cancelDisabled) {
                            dismiss();
                        }
                    }
                });
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

//        dialog.setOnKeyListener((d, keyCode, event) -> {
//            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
//                if (!cancelDisabled) dismiss();
//                return true;
//            }
//            return false;
//        });

        return dialog;
    }

    private void initialize(View view) {
        String okButtonText = App.getTextLocale(requireContext(), R.string.common_ok);

        linear1 = view.findViewById(R.id.linear1);
        LinearLayout linear2 = view.findViewById(R.id.linear2);
        TextView textview1 = view.findViewById(R.id.textview1);
        textview3 = view.findViewById(R.id.textview3);

        LinearLayout linear_more_information = view.findViewById(R.id.linear_more_information);
        TextView textview_more_information = view.findViewById(R.id.textview_more_information);

        if (okToYes) {
            okButtonText = App.getTextLocale(requireContext(), R.string.common_yes);
        }

        View button_ok = addButton(okButtonText, linear2);
        View button_cancel = addButton(App.getTextLocale(requireContext(), R.string.common_no), linear2);

        if (!okToYes) {
            button_cancel.setVisibility(View.GONE);
        }

        View button_report_exception;
        if (moreInformation != null) {
            textview_more_information.setText(moreInformation);
            button_report_exception = addButton(App.getTextLocale(requireContext(), R.string.common_report_exception), linear2);
            linear_more_information.setVisibility(View.VISIBLE);
            if (button_report_exception != null && reportExceptionDisabled) {
                button_report_exception.setOnClickListener(v -> {
                    GlobalViewModel.openWebpage(requireContext(), App.ProjectSupportLink);
                    dismiss();
                });
            }
        } else {
            linear_more_information.setVisibility(View.GONE);
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(30);

        if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark")) {
            drawable.setColor(Color.parseColor("#070707"));
            drawable.setStroke(1, Color.parseColor("#353535"));
        } else if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "light")) {
            drawable.setColor(Color.parseColor("#FFFFFF"));
            drawable.setStroke(1, Color.parseColor("#252525"));
        }

        linear_more_information.setBackground(drawable);
        if (cancelDisabled) {
            button_cancel.setVisibility(View.GONE);
        }

        button_ok.setOnClickListener(v -> {
            if (listener != null) listener.onOkClicked();
            dismiss();
        });

        button_cancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancelClicked();
            dismiss();
        });
    }

    private void initializeLogic() {
        String videoUri = App.getChevstrapSettings().getDisplayVideoThemeWithUri();
        boolean useTransparent = !videoUri.isEmpty();

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(15);

        if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "light")) {
            if (useTransparent) {
                drawable.setColor(Color.parseColor("#99EFEFEF"));
                drawable.setStroke(5, Color.parseColor("#99EAEAEA"));
            } else {
                drawable.setColor(Color.parseColor("#EFEFEF"));
                drawable.setStroke(5, Color.parseColor("#EAEAEA"));
            }
        } else if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark")) {
            if (useTransparent) {
                drawable.setColor(Color.parseColor("#99101010"));
                drawable.setStroke(5, Color.parseColor("#99151515"));
            } else {
                drawable.setColor(Color.parseColor("#101010"));
                drawable.setStroke(5, Color.parseColor("#151515"));
            }
        }

        if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "light")) {
            textview3.setTextColor(Color.parseColor("#000000"));
        } else if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark")) {
            textview3.setTextColor(Color.parseColor("#FFFFFF"));
        }

        linear1.setBackground(drawable);

        if (!messageText.isEmpty()) {
            textview3.setText(messageText);
        }
    }

    public View addButton(String name, LinearLayout parent) {
        if (getContext() == null) return null;

        CustomUIComponents.SmallButtonResult buttonResult =
                CustomUIComponents.addSmallButton(getContext(), name, parent);

        if (buttonResult.buttonView.getParent() == null) {
            parent.addView(buttonResult.buttonView);
        }

        buttonResult.buttonOne.setPadding(
                buttonResult.buttonOne.getPaddingLeft() + 80,
                buttonResult.buttonOne.getPaddingTop(),
                buttonResult.buttonOne.getPaddingRight() + 80,
                buttonResult.buttonOne.getPaddingBottom()
        );

        return buttonResult.buttonOne;
    }

    public interface MessageboxListener {
        void onOkClicked();
        void onCancelClicked();
    }
}
