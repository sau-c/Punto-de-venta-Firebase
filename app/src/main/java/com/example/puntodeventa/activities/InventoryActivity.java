package com.example.puntodeventa.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puntodeventa.R;
import com.example.puntodeventa.adapters.AccesorioAdapter;
import com.example.puntodeventa.models.Accesorio;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity{
    private List<Accesorio> accesorioList;
    private RecyclerView recyclerAccesorio;
    private DatabaseReference dbRef;
    ProgressDialog progressDialog;
    private AccesorioAdapter accesorioAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        dbRef = FirebaseDatabase.getInstance().getReference().child("accesorios");

        recyclerAccesorio = findViewById(R.id.recycler_accesorio);
        accesorioList = new ArrayList<>();
        accesorioAdapter = new AccesorioAdapter(InventoryActivity.this, accesorioList);
        accesorioAdapter.setCustomLayout(R.layout.item_accesorio);

        loadDataFromFirebase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("Buscar por nombre o ID");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Aquí implementa la lógica para filtrar los elementos del RecyclerView
                List<Accesorio> filteredList = new ArrayList<>();
                for (Accesorio accesorio : accesorioList) {
                    String fullInfo = accesorio.getId() +
                            accesorio.getProducto().toLowerCase() +
                            accesorio.getMarca().toLowerCase();

                    if (fullInfo.contains(newText.toLowerCase())) {
                        filteredList.add(accesorio);
                    }
                }

                // Actualiza el RecyclerView con la lista filtrada
                accesorioAdapter.setAccesorioList(filteredList);
                accesorioAdapter.notifyDataSetChanged();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        dbRef = FirebaseDatabase.getInstance().getReference().child("pass");

        if (id == R.id.action_admin) {
            startActivity(new Intent(this, AdminActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAccesorioAdapter(List<Accesorio> accesorioList) {
        LinearLayoutManager manager = new LinearLayoutManager(InventoryActivity.this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        // Configura el diseño personalizado si es necesario
        accesorioAdapter.setCustomLayout(R.layout.item_accesorio);

        recyclerAccesorio.setLayoutManager(manager);
        recyclerAccesorio.setAdapter(accesorioAdapter);
    }
    private void loadDataFromFirebase() {
        progressDialog = ProgressDialog.show(InventoryActivity.this,
                "Cargando resultados",
                "Espere por favor",
                true,
                false);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                accesorioList.clear();

                for (DataSnapshot accesorioSnapshot : dataSnapshot.getChildren()) {
                    Accesorio accesorio = accesorioSnapshot.getValue(Accesorio.class);
                    accesorioList.add(accesorio);
                }
                setAccesorioAdapter(accesorioList);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }
}
