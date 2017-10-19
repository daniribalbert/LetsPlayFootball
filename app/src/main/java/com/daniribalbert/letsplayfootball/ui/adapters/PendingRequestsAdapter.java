package com.daniribalbert.letsplayfootball.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.RequestsDbUtils;
import com.daniribalbert.letsplayfootball.data.model.JoinLeagueRequest;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter which displays information about the user Pending Requests.
 */
public class PendingRequestsAdapter
        extends RecyclerView.Adapter<PendingRequestsAdapter.PendingRequestViewHolder> {

    List<JoinLeagueRequest> mPendingRequestList = new ArrayList<>();

    @Override
    public PendingRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_join_league_request, parent, false);

        final PendingRequestViewHolder pendingRequestViewHolder = new PendingRequestViewHolder(
                view);

        view.findViewById(R.id.bt_accept_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = pendingRequestViewHolder.getAdapterPosition();
                RequestsDbUtils.acceptJoinLeagueRequest(mPendingRequestList.get(position));
                removeItem(position);
            }
        });

        view.findViewById(R.id.bt_remove_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = pendingRequestViewHolder.getAdapterPosition();
                RequestsDbUtils.removeJoinLeagueRequest(mPendingRequestList.get(position));
                removeItem(position);
            }
        });
        return pendingRequestViewHolder;
    }

    private void removeItem(int position) {
        mPendingRequestList.remove(position);
        notifyItemChanged(position);
    }

    @Override
    public void onBindViewHolder(PendingRequestViewHolder holder, int position) {
        JoinLeagueRequest request = mPendingRequestList.get(position);
        holder.setRequest(request);

    }

    @Override
    public int getItemCount() {
        return mPendingRequestList.size();
    }

    public void clear() {
        mPendingRequestList.clear();
        notifyDataSetChanged();
    }

    public void addItems(List<JoinLeagueRequest> pendingRequests) {
        int itemCount = getItemCount();
        mPendingRequestList.addAll(pendingRequests);
        notifyItemRangeInserted(itemCount, pendingRequests.size());
    }

    class PendingRequestViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card_request_receiver)
        ImageView mReceiverImage;

        @BindView(R.id.card_request_sender)
        ImageView mSenderImage;

        @BindView(R.id.card_request_text)
        TextView mRequestText;

        public PendingRequestViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setRequest(JoinLeagueRequest request) {
            String requestText;
            if (request.isPlayerRequest()) {
                GlideUtils.loadCircularImage(request.senderImage, mSenderImage);
                GlideUtils.loadCircularImage(request.league.image, mReceiverImage);
                requestText = mRequestText.getContext()
                                          .getString(R.string.request_manage_player_join_league,
                                                     request.senderName, request.league.title);
            } else {
                GlideUtils.loadCircularImage(request.league.image, mSenderImage);
                GlideUtils.loadCircularImage(request.senderImage, mReceiverImage);
                requestText = mRequestText.getContext()
                                          .getString(R.string.request_player_join_league,
                                                     request.league.title);
            }
            mRequestText.setText(requestText);
        }
    }
}
