package com.example.universalyoga;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class YogaClassAdapter extends RecyclerView.Adapter<YogaClassAdapter.ViewHolder> {
    private List<Yoga> yogaClassList;


    public YogaClassAdapter(List<Yoga> yogaClassList) {
        this.yogaClassList = yogaClassList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_yoga_class, parent, false); // Create a new layout for individual class item
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Yoga yogaClass = yogaClassList.get(position);
        holder.textViewClassName.setText(yogaClass.getType()); // Assuming type is the name
        holder.textViewClassTime.setText( "Time :" + yogaClass.getTime());
        holder.txtViewCapacity.setText("Capacity: " + yogaClass.getCapacity());
        holder.txtDayOfWeek.setText(yogaClass.getDayOfWeek());// Assuming time is a field in Yoga
        // Set other fields as needed
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewClassName = itemView.findViewById(R.id.textViewClassName);
            textViewClassTime = itemView.findViewById(R.id.textViewClassTime);
            txtViewCapacity = itemView.findViewById(R.id.txtCapacity);
            txtDayOfWeek = itemView.findViewById(R.id.txtDayOfWeek);
        }
    }
}

