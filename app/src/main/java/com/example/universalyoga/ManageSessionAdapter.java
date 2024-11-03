package com.example.universalyoga;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ManageSessionAdapter extends RecyclerView.Adapter<ManageSessionAdapter.ViewHolder> {

    private List<Session> sessionList;
    YogaDatabaseHelper dbHelper;
    View view;


    public ManageSessionAdapter(List<Session> sessionList) {
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

         view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_session, parent, false);
        dbHelper = new YogaDatabaseHelper(view.getContext());
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Session session = sessionList.get(position);
        Log.d("test", session.getDate().toString());
        holder.txtSessionDate.setText(session.getDate());
        holder.txtInstructorName.setText("Instructor: " + session.getInstructorId());
        holder.txtComment.setText("Comments: " + session.getComment());
        holder.txtSession.setText(dbHelper.getYogaClassById(session.getClassId()).getType());

        holder.btnEdit.setOnClickListener(v -> {
            Intent editSession = new Intent(view.getContext(), EditSessionActivity.class);
            editSession.putExtra("SessionId", session.getId());
            Log.e("test", String.valueOf(session.getId()));
            view.getContext().startActivity(editSession);
        });

        holder.btnDel.setOnClickListener(v -> {
            // Confirm delete action
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Delete Session")
                    .setMessage("Are you sure you want to delete this session?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete session from the database
                        boolean isDeleted = dbHelper.deleteSessionById(session.getId());
                        if (isDeleted) {
                            // Remove session from list and notify adapter
                            sessionList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, sessionList.size());
                            Toast.makeText(view.getContext(), "Session deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(view.getContext(), "Failed to delete session", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }



    public void updateSessionList(List<Session> newSessionList) {
        this.sessionList = newSessionList;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSessionDate;
        TextView txtInstructorName;
        TextView txtComment;
        TextView txtSession;

        ImageButton btnEdit;
        ImageButton btnDel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSession = itemView.findViewById(R.id.txtMSession);
            txtSessionDate = itemView.findViewById(R.id.txtMSessionDate);
            txtComment = itemView.findViewById(R.id.txtMSessionComent);
            txtInstructorName = itemView.findViewById(R.id.txtMSessionInstructor);
            btnEdit = itemView.findViewById(R.id.btnMSessionEdit);
            btnDel = itemView.findViewById(R.id.btnMSessionDel);
        }
    }
}
