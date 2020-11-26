package com.db.auctionclient.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.db.auctionclient.R;
import com.db.auctionclient.view.auctionsoverview.AuctionsOverviewFragment;
import com.db.auctionclient.viewmodel.SetBidViewModel;

/**
 * Allows the user to set his bid and enter an active auction.
 */
public class SetBidFragment extends Fragment implements View.OnClickListener {
    // Codes for bundling and retrieving the data.
    private static final String PARAM_AUCTION_ID = "PARAM_AUCTION_ID";
    private static final String PARAM_AUCTION_TITLE = "PARAM_AUCTION_TITLE";
    private static final String PARAM_STARTING_PRICE = "PARAM_STARTING_PRICE";
    private static final String PARAM_HOST_IP = "PARAM_HOST_IP";
    private static final String PARAM_HOST_PORT = "PARAM_HOST_PORT";

    private SetBidViewModel viewModel;
    // Auction data.
    private int auctionId;
    private String auctionTitle;
    private int startingPrice;
    private String hostIp;
    private int hostPort;
    // Components of the graphical interface.
    private EditText etUserBid;
    private Button btnNext;
    private Button btnAbort;

    public SetBidFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method for creating a new instance of {@link SetBidFragment}.
     * @param auctionId id of the auction, which should be joined.
     * @param auctionTitle title of the auction.
     * @param startingPrice starting price of the auction.
     * @param hostIp IP address of the host, which hosts the auction.
     * @param hostPort port of the host, which hosts the auction.
     * @return a new instance of {@link SetBidFragment}.
     */
    public static SetBidFragment newInstance(int auctionId, String auctionTitle, int startingPrice,
                                             String hostIp, int hostPort) {
        SetBidFragment fragment = new SetBidFragment();
        Bundle args = new Bundle();
        args.putInt(PARAM_AUCTION_ID, auctionId);
        args.putString(PARAM_AUCTION_TITLE, auctionTitle);
        args.putInt(PARAM_STARTING_PRICE, startingPrice);
        args.putString(PARAM_HOST_IP, hostIp);
        args.putInt(PARAM_HOST_PORT, hostPort);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            auctionId = getArguments().getInt(PARAM_AUCTION_ID);
            auctionTitle = getArguments().getString(PARAM_AUCTION_TITLE);
            startingPrice = getArguments().getInt(PARAM_STARTING_PRICE);
            hostIp = getArguments().getString(PARAM_HOST_IP);
            hostPort = getArguments().getInt(PARAM_HOST_PORT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(auctionTitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_set_bid, container, false);
        etUserBid = rootView.findViewById(R.id.etUserBid);
        btnNext = rootView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        btnAbort = rootView.findViewById(R.id.btnAbort);
        btnAbort.setOnClickListener(this);
        viewModel = new ViewModelProvider(
                this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()))
                .get(SetBidViewModel.class);
        return rootView;
    }


    @Override
    public void onClick(View view) {
        if(view == btnNext){
            if(etUserBid.getText().length() == 0) etUserBid.setText("0");
            int userBid = Integer.parseInt(etUserBid.getText().toString());
            if(userBid >= startingPrice){
                if(viewModel.workersAvailable()){
                    boolean joined = viewModel.joinAuction(auctionId, hostIp, hostPort, userBid);
                    if(joined){
                        ((MainActivity)getActivity())
                                .initFragment(AuctionConsoleFragment.newInstance(auctionId));
                    }else{
                        Toast.makeText(getContext(),
                                "Teilnahme nicht möglich. Die maximale Anzahl der Auktionsteilnahmen wurde erreicht.",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }else{
                    Toast.makeText(getContext(),
                            "Teilnahme ist bereits erfolgt.",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }else{
                etUserBid.setError("Das Gebot darf nicht niedriger sein als der Startpreis.");
            }
        }else if (view == btnAbort){
            ((MainActivity)getActivity()).initFragment(AuctionsOverviewFragment.newInstance());
        }
    }
}
