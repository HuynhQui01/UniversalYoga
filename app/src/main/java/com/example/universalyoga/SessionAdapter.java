package com.example.universalyoga;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {

    private List<Session> lstSession;
    YogaDatabaseHelper dbHelper;

    public SessionAdapter(List<Session> lstSession) {
        this.lstSession = lstSession;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each session item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        dbHelper = new YogaDatabaseHelper(view.getContext());
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the session at the current position
        Session session = lstSession.get(position);

        holder.txtDate.setText("Date start: " + session.getDate());
        holder.txtComment.setText("Comment: "+session.getComment());
        holder.txtInstructor.setText(dbHelper.getUserNameById(session.getInstructorId()));


    }

    @Override
    public int getItemCount() {
        return lstSession.size();
    }

    // Define the ViewHolder for the session item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate;
        TextView txtComment;
        TextView txtInstructor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtSessionDate);
            txtComment = itemView.findViewById(R.id.txtSessionComment);
            txtInstructor = itemView.findViewById(R.id.txtSessionInstructorName);
        }
    }
}
