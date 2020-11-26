package com.db.auctionclient.view;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.db.auctionclient.R;
import com.db.auctionclient.model.entities.AuctionTask;
import com.db.auctionclient.model.entities.AuctionWithTask;
import com.db.auctionclient.viewmodel.AuctionConsoleViewModel;

/**
 * Provides information about the auction, in which the user participates.
 */
public class AuctionConsoleFragment extends Fragment
        implements ChangeBidDialogFragment.DialogListener {
    // Codes for bundling and retrieving the data.
    private static final String PARAM_AUCTION_ID = "PARAM_AUCTION_ID";

    private AuctionConsoleViewModel viewModel;
    /** Dialog, which is only visible, if the user clicked on the change bid button.*/
    private DialogFragment changeBidDialogFragment;
    // Auction data.
    private int auctionId;
    private int startingPrice;
    // Components of the graphical interface.
    private TextView tvAuctionType;
    private TextView tvMinPrice;
    private TextView tvUserBid;
    private TextView tvFinalPrice;
    private TextView tvAuctionResult;
    private TextView tvStateMsg;
    private Button btnLeave;
    private Button btnChangeBid;

    public AuctionConsoleFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method for creating a new instance of {@link AuctionConsoleFragment}.
     * @param auctionId id of the auction, which was joined.
     * @return an new instance of {@link AuctionConsoleFragment}.
     */
    public static AuctionConsoleFragment newInstance(int auctionId) {
        AuctionConsoleFragment fragment = new AuctionConsoleFragment();
        Bundle args = new Bundle();
        args.putInt(PARAM_AUCTION_ID, auctionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            auctionId = getArguments().getInt(PARAM_AUCTION_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_auction_console, container, false);
        tvAuctionType = rootView.findViewById(R.id.tvAuctionType);
        tvStateMsg = rootView.findViewById(R.id.tvStateMsg);
        tvMinPrice = rootView.findViewById(R.id.tvMinPrice);
        tvUserBid = rootView.findViewById(R.id.tvUserBid);
        tvFinalPrice = rootView.findViewById(R.id.tvFinalPrice);
        tvFinalPrice.setText("/");
        tvAuctionResult = rootView.findViewById(R.id.tvAuctionResult);
        tvAuctionResult.setText("/");
        btnChangeBid = rootView.findViewById(R.id.btnChangeBid);
        btnChangeBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = ChangeBidDialogFragment.newInstance(startingPrice);
                changeBidDialogFragment = dialogFragment;
                dialogFragment.setTargetFragment(AuctionConsoleFragment.this, 42);
                dialogFragment.show(getActivity().getSupportFragmentManager(), "null");
            }
        });
        btnLeave = rootView.findViewById(R.id.btnLeave);
        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.leaveAuction(auctionId);
            }
        });
        viewModel = new ViewModelProvider(
                this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()))
                .get(AuctionConsoleViewModel.class);
        viewModel.getAuctionWithTaskById(auctionId).observe(getViewLifecycleOwner(), new Observer<AuctionWithTask>() {
            @Override
            public void onChanged(AuctionWithTask auctionWithTask) {
                if(auctionWithTask != null){
                    handleUpdate(auctionWithTask);
                }
            }
        });
        return rootView;
    }

    /**
     * Updates the components of the graphical interface.
     * @param auctionWithTask auction with its corresponding task.
     */
    void handleUpdate(AuctionWithTask auctionWithTask){
        if(auctionWithTask.getAuction() != null) {
            getActivity().setTitle(auctionWithTask.getAuction().getTitle());
            startingPrice = auctionWithTask.getAuction().getStartPrice();
            tvMinPrice.setText(String.valueOf(auctionWithTask.getAuction().getStartPrice()));
            tvAuctionType.setText(String.valueOf(auctionWithTask.getAuction().getAuctionType()));
        }
        if(auctionWithTask.getTask() != null){
            AuctionTask auctionTask = auctionWithTask.getTask();
            tvUserBid.setText(String.valueOf(auctionTask.getCurrentBid()));
            if(auctionTask.getLocalPhase() != null){
                switch (auctionTask.getLocalPhase()){
                    case Registration:
                        tvStateMsg.setText("Anmeldung zur Auktion läuft.");
                        break;
                    case Running:
                        tvStateMsg.setText("Auktion wird ausgeführt.");
                        if(changeBidDialogFragment != null) changeBidDialogFragment.dismiss();
                        disableButtons();
                        break;
                    case Completion:
                        tvStateMsg.setText("Auktion ist abgeschlossen.");
                        tvFinalPrice.setText(String.valueOf(auctionTask.getFinalPrice()));
                        tvAuctionResult.setText(auctionTask.isHasWon() ? "Gewonnen" : "Verloren");
                        disableButtons();
                        break;
                    case Abortion:
                        tvStateMsg.setText("Die Auktion kann aufgrund eines Fehlers nicht ausgeführt werden.");
                        //tvStateMsg.setText(auctionTask.getErrorMessage()); use this to get a detailed error message.
                        if(changeBidDialogFragment != null) changeBidDialogFragment.dismiss();
                        disableButtons();
                        break;
                }
            }
        }
    }

    @Override
    public void onChangeClick(DialogFragment dialog, int newBid) {
        dialog.dismiss();
        this.changeBidDialogFragment = null;
        viewModel.changeBid(auctionId, newBid);
    }

    @Override
    public void onCloseDialogClick(DialogFragment dialog) {
        dialog.dismiss();
        this.changeBidDialogFragment = null;
    }

    /**
     * Disables the leave and change bid buttons
     */
    private void disableButtons(){
        btnLeave.setEnabled(false);
        btnChangeBid.setEnabled(false);
    }
}
