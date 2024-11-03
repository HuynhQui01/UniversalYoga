package com.example.universalyoga;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    SessionManger sessionManger;
    Button btnLogout;
    Button btnLogin;
    Context context;
    TextView txtName;
    TextView txtPhone;
    TextView txtRole;
    TextView txtEmail;
    YogaDatabaseHelper dbHelper;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ManageLoginState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        // Inflate the layout for this fragment
        Mapping(view);
        ManageLoginState();


        return view;
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    sessionManger.logoutUser(context);
                    ManageLoginState();
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    void ManageLoginState(){
        if(!sessionManger.isUserLoggedIn(context)){
            btnLogout.setVisibility(View.INVISIBLE);
            txtName.setVisibility(View.INVISIBLE);
            txtPhone.setVisibility(View.INVISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            txtName.setText("");
            txtPhone.setText("");
            btnLogin.setOnClickListener(v ->{
                Intent login = new Intent(context, LoginActivity.class);
                startActivity(login);
            });

        }else{
            btnLogin.setVisibility(View.INVISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            txtName.setVisibility(View.VISIBLE);
            txtPhone.setVisibility(View.VISIBLE);

            btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
            User user = dbHelper.getUserByEmail(sessionManger.getUserEmail(context));

            txtName.setText("User name: " + user.getUsername());
            txtPhone.setText("Phone: "+user.getPhone());
            txtEmail.setText("Email: "+user.getEmail());
            txtRole.setText("Role: "+user.getRole().toUpperCase(Locale.ROOT));

        }
    }

    void Mapping(View view){
        context = getContext();
        sessionManger = new SessionManger();
        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogin = view.findViewById(R.id.btnLogin);
        txtName = view.findViewById(R.id.txtProfileName);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtRole = view.findViewById(R.id.txtProfileRole);
        txtEmail = view.findViewById(R.id.txtProfileEmail);
        dbHelper = new YogaDatabaseHelper(context);
    }
}