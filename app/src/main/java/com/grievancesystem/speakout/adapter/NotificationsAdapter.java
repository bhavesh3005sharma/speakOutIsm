package com.grievancesystem.speakout.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.grievancesystem.speakout.R;
import com.grievancesystem.speakout.models.Notification;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.text.DateFormat.getDateTimeInstance;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.viewHolder> {
    List<Notification> list;
    Context context;
    NotificationsAdapter.NotificationsListener mListener;

    public NotificationsAdapter(Context context, List list) {
        this.context = context;
        this.list = list;
    }

    public void setUpOnNotificationListener(NotificationsAdapter.NotificationsListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationsAdapter.viewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.viewHolder holder, int position) {
        Notification notification = list.get(position);
        holder.title.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());
        String time = null;
        long timeStamp = 0;
        Map map = notification.getTimeStampMap();
        if (map != null) {
            timeStamp = (long) map.get("timeStamp");
            DateFormat dateFormat = getDateTimeInstance();
            Date netDate = (new Date(timeStamp));
            time = dateFormat.format(netDate);
        }
        holder.date.setText(time);
        if (notification.getComplaint_id() == null) holder.chip.setVisibility(View.GONE);
        else holder.chip.setVisibility(View.VISIBLE);

        holder.chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.chipClicked(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }

    public interface NotificationsListener {
        void chipClicked(Notification notification);
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        TextView title, message, date;
        Chip chip;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title_noti);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message_noti);
            chip = itemView.findViewById(R.id.action_chip);
        }
    }
}
