package com.example.appitup.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appitup.R;
import com.example.appitup.models.Complaints;
import com.example.appitup.utility.Helper;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ComplaintsAdapter extends RecyclerView.Adapter implements Filterable {
    Context context;
    List<Complaints> list;
    List<Complaints> filteredList1;
    ComplaintsListener mListener;
    int cardType = 1; // 1-> Home Type Complaint card : 2-> Trending Type Complaint card

    public ComplaintsAdapter(Context context, List<Complaints> list) {
        this.context = context;
        this.list = list;
        filteredList1 = list;
    }

    public ComplaintsAdapter(Context context, List<Complaints> list, int cardType) {
        this.context = context;
        this.list = list;
        filteredList1 = list;
        this.cardType = cardType;
    }

    public void setUpOnComplaintListener(ComplaintsListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_complaints, parent, false);
            return new viewHolderComplaintCard(view);
        } else if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trending_complaint_card, parent, false);
            return new viewHolderTrendingCard(view);
        }
        return null;
    }

    private final Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Complaints> filteredList = new ArrayList<>();
            if (charSequence == null || getItemCount() == 0) {
                filteredList.addAll(filteredList1);
            } else {
                list.size();
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Complaints complaint : filteredList1) {
                    if (complaint.getStatus().toLowerCase().contains(filterPattern))
                        filteredList.add(complaint);
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            list.clear();
            if (filterResults.values != null)
                list.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder main_holder, int position) {
        Complaints complaints = list.get(position);
        if (cardType == 1) {
            viewHolderComplaintCard holder = (viewHolderComplaintCard) main_holder;
            if (complaints.getAnonymous().equals("true")) holder.textUserName.setText("@Anonymous");
            else holder.textUserName.setText("@" + complaints.getUsername());
            holder.textViewTitle.setText(complaints.getSubject());
            holder.textViewBody.setText(complaints.getBody());
            holder.textViewDateTime.setText(complaints.getTimeStampStr());

            holder.chipStatus.setText(complaints.getStatus());
            if (complaints.getStatus().equals(Helper.PENDING)) {
                holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(context.getResources().getColor(R.color.pending_color)));
            } else if (complaints.getStatus().equals(Helper.IN_PROGRESS)) {
                holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(context.getResources().getColor(R.color.inprogress_color)));
            } else if (complaints.getStatus().equals(Helper.RESOLVED)) {
                holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(context.getResources().getColor(R.color.resolved_color)));
            }
            holder.chipCategory.setText(complaints.getCategory());
            holder.chipSubcategory.setText(complaints.getSubcategory());

            final long[] upVoters = {complaints.getUpvotes()};
            final long[] downVoters = {complaints.getDownvotes()};
            int commenter = (complaints.getListOfCommenter() != null) ? complaints.getListOfCommenter().size() : 0;

            holder.upVoteNumber.setText(upVoters[0] + " upvotes");
            holder.downVoteNumber.setText(downVoters[0] + " downvotes");
            holder.commentNumber.setText(commenter + " comments");

            if (complaints.getVoteStatus() == Helper.UPVOTED) {
                holder.upVote.setImageResource(R.drawable.ic_upvote_filled);
                holder.downVote.setImageResource(R.drawable.ic_downvote_outlined);
            } else if (complaints.getVoteStatus() == Helper.DOWNVOTED) {
                holder.downVote.setImageResource(R.drawable.ic_downvote_filled);
                holder.upVote.setImageResource(R.drawable.ic_upvote_outlined);
            }

            holder.upVote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (complaints.getVoteStatus() == Helper.DOWNVOTED) {
                        holder.upVoteNumber.setText(upVoters[0] + 1 + " upvotes");
                        holder.downVoteNumber.setText(downVoters[0] - 1 + " downvotes");
                    } else if (complaints.getVoteStatus() == Helper.NOT_VOTED) {
                        holder.upVoteNumber.setText(upVoters[0] + 1 + " upvotes");
                        holder.downVoteNumber.setText(downVoters[0] + " downvotes");
                    } else if (complaints.getVoteStatus() == Helper.UPVOTED) {
                        holder.upVoteNumber.setText(upVoters[0] + " upvotes");
                        holder.downVoteNumber.setText(downVoters[0] + " downvotes");
                    }

                    holder.upVote.setImageResource(R.drawable.ic_upvote_filled);
                    holder.downVote.setImageResource(R.drawable.ic_downvote_outlined);
                    if (mListener != null)
                        mListener.upVoteClicked(complaints);
                }
            });

            holder.downVote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (complaints.getVoteStatus() == Helper.UPVOTED) {
                        holder.upVoteNumber.setText(upVoters[0] - 1 + " upvotes");
                        holder.downVoteNumber.setText(downVoters[0] + 1 + " downvotes");
                    } else if (complaints.getVoteStatus() == Helper.NOT_VOTED) {
                        holder.upVoteNumber.setText(upVoters[0] + " upvotes");
                        holder.downVoteNumber.setText(downVoters[0] + 1 + " downvotes");
                    } else if (complaints.getVoteStatus() == Helper.DOWNVOTED) {
                        holder.upVoteNumber.setText(upVoters[0] + " upvotes");
                        holder.downVoteNumber.setText(downVoters[0] + " downvotes");
                    }

                    holder.downVote.setImageResource(R.drawable.ic_downvote_filled);
                    holder.upVote.setImageResource(R.drawable.ic_upvote_outlined);
                    if (mListener != null)
                        mListener.downVoteClicked(complaints);
                }
            });

            holder.comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null)
                        mListener.commentsClicked(complaints);
                }
            });

            holder.textUserName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null)
                        mListener.usernameClicked(complaints);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onCardClicked(complaints);
                    }
                }
            });
        } else if (cardType == 2) {
            viewHolderTrendingCard holder = (viewHolderTrendingCard) main_holder;
            holder.textViewNumber.setText(1 + position + ".");
            holder.textViewCategory.setText(complaints.getCategory() + " .Trending");
            holder.textViewTitle.setText(complaints.getSubject());
            holder.textViewUpvotes.setText(complaints.getUpvotes() + " Upvotes");
            if (complaints.getStatus().equals(Helper.PENDING))
                holder.cardViewAppStatus.setCardBackgroundColor(context.getResources().getColor(R.color.pending_color));
            if (complaints.getStatus().equals(Helper.IN_PROGRESS))
                holder.cardViewAppStatus.setCardBackgroundColor(context.getResources().getColor(R.color.inprogress_color));
            if (complaints.getStatus().equals(Helper.RESOLVED))
                holder.cardViewAppStatus.setCardBackgroundColor(context.getResources().getColor(R.color.resolved_color));
        }
    }

    public interface ComplaintsListener {
        void upVoteClicked(Complaints complaint);

        void downVoteClicked(Complaints complaint);

        void commentsClicked(Complaints complaint);

        void onCardClicked(Complaints complaints);

        void usernameClicked(Complaints complaint);
    }

    public static class viewHolderComplaintCard extends RecyclerView.ViewHolder {
        TextView textUserName, textViewDateTime, textViewTitle, textViewBody, upVoteNumber, downVoteNumber, commentNumber;
        Chip chipCategory, chipSubcategory, chipStatus;
        ImageButton upVote, downVote, comment;

        public viewHolderComplaintCard(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.textViewUsername);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewBody = itemView.findViewById(R.id.textViewBody);
            chipCategory = itemView.findViewById(R.id.chipCategory);
            chipSubcategory = itemView.findViewById(R.id.chipSubcategory);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            upVoteNumber = itemView.findViewById(R.id.upVoteNumber);
            downVoteNumber = itemView.findViewById(R.id.downVoteNumber);
            commentNumber = itemView.findViewById(R.id.commentNumber);
            upVote = itemView.findViewById(R.id.upVote);
            downVote = itemView.findViewById(R.id.downVote);
            comment = itemView.findViewById(R.id.comment);
        }
    }

    public static class viewHolderTrendingCard extends RecyclerView.ViewHolder {
        TextView textViewNumber, textViewCategory, textViewUpvotes, textViewTitle;
        CardView cardViewAppStatus;

        public viewHolderTrendingCard(@NonNull View itemView) {
            super(itemView);
            textViewNumber = itemView.findViewById(R.id.textViewNumber);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewUpvotes = itemView.findViewById(R.id.textViewUpvotes);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            cardViewAppStatus = itemView.findViewById(R.id.cardViewAppStatus);
        }
    }
}
