package com.db.auctionclient.view.auctionsoverview;

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
import com.db.auctionclient.model.entities.Auction;
import com.db.auctionclient.view.SetBidFragment;
import com.db.auctionclient.view.ListItemListener;
import com.db.auctionclient.view.MainActivity;
import com.db.auctionclient.viewmodel.AuctionsOverviewViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists all auctions, which are currently active.
 */
public class AuctionsOverviewFragment extends Fragment implements ListItemListener<Auction> {
    // Components of the graphical interface.
    private RecyclerView rvAuctions;
    private AuctionListAdapter adapter;
    private AuctionsOverviewViewModel viewModel;

    public AuctionsOverviewFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method for creating a new instance of {@link AuctionsOverviewFragment}.
     * @return a new instance of {@link AuctionsOverviewFragment}.
     */
    public static AuctionsOverviewFragment newInstance() {
        return new AuctionsOverviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_auctions_overview, container, false);

        rvAuctions = rootView.findViewById(R.id.rvAuctions);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        rvAuctions.setLayoutManager(layoutManager);
        adapter = new AuctionListAdapter(new ArrayList<>(), this);
        rvAuctions.setAdapter(adapter);
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()))
                .get(AuctionsOverviewViewModel.class);
        viewModel.getAllAuctions().observe(getViewLifecycleOwner(), new Observer<List<Auction>>() {
            @Override
            public void onChanged(List<Auction> auctions) {
                if(auctions != null){
                    // update auction list
                    adapter.setAuctions(auctions);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Auktionsübersicht");
    }

    @Override
    public void onClick(Auction item) {
        SetBidFragment setBidFragment = SetBidFragment.newInstance(
                item.getId(), item.getTitle(), item.getStartPrice(),
                item.getHostIp(), item.getHostPort());
        ((MainActivity)getActivity()).initFragment(setBidFragment);
    }
}
