package com.example.universalyoga;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private RecyclerView rcvClass;
    private ImageView imageView;
    private Handler handler;
    private Runnable runnable;
    private int[] imageArray = {R.drawable.yoga1, R.drawable.yoga2, R.drawable.yoga3};
    private int currentIndex = 0;
    SessionAdapter sessionAdapter;

    YogaDatabaseHelper dbHelper;
    SessionManger sessionManger;

    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        dbHelper = new YogaDatabaseHelper(rootView.getContext());
        sessionManger = new SessionManger();

        imageView = rootView.findViewById(R.id.imgMain);
        rcvClass = rootView.findViewById(R.id.rcvHomeClass);
        rcvClass.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        if(sessionManger.isUserLoggedIn(rootView.getContext())){
            User user = dbHelper.getUserByEmail(sessionManger.getUserEmail(rootView.getContext()));
            List<Session> sessionList = dbHelper.getSessionByInstructorId(user.getId());
            sessionAdapter = new SessionAdapter(sessionList);
            rcvClass.setAdapter(sessionAdapter);
        }

        // Start image slideshow
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(imageArray[currentIndex]);
                currentIndex = (currentIndex + 1) % imageArray.length;
                handler.postDelayed(this, 3000); // Change image every 3 seconds
            }
        };
        handler.post(runnable);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the slideshow when the fragment view is destroyed
        handler.removeCallbacks(runnable);
    }
}
