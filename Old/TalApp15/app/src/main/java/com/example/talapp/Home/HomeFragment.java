package com.example.talapp.Home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import com.example.talapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class HomeFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String KEY_UNITA = "unita";
    private String KEY_DATA = "data";
    private String KEY_HB = "hb";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        root.findViewById(R.id.CVCalendario).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(root).navigate(R.id.action_homeFragment_to_calendarioFragment);
            }
        });

        root.findViewById(R.id.CVTrasfusioni).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(root).navigate(R.id.action_homeFragment_to_trasfusioniFragment);
            }
        });

        root.findViewById(R.id.CVEsami).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(root).navigate(R.id.action_homeFragment_to_esamiFragment);
            }
        });

        root.findViewById(R.id.CVTerapie).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(root).navigate(R.id.action_homeFragment_to_terapieFragment);
            }
        });

        //root.findViewById(R.id.buttonDB).setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        saveTrasfusione(root);
        //    }
        //});
        return root;
    }

    //public void saveTrasfusione(View v){
    //    Map<String, Object> trasfusione = new HashMap<>();
    //    trasfusione.put(KEY_UNITA, "2");
    //    trasfusione.put(KEY_DATA, Calendar.getInstance());
    //    trasfusione.put(KEY_HB, 12.3);
//
    //
//
    //    //db.collection("DB_TalApp").document("Trasfusione").set
    //    //db.collection("DB_TalApp").add(trasfusione);
    //}
}