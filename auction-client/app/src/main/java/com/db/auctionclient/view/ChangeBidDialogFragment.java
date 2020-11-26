package com.db.auctionclient.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.db.auctionclient.R;

/**
 * A pop up window, which allows the user to change his current bid.
 */
public class ChangeBidDialogFragment extends DialogFragment {
    // Codes for bundling and retrieving the data.
    private static final String PARAM_STARTING_PRICE = "PARAM_STARTING_PRICE";

    private int startingPrice;
    // Components of the graphical interface.
    private EditText etNewUserBid;
    private Button btnChangeBid;
    private Button btnCloseDialog;
    private DialogListener listener;

    public ChangeBidDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method for creating a new instance of {@link ChangeBidDialogFragment}.
     * @param startingPrice starting price of the auction.
     * @return A new instance of fragment ChangeBidDialogFragment.
     */
    public static ChangeBidDialogFragment newInstance(int startingPrice) {
        ChangeBidDialogFragment fragment = new ChangeBidDialogFragment();
        Bundle args = new Bundle();
        args.putInt(PARAM_STARTING_PRICE, startingPrice);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_fragment_change_bid, null);
        listener = (DialogListener) getTargetFragment();
        etNewUserBid = dialogView.findViewById(R.id.etNewUserBid);
        btnChangeBid = dialogView.findViewById(R.id.btnChangeBid);
        btnChangeBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etNewUserBid.getText().length() == 0) etNewUserBid.setText("0");
                int userBid = Integer.parseInt(etNewUserBid.getText().toString());
                if(userBid >= startingPrice){
                    listener.onChangeClick(ChangeBidDialogFragment.this, userBid);
                }else{
                    etNewUserBid.setError("Das Gebot darf nicht niedriger sein als der Startpreis.");
                }
            }
        });
        btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCloseDialogClick(ChangeBidDialogFragment.this);
            }
        });
        dialogBuilder.setView(dialogView);
        return dialogBuilder.setView(dialogView).create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            startingPrice = getArguments().getInt(PARAM_STARTING_PRICE);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    /**
     * Click event listener of {@link ChangeBidDialogFragment}.
     */
    public interface DialogListener{
        /**
         * Callback method, which is triggered, when the change button is clicked.
         * @param dialog instance of the dialog fragment.
         * @param newBid the new bid, which was set by the user.
         */
        public void onChangeClick(DialogFragment dialog, int newBid);

        /**
         * Callback method, which is triggered, when the close button is clicked.
         * @param dialog instance of the dialog fragment.
         */
        public void onCloseDialogClick(DialogFragment dialog);
    }
}
