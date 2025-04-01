package com.example.scratch;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatCardView extends LinearLayout {

    private TextView tvTitle;
    private EditText etValue;
    private OnValueChangeListener valueChangeListener;

    public StatCardView(Context context) {
        super(context);
        init(context);
    }

    public StatCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StatCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.stat_card_view, this, true);
        tvTitle = findViewById(R.id.tvStatTitle);
        etValue = findViewById(R.id.etStatValue);

        if (etValue != null) {
            etValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (valueChangeListener != null) {
                        valueChangeListener.onValueChanged(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    public void setTitle(String title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    public void setValue(String value) {
        if (etValue != null) {
            etValue.setText(value);
        }
    }

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        this.valueChangeListener = listener;
    }

    public interface OnValueChangeListener {
        void onValueChanged(String newValue);
    }
}
