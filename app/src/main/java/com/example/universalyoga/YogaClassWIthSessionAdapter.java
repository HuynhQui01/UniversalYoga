package com.example.universalyoga;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class YogaClassWIthSessionAdapter extends RecyclerView.Adapter<YogaClassWIthSessionAdapter.ViewHolder> {
    private List<Yoga> yogaClassList;
    YogaDatabaseHelper dbHelper;
    SessionAdapter sessionAdapter;


    public YogaClassWIthSessionAdapter(List<Yoga> yogaClassList) {
        this.yogaClassList = yogaClassList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_yoga_class, parent, false);
        dbHelper = new YogaDatabaseHelper(view.getContext());

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Yoga yogaClass = yogaClassList.get(position);
        List<Session> lstSession = dbHelper.GetSessionByClassId(yogaClass.getId());
        holder.textViewClassName.setText(yogaClass.getType());
        holder.textViewClassTime.setText( "Time: " + yogaClass.getTime());
        holder.txtViewCapacity.setText("Capacity: " + yogaClass.getCapacity());
        holder.txtDayOfWeek.setText(yogaClass.getDayOfWeek());
        holder.txtPrice.setText("Price: " + yogaClass.getPrice());
        holder.txtDes.setText(("Description: " + yogaClass.getDescription()));

        holder.rcvSession.setLayoutManager(new LinearLayoutManager(holder.rcvSession.getContext()));

        sessionAdapter = new SessionAdapter(lstSession);
        holder.rcvSession.setAdapter(sessionAdapter);

        holder.btnView.setOnClickListener(v ->{

            holder.rcvSession.setVisibility(View.VISIBLE);
        });

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
        Button btnView;
        TextView txtPrice;
        TextView txtDes;
        RecyclerView rcvSession;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewClassName = itemView.findViewById(R.id.textViewClassName);
            textViewClassTime = itemView.findViewById(R.id.textViewClassTime);
            txtViewCapacity = itemView.findViewById(R.id.txtCapacity);
            txtDayOfWeek = itemView.findViewById(R.id.txtDayOfWeek);
            btnView = itemView.findViewById(R.id.btnViewSession);
            txtPrice = itemView.findViewById(R.id.txtClassPrice);
            txtDes = itemView.findViewById(R.id.txtClassDescription);
            rcvSession = itemView.findViewById(R.id.rcvSession);
        }
    }
}

