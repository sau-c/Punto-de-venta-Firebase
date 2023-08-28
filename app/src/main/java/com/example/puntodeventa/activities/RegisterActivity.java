package com.example.puntodeventa.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.puntodeventa.R;
import com.example.puntodeventa.models.Accesorio;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    private DatabaseReference dbRef;
    EditText etId, etProducto, etMarca, etPrecio, etStock;
    AppCompatButton btnRegister;
    String dia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        SimpleDateFormat fechaFormat = new SimpleDateFormat("dd/MM/yyyy 'a las' hh:mm:ss a");
        dia = fechaFormat.format(currentDate);

        dbRef = FirebaseDatabase.getInstance().getReference();

        etId = findViewById(R.id.et_id);
        etProducto = findViewById(R.id.tv_producto);
        etMarca = findViewById(R.id.et_marca);
        etPrecio = findViewById(R.id.et_precio);
        etStock = findViewById(R.id.et_stock);

        btnRegister = findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(v -> registerProduct());
    }

    public void startScan(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("ESCANEA EL CODIGO DE BARRAS");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

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
                            etProducto.setText(accesorio.getProducto());
                            etMarca.setText(accesorio.getMarca());
                            etPrecio.setText(String.valueOf(accesorio.getPrecio()));
                        } else {
                            etId.setText(result.getContents());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(RegisterActivity.this, "ERROR AL OBTENER DATOS", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void registerProduct() {
        String id = etId.getText().toString().trim();
        String producto = etProducto.getText().toString().trim().toLowerCase();
        String marca = etMarca.getText().toString().trim().toLowerCase();
        String precioStr = etPrecio.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();

        // Check if any field is empty
        if (id.isEmpty() || producto.isEmpty() || marca.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "HAY CAMPOS VACIOS", Toast.LENGTH_LONG).show();
            return;
        }

        btnRegister.setEnabled(false); //evitar doble registro

        int stock = Integer.parseInt(stockStr);
        double precio1 = Double.parseDouble(precioStr); // String -> Number
        double precio = Math.round(precio1 * 100.0) / 100.0; // Redondear el precio a dos decimales

        ProgressDialog progressDialog = ProgressDialog.show(this,
                "Registrando nuevo accesorio",
                "Espere por favor",
                true,
                false);

        // Verificar si el ID ya está registrado en la base de datos
        DatabaseReference accesorioRef = dbRef.child("accesorios").child(id);
        accesorioRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) {
                    // Si el ID no está registrado, crear un nuevo accesorio
                    Accesorio nuevoAccesorio = new Accesorio(id, producto, marca, precio, stock, dia);
                    currentData.setValue(nuevoAccesorio);
                } else {
                    // Si el ID ya está registrado, incrementar el stock
                    Accesorio accesorioExistente = currentData.getValue(Accesorio.class);
                    int nuevoStock = accesorioExistente.getStock() + stock;
                    accesorioExistente.setStock(nuevoStock);
                    accesorioExistente.setTiempo(dia);
                    currentData.setValue(accesorioExistente);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                progressDialog.dismiss();
                if (committed && error == null) {
                    Toast.makeText(RegisterActivity.this, "REGISTRO EXITOSO", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, RegisterActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } else {
                    Toast.makeText(RegisterActivity.this, "ERROR DE REGISTRO", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}