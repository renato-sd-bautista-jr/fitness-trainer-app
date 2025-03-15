package com.example.scratch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProgressPagerAdapter extends FragmentStateAdapter {

    private final String[] metrics = {"Weight", "Heart Rate", "Calories", "Workout Duration", "Sleep", "Review"};

    public ProgressPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ProgressDetailFragment.newInstance(metrics[position]);
    }

    @Override
    public int getItemCount() {
        return metrics.length;
    }
}
