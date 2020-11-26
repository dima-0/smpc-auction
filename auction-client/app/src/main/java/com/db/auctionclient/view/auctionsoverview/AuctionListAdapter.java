package com.db.auctionclient.view.auctionsoverview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.db.auctionclient.R;
import com.db.auctionclient.model.entities.Auction;
import com.db.auctionclient.view.ListItemListener;

import java.util.List;

/**
 * Adapter, which is responsible for binding auction data to the views of the items in
 * the recycler view.
 */
public class AuctionListAdapter extends RecyclerView.Adapter<AuctionListAdapter.AuctionHolder> {
    /** List of auctions.*/
    private List<Auction> auctions;
    /** Event listener, which is used when an item is clicked. */
    protected ListItemListener<Auction> onClickListener;

    /**
     * @param auctions list of auctions.
     * @param onClickListener event listener, which is used when an item is clicked.
     */
    public AuctionListAdapter(List<Auction> auctions, ListItemListener<Auction> onClickListener) {
        this.auctions = auctions;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public AuctionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View auctionView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_auction_entry, parent, false);
        return new AuctionHolder(auctionView);
    }

    @Override
    public void onBindViewHolder(@NonNull AuctionHolder holder, int position) {
        Auction auction = auctions.get(position);
        holder.rootItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(auction);
            }
        });
        holder.tvTitle.setText(auction.getTitle());
        holder.tvStartingDate.setText("01.01.2000, 12:05");
        holder.tvMinPrice.setText(String.valueOf(auction.getStartPrice()));
    }

    @Override
    public int getItemCount() {
        return auctions.size();
    }

    /**
     * Updates the data in the recycler view.
     * @param auctions new list of auctions.
     */
    public void setAuctions(List<Auction> auctions){
        this.auctions = auctions;
        notifyDataSetChanged();
    }

    /**
     * Holds the state of an item, which shows auction information.
     */
    public static class AuctionHolder extends RecyclerView.ViewHolder{
        // Components of the graphical interface.
        private View rootItemView;
        private TextView tvTitle;
        private TextView tvStartingDate;
        private TextView tvMinPrice;

        public AuctionHolder(@NonNull View itemView) {
            super(itemView);
            rootItemView = itemView;
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStartingDate = itemView.findViewById(R.id.tvStartingDate);
            tvMinPrice = itemView.findViewById(R.id.tvMinPrice);
        }
    }

}
