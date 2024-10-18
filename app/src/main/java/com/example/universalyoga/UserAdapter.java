package com.example.universalyoga;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;


    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        User user = userList.get(holder.getAdapterPosition());
        holder.txtUserName.setText(user.getUsername());
        holder.txtUserEmail.setText(user.getEmail());
        holder.txtPhone.setText(user.getPhone());
        holder.txtRole.setText(user.getRole());

        holder.imgBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang activity hoặc dialog để chỉnh sửa user
                Intent intent = new Intent(v.getContext(), EditProfileActivity.class);
                intent.putExtra("userEmail", user.getEmail());
                v.getContext().startActivity(intent);
            }
        });

        holder.imgBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete User")
                        .setMessage("Are you sure you want to delete this user?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                YogaDatabaseHelper dbHelper = new YogaDatabaseHelper(v.getContext());
                                dbHelper.deleteUserByEmail(user.getEmail());

                                userList.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName;
        TextView txtUserEmail;
        TextView txtPhone;
        TextView txtRole;
        ImageButton imgBtnEdit;
        ImageButton imgBtnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserName = itemView.findViewById(R.id.txtMUserName);
            txtUserEmail = itemView.findViewById(R.id.txtMUserEmail);
            txtPhone = itemView.findViewById(R.id.txtMuserphone);
            txtRole = itemView.findViewById(R.id.txtMRole);
            imgBtnDelete = itemView.findViewById(R.id.imgbtnDelUser);
            imgBtnEdit = itemView.findViewById(R.id.imgbtnEditUser);
        }
    }
}
