package com.example.frontend.ui.publish;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.frontend.databinding.FragmentPublishBinding;

public class PublishFragment extends Fragment {

    private FragmentPublishBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PublishViewModel publishViewModel =
                new ViewModelProvider(this).get(PublishViewModel.class);

        binding = FragmentPublishBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textPublish;
        publishViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}