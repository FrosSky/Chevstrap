package com.chevstrap.rbx.UI.Elements.CustomDialogs;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.CustomWatcher;
import com.chevstrap.rbx.Extensions.CustomUIComponents;
import com.chevstrap.rbx.Models.Entities.ActivityData;
import com.chevstrap.rbx.Paths;
import com.chevstrap.rbx.R;
import com.chevstrap.rbx.Utility.FileTool;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerHistoryFragment extends DialogFragment {
	private final List<ActivityData> history = new ArrayList<>();
	private LinearLayout linear1;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.serverhistory_fragment, container, false);
		try {
			initialize(view);
			initializeLogic();
			return view;
		} catch (Exception e) {
			App.getLogger().writeException("ServerHistoryFragment", e);
			return null;
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}
		return dialog;
	}

	private void initialize(View view) {
		ListView listView = view.findViewById(R.id.listview);
		linear1 = view.findViewById(R.id.linear1);
		TextView title = view.findViewById(R.id.textview_iswhatda);
		LinearLayout linear_bottom_list = view.findViewById(R.id.linear_bottom_list);
		View button_close = addButton(App.getTextLocale(requireContext(), R.string.common_close), linear_bottom_list);

		button_close.setOnClickListener(v -> dismiss());

		if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "light")) {
			title.setTextColor(Color.BLACK);
		} else {
			title.setTextColor(Color.WHITE);
		}

		GradientDrawable listBackground = new GradientDrawable();
		listBackground.setCornerRadius(20);
		listBackground.setColor(Color.parseColor("#070707"));
		listView.setBackground(listBackground);

		try {
            history.addAll(CustomWatcher.getInstance().getAllHistoryServer());
            listView.setAdapter(new HistoryAdapter(history));
        } catch (Exception e) {
			App.getLogger().writeException("ServerHistoryFragment", e);
		}
	}

	private void initializeLogic() {
		String videoUri = App.getChevstrapSettings().getDisplayVideoThemeWithUri();
		boolean useTransparent = !videoUri.isEmpty();

		GradientDrawable bg = new GradientDrawable();
		bg.setCornerRadius(15);

		if (Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "light")) {
			if (useTransparent) {
				bg.setColor(Color.parseColor("#99EFEFEF"));
				bg.setStroke(5, Color.parseColor("#99EAEAEA"));
			} else {
				bg.setColor(Color.parseColor("#EFEFEF"));
				bg.setStroke(5, Color.parseColor("#EAEAEA"));
			}
		} else {
			if (useTransparent) {
				bg.setColor(Color.parseColor("#99101010"));
				bg.setStroke(5, Color.parseColor("#99151515"));
			} else {
				bg.setColor(Color.parseColor("#101010"));
				bg.setStroke(5, Color.parseColor("#151515"));
			}
		}
		linear1.setBackground(bg);
	}

	public static class HistoryAdapter extends BaseAdapter {
		private final List<ActivityData> history;

		public HistoryAdapter(List<ActivityData> history) {
			this.history = history;
		}

		@Override
		public int getCount() {
			return history.size();
		}

		@Override
		public ActivityData getItem(int position) {
			return history.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.experience_item_history, parent, false);

				holder = new ViewHolder();
				holder.linearHey = convertView.findViewById(R.id.linear_hey);
				holder.linearRejoin = convertView.findViewById(R.id.linear_rejoin);
				holder.imageview_thumbnail = convertView.findViewById(R.id.imageview_thumbnail);
				holder.nameText = convertView.findViewById(R.id.textview_name_option);
				holder.descText = convertView.findViewById(R.id.textview_description_option);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			boolean isDark = Objects.equals(App.getChevstrapSettings().getAppThemeInApp(), "dark");
			String videoUri = App.getChevstrapSettings().getDisplayVideoThemeWithUri();
			boolean useTransparent = !videoUri.isEmpty();

			holder.nameText.setTextColor(isDark ? Color.WHITE : Color.BLACK);
			holder.descText.setTextColor(isDark ? Color.parseColor("#ABABAB") : Color.parseColor("#545454"));

			ActivityData data = getItem(position);
			String name = (data.universeDetails != null && data.universeDetails.getData() != null)
					? data.universeDetails.getData().getName()
					: "";

			holder.imageview_thumbnail.setBackgroundColor(Color.parseColor("#202020"));
			holder.imageview_thumbnail.setTag(null);

			if (data.universeDetails != null && data.universeDetails.getThumbnail() != null) {
				String imageUrl = data.universeDetails.getThumbnail().getImageUrl();
				holder.imageview_thumbnail.setTag(imageUrl);

				new Thread(() -> {
					try {
						URL url = new URL(imageUrl);
						Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
						Drawable drawable = new BitmapDrawable(parent.getContext().getResources(), bitmap);

						holder.imageview_thumbnail.post(() -> {
							if (imageUrl.equals(holder.imageview_thumbnail.getTag())) {
								holder.imageview_thumbnail.setBackground(drawable);
							}
						});
					} catch (IOException e) {
						App.getLogger().writeException("ServerHistoryFragment::getView", e);
					}
				}).start();
			}

			GradientDrawable rejoinDrawable = new GradientDrawable();
			rejoinDrawable.setCornerRadius(30);
			rejoinDrawable.setColor(Color.parseColor("#38A181"));
			rejoinDrawable.setStroke(1, Color.parseColor("#38A181"));
			holder.linearRejoin.setBackground(rejoinDrawable);

			holder.nameText.setText(name);
			holder.descText.setText(data.getGameHistoryDescription());

			GradientDrawable bgDrawable = new GradientDrawable();
			bgDrawable.setCornerRadii(new float[]{30,30,30,30,30,30,30,30});

			if (isDark) {
				if (useTransparent) {
					bgDrawable.setColor(Color.parseColor("#661B1B1B"));
					bgDrawable.setStroke(2, Color.parseColor("#19070707"));
				} else {
					bgDrawable.setColor(Color.parseColor("#1B1B1B"));
					bgDrawable.setStroke(2, Color.parseColor("#070707"));
				}
			} else {
				if (useTransparent) {
					bgDrawable.setColor(Color.parseColor("#66F0F3F5"));
					bgDrawable.setStroke(2, Color.parseColor("#19EAEDEF"));
				} else {
					bgDrawable.setColor(Color.parseColor("#F0F3F5"));
					bgDrawable.setStroke(2, Color.parseColor("#EAEDEF"));
				}
			}

			holder.linearHey.setBackground(bgDrawable);
			holder.linearHey.setClipToOutline(true);
			holder.imageview_thumbnail.setClipToOutline(true);
			holder.imageview_thumbnail.setOutlineProvider(ViewOutlineProvider.BACKGROUND);

			holder.linearRejoin.setOnClickListener(v -> {
				if (parent.getContext() != null) {
					data.rejoinServer(parent.getContext());
				}
			});

			return convertView;
		}

		static class ViewHolder {
			LinearLayout linearHey;
			LinearLayout linearRejoin;
			ImageView imageview_thumbnail;
			TextView nameText;
			TextView descText;
		}
	}


	public View addButton(String name, LinearLayout parent) {
		if (getContext() == null) return null;

		CustomUIComponents.SmallButtonResult buttonResult =
				CustomUIComponents.addSmallButton(getContext(), name, parent);

		buttonResult.buttonOne.setPadding(
				buttonResult.buttonOne.getPaddingLeft() + 100,
				buttonResult.buttonOne.getPaddingTop(),
				buttonResult.buttonOne.getPaddingRight() + 100,
				buttonResult.buttonOne.getPaddingBottom()
		);

		if (buttonResult.buttonView.getParent() == null) {
			parent.addView(buttonResult.buttonView);
		}

		return buttonResult.buttonOne;
	}
}
