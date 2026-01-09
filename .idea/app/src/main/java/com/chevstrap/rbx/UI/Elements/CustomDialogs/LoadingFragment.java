package com.chevstrap.rbx.UI.Elements.CustomDialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.Extensions.CustomUIComponents;
import com.chevstrap.rbx.R;

public class LoadingFragment extends DialogFragment {
    public interface MessageLoadingListener {
        void onCancelClicked();
    }

    private MessageLoadingListener listener;
    private TextView textview_taskcurrently;
    private TextView textview_loadingstatus;
    private String pendingText = null;
    private String pendingStatus = null;

    public void setMessageboxListener(MessageLoadingListener listener) {
        this.listener = listener;
    }

    public void setMessageText(String text) {
        pendingText = text;
        if (textview_taskcurrently != null) {
            textview_taskcurrently.setText(text);
        }
    }

    public void setMessageStatus(String text) {
        pendingStatus = text;
        if (textview_loadingstatus != null) {
            textview_loadingstatus.setText(text);
        }
    }

    public View addButton(String name, LinearLayout parent) {
        if (getContext() == null) return null;
        CustomUIComponents.SmallButtonResult buttonResult =
                CustomUIComponents.addSmallButton(getContext(), name, parent);

        if (buttonResult.buttonView.getParent() == null) {
            parent.addView(buttonResult.buttonView);
        }
        return buttonResult.buttonOne;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater _inflater, @Nullable ViewGroup _container, @Nullable Bundle _savedInstanceState) {
        View _view = _inflater.inflate(R.layout.loading_fragment, _container, false);
        initialize(_savedInstanceState, _view);
        return _view;
    }

    private void initialize(Bundle _savedInstanceState, View _view) {
        LinearLayout linear_background = _view.findViewById(R.id.linear_background);
        LinearLayout linear5 = _view.findViewById(R.id.linear5);
        View button_cancel = addButton(App.getTextLocale(requireContext(), R.string.common_cancel), linear5);
        textview_taskcurrently = _view.findViewById(R.id.textview_taskcurrently);
        textview_loadingstatus = _view.findViewById(R.id.textview_loadingstatus);

        if (pendingText != null) textview_taskcurrently.setText(pendingText);
        if (pendingStatus != null) textview_loadingstatus.setText(pendingStatus);

        button_cancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancelClicked();
            dismiss();
        });
    }
}