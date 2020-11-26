package com.db.auctionclient.view.runningauctions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.auctionclient.R;
import com.db.auctionclient.model.entities.AuctionWithTask;
import com.db.auctionclient.view.AuctionConsoleFragment;
import com.db.auctionclient.view.ListItemListener;
import com.db.auctionclient.view.MainActivity;
import com.db.auctionclient.viewmodel.RunningAuctionsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lists all auctions, in which the user currently participates.
 */
public class RunningAuctionsFragment extends Fragment implements ListItemListener<AuctionWithTask> {
    // Components of the graphical interface.
    private RecyclerView rvAuctionTasks;
    private AuctionTasksAdapter adapter;
    private RunningAuctionsViewModel viewModel;

    public RunningAuctionsFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method for creating a new instance of {@link RunningAuctionsFragment}.
     * @return a new instance of {@link RunningAuctionsFragment}.
     */
    public static RunningAuctionsFragment newInstance() {
        RunningAuctionsFragment fragment = new RunningAuctionsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_running_auctions, container, false);

        rvAuctionTasks = rootView.findViewById(R.id.rvAuctionTasks);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        rvAuctionTasks.setLayoutManager(layoutManager);
        adapter = new AuctionTasksAdapter(new ArrayList<>(), this);
        rvAuctionTasks.setAdapter(adapter);
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()))
                .get(RunningAuctionsViewModel.class);
        viewModel.getAllAuctionWithTasks().observe(getViewLifecycleOwner(), new Observer<List<AuctionWithTask>>() {
            @Override
            public void onChanged(List<AuctionWithTask> auctionWithTasks) {
                if(auctionWithTasks != null){
                    // get all auctions, which have task and update the recycler view
                    List<AuctionWithTask> auctionTasks = auctionWithTasks.stream()
                            .filter(a -> a.getTask() != null)
                            .collect(Collectors.toList());
                    adapter.setAuctionTasks(auctionTasks);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onClick(AuctionWithTask item) {
        AuctionConsoleFragment fragment = AuctionConsoleFragment.newInstance(item.getTask().getAuctionId());
        ((MainActivity)getActivity()).initFragment(fragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Laufende Auktionen");
    }
}
