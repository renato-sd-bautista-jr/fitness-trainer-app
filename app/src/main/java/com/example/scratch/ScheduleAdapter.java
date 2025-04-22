    package com.example.scratch;

    import android.content.Context;
    import android.graphics.Typeface;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ArrayAdapter;
    import android.widget.TextView;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import androidx.core.content.ContextCompat;

    import org.checkerframework.checker.nullness.qual.NonNull;

    import java.util.List;
    import java.util.Locale;

    public class ScheduleAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final List<String> items;
        private final SimpleDateFormat sdfOutput = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        public ScheduleAdapter(Context context, List<String> items) {
            super(context, R.layout.item_schedule, items);
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = convertView != null ? convertView : inflater.inflate(R.layout.item_schedule, parent, false);

            TextView tvItem = view.findViewById(R.id.tvScheduleItem);
            String item = items.get(position);
            tvItem.setText(item);

            // Default style
            tvItem.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            tvItem.setTypeface(null, Typeface.NORMAL);

            if (item.startsWith("📅")) {
                // Highlight date headers
                tvItem.setTypeface(null, Typeface.BOLD);

                try {
                    String dateStr = item.replace("📅 ", "").trim();
                    Date date = sdfOutput.parse(dateStr);
                    if (date != null && date.before(new Date())) {
                        tvItem.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray)); // Past date = gray
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            return view;
        }
    }
