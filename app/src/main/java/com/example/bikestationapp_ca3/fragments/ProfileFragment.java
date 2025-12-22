package com.example.bikestationapp_ca3.fragments;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bikestationapp_ca3.R;
import com.example.bikestationapp_ca3.data_classes.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    ValueEventListener listener;
    String uid;
    User user;
    DatabaseReference ref;

    TextView tvName, tvEmail;
    EditText etName, etEmail;
    Button btnSaveChanges;

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
        SharedPreferences sp = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        uid = sp.getString("USER", "");

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        ref = db.getReference("users").child(uid);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);

                tvName = getActivity().findViewById(R.id.tv_name);
                tvEmail = getActivity().findViewById(R.id.tv_email);
                etName = getActivity().findViewById(R.id.et_name);
                etEmail = getActivity().findViewById(R.id.et_email);

                btnSaveChanges = getActivity().findViewById(R.id.button_saveChanges);

                tvName.setText(user.getName());
                tvEmail.setText(user.getEmail());
                etName.setText(user.getName());
                etEmail.setText(user.getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ref.addValueEventListener(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ref.removeEventListener(listener);
    }

    public void changeName(View view) {
        if (!btnSaveChanges.isEnabled()) {
            btnSaveChanges.setEnabled(true);
        }
        tvName.setVisibility(INVISIBLE);
        etName.setVisibility(VISIBLE);
    }

    public void changeEmail(View view) {
        if (!btnSaveChanges.isEnabled()) {
            btnSaveChanges.setEnabled(true);
        }
        tvEmail.setVisibility(INVISIBLE);
        etEmail.setVisibility(VISIBLE);
    }

    public void saveChanges(View view) {
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getActivity(),
                    "Please fill in the required fields",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        User updatedUser = new User(email, user.getPassword(), name);
        updatedUser.setFavourites(user.getFavourites());

        ref.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(
                        getActivity(),
                        "User details successfully updated",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(
                        getActivity(),
                        "An error has occurred",
                        Toast.LENGTH_SHORT
                ).show();
                Log.e("FirebaseDB", e.getMessage());
            }
        });
    }
}