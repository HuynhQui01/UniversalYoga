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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManageFragment extends Fragment {

    Button btnManageAccount;
    Button btnManageClass;
    Button btnManageSession;
    Button btnManageResetData;
    SessionManger sessionManger;
    Context context;
    YogaDatabaseHelper dbHelper;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ManageFragment() {
        // Required empty public constructor
    }
    public static ManageFragment newInstance(String param1, String param2) {
        ManageFragment fragment = new ManageFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage, container, false);
        Mapping(view);

            btnManageAccount.setOnClickListener(v -> {
                Intent manageAccount = new Intent(context, ManageAccountActivity.class);
                startActivity(manageAccount);
            });
        btnManageClass.setOnClickListener(v -> {
            Intent manageClass = new Intent(context, ManageClassActivity.class);
            startActivity(manageClass);
        });

        btnManageSession.setOnClickListener(v ->{
            Intent manageSession = new Intent(context, ManageSessionActivity.class);
            startActivity(manageSession);
        });
        btnManageResetData.setOnClickListener(v -> {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Confirm delete")
                    .setMessage("Are you sure to delete all data?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        dbHelper.deleteAllDataOfFire();
                        dbHelper.resetDataInSQLite();

                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        return view;
    }

    void Mapping(View view){

        context = getContext();
        sessionManger = new SessionManger();
        btnManageAccount = view.findViewById(R.id.btnManageAccount);
        btnManageClass = view.findViewById(R.id.btnManageClass);
        btnManageSession = view.findViewById(R.id.btnCreateSession);
        btnManageResetData = view.findViewById(R.id.btnManageReset);
        dbHelper = new YogaDatabaseHelper(context);
    }
}