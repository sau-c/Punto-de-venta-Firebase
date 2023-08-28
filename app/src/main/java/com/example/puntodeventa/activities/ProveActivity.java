package com.example.puntodeventa.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.puntodeventa.R;
import com.example.puntodeventa.adapters.AccesorioAdapter;
import com.example.puntodeventa.models.Accesorio;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.util.ArrayList;
import java.util.List;

public class ProveActivity extends AppCompatActivity {
    private DatabaseReference dbRef;
    private AppCompatButton btnCompare, btnViewCount;
    private TextView tTile, tvProducto;
    private EditText etId;
    ProgressDialog progressDialog;
    private List<Accesorio> accesorioList;
    private RecyclerView recyclerAccesorio;
    private AccesorioAdapter accesorioAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prove);

        dbRef = FirebaseDatabase.getInstance().getReference();

        btnCompare = findViewById(R.id.btn_compare);
        btnViewCount = findViewById(R.id.btn_view);
        tTile = findViewById(R.id.tvTitle);
        etId = findViewById(R.id.et_id); // Id en tu layout
        tvProducto = findViewById(R.id.tv_producto); // Stock en tu layout

        //Recycler discrepancias
        recyclerAccesorio = findViewById(R.id.recycler_accesorio);
        accesorioList = new ArrayList<>();
        accesorioAdapter = new AccesorioAdapter(ProveActivity.this, accesorioList);
        accesorioAdapter.setCustomLayout(R.layout.item_accesorio);

        btnCompare.setOnClickListener(v -> compareProduct());
        btnViewCount.setOnClickListener(v -> loadDataFromFirebase());

        loadDataFromFirebase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_prove, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_finish_prove) {
            // Crear un cuadro de diálogo de confirmación
            new AlertDialog.Builder(this)
                    .setTitle("Terminar")
                    .setMessage("¿Estás seguro de que quieres terminar? Esta acción eliminará el conteo actual.")
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // El usuario confirmó la eliminación, eliminar el nodo
                            dbRef.child("accesorios_temp").removeValue().addOnSuccessListener(
                                            aVoid -> {
                                                Toast.makeText(ProveActivity.this, "REVISION TERMINADA.", Toast.LENGTH_LONG).show();
                                                // Continuar con la lógica adicional después de eliminar
                                                startActivity(new Intent(ProveActivity.this, InventoryActivity.class)
                                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                            })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ProveActivity.this, "ERROR AL LIMPIAR CONTEO.", Toast.LENGTH_LONG).show();
                                        // Puedes manejar el error aquí si es necesario
                                    });
                        }
                    })
                    .setNegativeButton("No", null).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void compareProduct() {
        DatabaseReference accesoriosRef = dbRef.child("accesorios");
        DatabaseReference accesoriosTempRef = dbRef.child("accesorios_temp");

        accesoriosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                accesorioList.clear();

                List<Accesorio> accesorios = new ArrayList<>();
                List<Accesorio> accesoriosTemp = new ArrayList<>();

                // Obtén los accesorios en "accesorios"
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Accesorio accesorio = snapshot.getValue(Accesorio.class);
                    accesorios.add(accesorio);
                }

                accesoriosTempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot tempDataSnapshot) {
                        // Obtén los accesorios en "accesorios_temp"
                        for (DataSnapshot snapshot : tempDataSnapshot.getChildren()) {
                            Accesorio accesorioTemp = snapshot.getValue(Accesorio.class);
                            accesoriosTemp.add(accesorioTemp);
                        }

                        for (Accesorio accesorioTemp : accesoriosTemp) {
                            Accesorio matchingTempAccesorio = findMatchingAccesorio(accesorioTemp, accesorios);

                            if (accesorioTemp.getStock() != matchingTempAccesorio.getStock()) {
                                // Si el stock es diferente
                                int diferencia = matchingTempAccesorio.getStock() - accesorioTemp.getStock();
                                String mensaje = diferencia < 0 ? "SOBRAN " : "FALTAN ";
                                mensaje += Math.abs(diferencia) + " pzs.";
                                accesorioTemp.setTiempo(mensaje);
                                accesorioList.add(accesorioTemp);
                            }
                        }

                        // Verificar los elementos que no contaste pero que existen en "accesorios"
                        for (Accesorio accesorio : accesorios) {
                            if (findMatchingAccesorio(accesorio, accesoriosTemp) == null) {
                                accesorio.setStock(0);
                                accesorio.setTiempo("FALTA EN SU TOTALIDAD");
                                accesorioList.add(accesorio);
                            }
                        }
                        accesorioAdapter.notifyDataSetChanged();

                        if (accesorioList.isEmpty()) {
                            tTile.setText("TODO CORRECTO");
                            tTile.setBackgroundColor(ContextCompat.getColor(ProveActivity.this, R.color.green_500));
                            tTile.setTextColor(ContextCompat.getColor(ProveActivity.this, R.color.white));
                        } else {
                            // Aquí puedes mostrar las discrepancias en una lista o hacer lo que necesites con ellas
                            tTile.setText("Discrepancias de stock en:");
                            tTile.setBackgroundColor(ContextCompat.getColor(ProveActivity.this, R.color.yellow_200));
                            tTile.setTextColor(ContextCompat.getColor(ProveActivity.this, R.color.black));

                            showDiscrepancies(accesorioList);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ProveActivity.this, "ERROR AL CONSULTAR CONTEO ACTUAL", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProveActivity.this, "ERROR AL CONSULTAR INVENTARIO", Toast.LENGTH_LONG).show();
            }
        });
    }

    private Accesorio findMatchingAccesorio(Accesorio accesorio, List<Accesorio> accesoriosTemp) {
        for (Accesorio tempAccesorio : accesoriosTemp) {
            if (tempAccesorio.getId().equals(accesorio.getId())) {
                return tempAccesorio;
            }
        }
        return null;
    }

    private void showDiscrepancies(List<Accesorio> discrepancias) {
        // Suponiendo que tienes un adaptador llamado accesorioAdapter configurado en tu RecyclerView
        if (accesorioAdapter != null) {
            // Actualiza la lista de datos en el adaptador con las discrepancias
            accesorioAdapter.setAccesorioList(discrepancias);
            // Notifica al adaptador que los datos han cambiado
            accesorioAdapter.notifyDataSetChanged();
        }
    }

    private void setAccesorioAdapter(List<Accesorio> accesorioList) {
        LinearLayoutManager manager = new LinearLayoutManager(ProveActivity.this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        accesorioAdapter.setCustomLayout(R.layout.item_accesorio);
        recyclerAccesorio.setLayoutManager(manager);
        recyclerAccesorio.setAdapter(accesorioAdapter);
    }

    private void loadDataFromFirebase() {
        progressDialog = ProgressDialog.show(ProveActivity.this,
                "Cargando resultados",
                "Espere por favor",
                true,
                false);

        dbRef.child("accesorios_temp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                accesorioList.clear();
                tTile.setText("Conteo actual:");
                tTile.setBackgroundColor(ContextCompat.getColor(ProveActivity.this, R.color.black));
                tTile.setTextColor(ContextCompat.getColor(ProveActivity.this, R.color.white));

                for (DataSnapshot accesorioSnapshot : dataSnapshot.getChildren()) {
                    Accesorio accesorio = accesorioSnapshot.getValue(Accesorio.class);
                    accesorioList.add(accesorio);
                }
                setAccesorioAdapter(accesorioList);
                accesorioAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    public void startScan(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("ESCANEA EL CODIGO DE BARRAS");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null){
            if (result.getContents() == null){
                Toast.makeText(this, "LECTURA CANCELADA", Toast.LENGTH_LONG).show();
            } else {
                String scannedId = result.getContents();
                DatabaseReference accesorioRef = dbRef.child("accesorios").child(scannedId);
                accesorioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Accesorio accesorio = dataSnapshot.getValue(Accesorio.class);
                            etId.setText(accesorio.getId());
                            tvProducto.setText(accesorio.getProducto());
                        } else {
                            Toast.makeText(ProveActivity.this, "ID NO ENCONTRADO EN LA BASE DE DATOS", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ProveActivity.this, "ERROR AL OBTENER DATOS", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void countProduct(View view) {
        ProgressDialog progressDialog = ProgressDialog.show(this,
                "Contando nuevo accesorio",
                "Espere por favor",
                true,
                false);

        String scannedId = etId.getText().toString().trim();

        // Verificar si el ID existe en la base de datos "accesorios"
        DatabaseReference accesoriosRef = dbRef.child("accesorios").child(scannedId);
        accesoriosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // El ID existe en "accesorios", copia su información y actualiza "accesorios_temp"
                    Accesorio accesorioExistente = dataSnapshot.getValue(Accesorio.class);

                    DatabaseReference accesoriosTempRef = dbRef.child("accesorios_temp").child(scannedId);
                    accesoriosTempRef.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            if (currentData.getValue() == null) {
                                // Si el ID no está registrado en "accesorios_temp", registrar el nuevo accesorio
                                accesorioExistente.setStock(1); // Inicializar el stock en 1
                                currentData.setValue(accesorioExistente);
                            } else {
                                // Si el ID ya está registrado en "accesorios_temp", acumular el stock
                                Accesorio accesorioTemp = currentData.getValue(Accesorio.class);
                                int nuevoStock = accesorioTemp.getStock() + 1;
                                accesorioTemp.setStock(nuevoStock);
                                currentData.setValue(accesorioTemp);
                            }
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                            progressDialog.dismiss();
                            if (committed && error == null) {
                                Toast.makeText(ProveActivity.this, "CONTEO EXITOSO", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProveActivity.this, "ERROR DE CONTEO", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    // El ID no existe en "accesorios", mostrar mensaje de error
                    progressDialog.dismiss();
                    Toast.makeText(ProveActivity.this, "ID NO REGISTRADO", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(ProveActivity.this, "ERROR AL OBTENER DATOS", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void startSearch(View view) {
        // Obtén el ID ingresado en el EditText y elimina espacios en blanco
        String id = etId.getText().toString().trim();

        if (!id.isEmpty()) {
            // Crea una referencia al nodo "accesorios" en la base de datos
            DatabaseReference accesorioRef = dbRef.child("accesorios").child(id);

            // Escucha una sola vez los datos en el nodo específico
            accesorioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Si el nodo existe, obtén los datos y actualiza los EditText
                        Accesorio accesorio = dataSnapshot.getValue(Accesorio.class);
                        etId.setText(accesorio.getId());
                        tvProducto.setText(accesorio.getProducto());
                    } else {
                        // Si el nodo no existe, muestra un mensaje de error
                        Toast.makeText(ProveActivity.this, "ID NO ENCONTRADO EN LA BASE DE DATOS", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Maneja errores de base de datos
                    Toast.makeText(ProveActivity.this, "ERROR AL OBTENER DATOS: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Si no se ingresó un ID, muestra un mensaje de error
            Toast.makeText(ProveActivity.this, "INGRESA EL ID PARA BUSCAR", Toast.LENGTH_LONG).show();
        }
    }
}
