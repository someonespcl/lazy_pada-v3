package com.lazypanda.activities;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lazypanda.R;
import com.lazypanda.adapters.AdapterUser;
import com.lazypanda.models.ModelUsers;
import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    public UsersFragment() {}

    RecyclerView recyclerView;
    AdapterUser adapterUsers;
    List<ModelUsers> usersList;
    StaggeredGridLayoutManager layoutManager;
    private SearchView search_user_text;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.users_recyclerview);
        recyclerView.setHasFixedSize(true);
        // layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        // layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_LAZY);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2); // 2 columns initially
        layoutManager.setSpanSizeLookup(
                new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        // Return the span size for each item position
                        // For example, return 1 for regular items and 2 for wider items
                        return (position % 3 == 0) ? 2 : 1; // Adjust this logic as needed
                    }
                });
        recyclerView.setLayoutManager(layoutManager/*new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false)*/);

        usersList = new ArrayList<>();
        getAllUsers();

        EditText search_user_text = view.findViewById(R.id.search_users);
        search_user_text.addTextChangedListener(
        new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence charSequence, int i, int i1, int i2) {
                // This method is called before text changes, but we don't need it here.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called whenever the text changes.
                String searchText = charSequence.toString().trim();
                searchUser(searchText);
                //handleSearchAction(searchText);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // This method is called after text changes, but we don't need it here.
            }
        });

        return view;
    }

    private void searchUser(String query) {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dRefer = FirebaseDatabase.getInstance().getReference("Users");

        dRefer.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnap) {
                        usersList.clear();
                        for (DataSnapshot ds : dataSnap.getChildren()) {
                            ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                            if (modelUsers != null
                                    && modelUsers.getUId() != null
                                    && !modelUsers.getUId().equals(fUser.getUid())) {
                                if (modelUsers.getName().toLowerCase().contains(query.toLowerCase())
                                        || modelUsers
                                                .getEmail()
                                                .toLowerCase()
                                                .contains(query.toLowerCase())) {
                                    usersList.add(modelUsers);
                                }
                                adapterUsers = new AdapterUser(getActivity(), usersList);
                                adapterUsers.notifyDataSetChanged();
                                recyclerView.setAdapter(adapterUsers);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError dataError) {}
                });
    }

    private void getAllUsers() {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dRefer = FirebaseDatabase.getInstance().getReference("Users");

        dRefer.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnap) {
                        usersList.clear();
                        for (DataSnapshot ds : dataSnap.getChildren()) {
                            ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                            if (!modelUsers.getUId().equals(fUser.getUid())) {
                                usersList.add(modelUsers);
                            }

                            adapterUsers = new AdapterUser(getActivity(), usersList);
                            recyclerView.setAdapter(adapterUsers);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError dataError) {}
                });
    }
}
