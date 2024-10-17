package com.example.universalyoga;

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

    Button btnCreatAcc;
    Button btnCreateClass;
    Button btnCreateSession;

    SessionManger sessionManger;
    Context context;
    YogaDatabaseHelper dbHelper;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ManageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ManageFragment.
     */
    // TODO: Rename and change types and number of parameters
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

            btnCreatAcc.setOnClickListener(v -> {
                Intent createAcc = new Intent(context, CreateAccount.class);
                startActivity(createAcc);
            });
        btnCreateClass.setOnClickListener(v -> {
            Intent createClass = new Intent(context, CreateClassActivity.class);
            startActivity(createClass);
        });

        btnCreateSession.setOnClickListener(v ->{
            Intent createSession = new Intent(context, CreateSessionActivity.class);
            startActivity(createSession);
        });




        return view;
    }

    void Mapping(View view){
        context = getContext();
        sessionManger = new SessionManger();
        btnCreatAcc = view.findViewById(R.id.btnCreateAcc);
        btnCreateClass = view.findViewById(R.id.btnCreateClass);
        btnCreateSession = view.findViewById(R.id.btnCreateSession);
        dbHelper = new YogaDatabaseHelper(context);
    }
}