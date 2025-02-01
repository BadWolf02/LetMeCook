package com.example.letmecook.ui.calendar;

import android.os.Bundle; //Passes data to the fragment and restores its state after config changes
import android.view.LayoutInflater; // handles the XML layout file into a View object
import android.view.View;
import android.view.ViewGroup; // View object that gets displayed by the fragment
import android.widget.TextView;
import androidx.annotation.NonNull; //Arguments or return values that cannot be null
import androidx.fragment.app.Fragment; //base class for the fragment
import androidx.lifecycle.ViewModelProvider; //managing ViewModelProvider
import com.example.letmecook.databinding.FragmentCalendarBinding;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding; // binding object allows interaction with views

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CalendarViewModel calendarViewModel =
                new ViewModelProvider(this).get(CalendarViewModel.class);

        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textCalendar;
        calendarViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}