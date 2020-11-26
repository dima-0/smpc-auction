package com.db.auctionclient.view.runningauctions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.db.auctionclient.R;
import com.db.auctionclient.model.entities.AuctionWithTask;
import com.db.auctionclient.view.ListItemListener;

import java.util.List;

/**
 * Adapter, which is responsible for binding auction task data to the views of the items in
 * the recycler view.
 */
public class AuctionTasksAdapter extends RecyclerView.Adapter<AuctionTasksAdapter.AuctionTaskHolder>{
    /** List of auctions and the corresponding task.*/
    private List<AuctionWithTask> auctionTasks;
    /** Event listener, which is used when an item is clicked.*/
    private ListItemListener<AuctionWithTask> onClickListener;

    /**
     * @param auctionTasks list of auctions and the corresponding task.
     * @param onClickListener event listener, which is used when an item is clicked.
     */
    public AuctionTasksAdapter(List<AuctionWithTask> auctionTasks, ListItemListener<AuctionWithTask> onClickListener) {
        this.auctionTasks = auctionTasks;
        this.onClickListener = onClickListener;
    }

    /**
     * Updates the data in the recycler view.
     * @param auctionTasks new list of auctions and its tasks.
     */
    public void setAuctionTasks(List<AuctionWithTask> auctionTasks){
        this.auctionTasks = auctionTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AuctionTaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View auctionTaskView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_auctiontask_entry, parent, false);
        return new AuctionTaskHolder(auctionTaskView);
    }

    @Override
    public void onBindViewHolder(@NonNull AuctionTaskHolder holder, int position) {
        AuctionWithTask auctionTask = auctionTasks.get(position);
        holder.rootItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(auctionTask);
            }
        });
        holder.tvTitle.setText(auctionTask.getAuction().getTitle());
        holder.tvStartingDate.setText("01.01.2000, 12:05");
        holder.tvMinPrice.setText(String.valueOf(auctionTask.getAuction().getStartPrice()));
        holder.tvUserBid.setText(String.valueOf(auctionTask.getTask().getCurrentBid()));
    }

    @Override
    public int getItemCount() {
        return auctionTasks.size();
    }

    /**
     * Holds the state of an item, which shows auction task information.
     */
    public static class AuctionTaskHolder extends RecyclerView.ViewHolder{
        // Components of the graphical interface.
        private View rootItemView;
        private TextView tvTitle;
        private TextView tvStartingDate;
        private TextView tvMinPrice;
        private TextView tvUserBid;

        public AuctionTaskHolder(@NonNull View itemView) {
            super(itemView);
            rootItemView = itemView;
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStartingDate = itemView.findViewById(R.id.tvStartingDate);
            tvMinPrice = itemView.findViewById(R.id.tvMinPrice);
            tvUserBid = itemView.findViewById(R.id.tvUserBid);
        }
    }
}
