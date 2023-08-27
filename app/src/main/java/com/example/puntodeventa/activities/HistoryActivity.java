package com.example.puntodeventa.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.puntodeventa.R;
import com.example.puntodeventa.adapters.AccesorioAdapter;
import com.example.puntodeventa.models.Accesorio;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private List<Accesorio> ventaList;
    private RecyclerView recyclerAccesorio;
    private TextView tHoy, tTotal;
    private DatabaseReference dbRef;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerAccesorio = findViewById(R.id.recycler_accesorio);
        tHoy = findViewById(R.id.tvHoy);
        tTotal = findViewById(R.id.tvTotal);
        //Only for design
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        SimpleDateFormat fechaFormat = new SimpleDateFormat("dd MM yyyy");//For database
        SimpleDateFormat fechaTitleFormat = new SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy");

        String dia = fechaFormat.format(currentDate);
        String diaTitulo = fechaTitleFormat.format(currentDate);
        diaTitulo = diaTitulo.substring(0, 1).toUpperCase() + diaTitulo.substring(1);
        tHoy.setText(diaTitulo);

        dbRef = FirebaseDatabase.getInstance().getReference().child("ventas").child(dia);

        ventaList = new ArrayList<>();

        loadDataFromFirebase();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sell) {
            startActivity(new Intent(this, SellActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadDataFromFirebase() {
        progressDialog = ProgressDialog.show(HistoryActivity.this,
                "Cargando resultados",
                "Espere por favor",
                true,
                false);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ventaList.clear();

                for (DataSnapshot accesorioSnapshot : dataSnapshot.getChildren()) {
                    if(!accesorioSnapshot.getKey().equals("total")){
                        Accesorio accesorio = accesorioSnapshot.getValue(Accesorio.class);
                        ventaList.add(accesorio);
                    }
                }
                // Verificar si el nodo "total" existe en el DataSnapshot
                if (dataSnapshot.hasChild("total")) {
                    String total = String.valueOf(dataSnapshot.child("total").getValue());
                    tTotal.setText(total);
                } else {
                    // Si no existe, establece el valor de "total" en 0
                    tTotal.setText("0");
                }

                setAccesorioAdapter(ventaList);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void setAccesorioAdapter(List<Accesorio> accesorioList) {
        LinearLayoutManager manager = new LinearLayoutManager(HistoryActivity.this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        AccesorioAdapter accesorioAdapter = new AccesorioAdapter(HistoryActivity.this, accesorioList);

        // Configura el diseño personalizado si es necesario
        accesorioAdapter.setCustomLayout(R.layout.item_venta);

        recyclerAccesorio.setLayoutManager(manager);
        recyclerAccesorio.setAdapter(accesorioAdapter);
    }
}