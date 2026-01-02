package com.example.bikestationapp_ca3.fragments;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bikestationapp_ca3.MainActivity;
import com.example.bikestationapp_ca3.R;
import com.example.bikestationapp_ca3.data_classes.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
    SharedPreferences sp;

    ImageView ivName, ivLogout;
    TextView tvName;
    EditText etName;
    Button btnSaveChanges, btnDeleteUser;

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
        sp = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        uid = sp.getString("USER", "");

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        ref = db.getReference("users").child(uid);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);

                ivName = getActivity().findViewById(R.id.iv_name);
                ivLogout = getActivity().findViewById(R.id.iv_logout);
                tvName = getActivity().findViewById(R.id.tv_name);
                etName = getActivity().findViewById(R.id.et_profileName);

                btnSaveChanges = getActivity().findViewById(R.id.button_saveChanges);
                btnDeleteUser = getActivity().findViewById(R.id.button_deleteUser);

                ivName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeName(v);
                    }
                });

                ivLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logout(v);
                    }
                });

                btnSaveChanges.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveChanges(v);
                    }
                });

                btnDeleteUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteUser(v);
                    }
                });

                tvName.setText(user.getName());
                etName.setText(user.getName());
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

    public void saveChanges(View view) {
        String name = etName.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(getActivity(),
                    "Please fill in the required fields",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        User updatedUser = new User(user.getEmail(), user.getPassword(), name);
        updatedUser.setFavourites(user.getFavourites());

        ref.setValue(updatedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        tvName.setText(updatedUser.getName());
        etName.setText(updatedUser.getName());

        etName.setVisibility(INVISIBLE);
        tvName.setVisibility(VISIBLE);

        btnSaveChanges.setEnabled(false);
    }

    public void deleteUser(View view) throws NullPointerException {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        FirebaseAuth auth = FirebaseAuth.getInstance();

        builder.setMessage("Are you sure that you would like to permanently delete your account?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        auth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                ref.removeValue();

                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("USER", "");
                                editor.commit();

                                Toast.makeText(
                                        getActivity(),
                                        "Account has been deleted",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    public void logout(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        FirebaseAuth auth = FirebaseAuth.getInstance();

        builder.setMessage("Logout and return to login screen?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        auth.signOut();
                        dialog.dismiss();
                        requireActivity().finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}