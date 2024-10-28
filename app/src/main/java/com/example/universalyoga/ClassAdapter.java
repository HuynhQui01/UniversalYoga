package com.example.universalyoga;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder>{
    private List<Yoga> yogaClassList;
    View view;
    YogaDatabaseHelper dbHelper;


    public ClassAdapter(List<Yoga> yogaClassList) {
        this.yogaClassList = yogaClassList;
    }

    @NonNull
    @Override
    public ClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.manage_item_yoga_class, parent, false);
         dbHelper = new YogaDatabaseHelper(view.getContext());
        return new ClassAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassAdapter.ViewHolder holder, int position) {
        Yoga yogaClass = yogaClassList.get(position);

        holder.textViewClassName.setText(yogaClass.getType());
        holder.textViewClassTime.setText( "Time: " + yogaClass.getTime());
        holder.txtViewCapacity.setText("Capacity: " + yogaClass.getCapacity());
        holder.txtDayOfWeek.setText(yogaClass.getDayOfWeek());
        holder.txtDuration.setText("Duration: "+yogaClass.getDuration()+" minutes");
        holder.txtPrice.setText("Price: " + yogaClass.getPrice());
        holder.txtDes.setText(("Description: " + yogaClass.getDescription()));

        holder.btnEit.setOnClickListener(v ->{
            Intent editClass = new Intent(view.getContext(), EditClassActivity.class);
            editClass.putExtra("ClassId", yogaClass.getId());
            view.getContext().startActivity(editClass);
        });

        holder.btnDel.setOnClickListener(v -> {
            showDeleteConfirmationDialog(view.getContext(), yogaClass.getId());
        });

    }

    private void showDeleteConfirmationDialog(Context context, int classId) {
        new AlertDialog.Builder(context)
                .setTitle("Confirm Delete")
                .setMessage("This action will delete ALL SESSIONS in this class. Are you sure to delete?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    dbHelper.deleteYogaClassById(classId);
                    yogaClassList.removeIf(yoga -> yoga.getId() == classId);


                    notifyDataSetChanged();
                    Toast.makeText(view.getContext(), "Yoga class deleted successfully.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return yogaClassList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewClassName;
        TextView textViewClassTime;
        TextView txtViewCapacity;
        TextView txtDayOfWeek;
        TextView txtDuration;
        ImageButton btnEit;
        ImageButton btnDel;
        TextView txtPrice;
        TextView txtDes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewClassName = itemView.findViewById(R.id.txtMClassType);
            textViewClassTime = itemView.findViewById(R.id.txtMClassTime);
            txtViewCapacity = itemView.findViewById(R.id.txtMClassCapacity);
            txtDayOfWeek = itemView.findViewById(R.id.txtMClassDayOfWeek);
            txtDuration = itemView.findViewById(R.id.txtMClassDuration);
            btnEit = itemView.findViewById(R.id.btnMEditClass);
            btnDel = itemView.findViewById(R.id.btnMDelClass);
            txtPrice = itemView.findViewById(R.id.txtMClassPrice);
            txtDes = itemView.findViewById(R.id.txtMClassDes);
        }
    }
}


