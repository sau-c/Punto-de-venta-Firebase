package com.example.puntodeventa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.puntodeventa.R;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SellActivity extends AppCompatActivity {
    private DatabaseReference dbRef;
    EditText etId, etStock;
    TextView etProducto, etMarca, etPrecio;
    AppCompatButton btnSell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        dbRef = FirebaseDatabase.getInstance().getReference();

        etId = findViewById(R.id.et_id); // Id en tu layout
        etProducto = findViewById(R.id.tv_producto); // Producto en tu layout
        etMarca = findViewById(R.id.et_marca); // Marca en tu layout
        etPrecio = findViewById(R.id.et_precio); // Precio en tu layout
        etStock = findViewById(R.id.et_stock); // Stock en tu layout
        btnSell = findViewById(R.id.btn_sell);

        btnSell.setOnClickListener(v -> sellProduct());

        //PARA EVITAR ERRORES DE USUARIO
        etId.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Este método se llama cuando el texto en el EditText de ID cambia
                // Si el EditText de ID está vacío, limpia los otros TextViews
                etProducto.setText("");
                etMarca.setText("");
                etPrecio.setText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
                            etProducto.setText(accesorio.getProducto());
                            etMarca.setText(accesorio.getMarca());
                            etPrecio.setText(String.valueOf(accesorio.getPrecio()));
                        } else {
                            Toast.makeText(SellActivity.this, "ID NO ENCONTRADO EN LA BASE DE DATOS", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(SellActivity.this, "ERROR AL OBTENER DATOS", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
                        etProducto.setText(accesorio.getProducto());
                        etMarca.setText(accesorio.getMarca());
                        etPrecio.setText(String.valueOf(accesorio.getPrecio()));
                    } else {
                        // Si el nodo no existe, muestra un mensaje de error
                        Toast.makeText(SellActivity.this, "ID NO ENCONTRADO EN LA BASE DE DATOS", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Maneja errores de base de datos
                    Toast.makeText(SellActivity.this, "ERROR AL OBTENER DATOS: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Si no se ingresó un ID, muestra un mensaje de error
            Toast.makeText(SellActivity.this, "INGRESA EL ID PARA BUSCAR", Toast.LENGTH_LONG).show();
        }
    }


    public void sellProduct() {
        String id = etId.getText().toString().trim();
        String producto = etProducto.getText().toString().trim().toLowerCase();
        String marca = etMarca.getText().toString().trim().toLowerCase();
        String precioStr = etPrecio.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();

        //Extraer fecha y hora
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        SimpleDateFormat fechaFormat = new SimpleDateFormat("dd MM yyyy");
        SimpleDateFormat horaFormat = new SimpleDateFormat("hh:mm:ss a");

        String dia = fechaFormat.format(currentDate);
        String hora = horaFormat.format(currentDate);

        // Check if any field is empty
        if (id.isEmpty() || producto.isEmpty() || marca.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty() || stockStr.equals("0")) {
            Toast.makeText(this, "HAY CAMPOS VACIOS", Toast.LENGTH_LONG).show();
            return;
        }

        btnSell.setEnabled(false);
        int cantidadVendida = Integer.parseInt(stockStr);
        double precio = Double.parseDouble(precioStr);

        // Obtener una referencia al accesorio en la base de datos
        DatabaseReference accesorioRef = dbRef.child("accesorios").child(id);
        DatabaseReference ventasRef = dbRef.child("ventas").child(dia); // Crea Referencia al dia

        // Ejecutar la transacción
        accesorioRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Accesorio accesorio = mutableData.getValue(Accesorio.class);
                Accesorio venta = new Accesorio(id, producto, marca, precio, cantidadVendida, hora);

                if (accesorio == null) {
                    return Transaction.success(mutableData);
                }

                int stockActual = accesorio.getStock();

                if (stockActual >= cantidadVendida) {
                    int nuevoStock = stockActual - cantidadVendida;
                    accesorio.setStock(nuevoStock);
                    mutableData.setValue(accesorio);

                    // Guardar la venta en la base de datos de ventas
                    ventasRef.push().setValue(venta);

                    // Obtener el valor actual de "total" desde Firebase
                    DatabaseReference totalRef = ventasRef.child("total");
                    totalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            double total = 0;

                            // Obtener el valor actual de "total" si existe
                            if (dataSnapshot.exists()) {
                                total = Double.parseDouble(dataSnapshot.getValue().toString());
                            }

                            // Actualizar el total con la nueva venta
                            total += precio * cantidadVendida;
                            total = Math.round(total * 100.0) / 100.0;

                            // Guardar el nuevo valor de "total" en Firebase
                            totalRef.setValue(total);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Manejar errores de Firebase
                        }
                    });

                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Toast.makeText(SellActivity.this, "VENTA REGISTRADA", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SellActivity.this, HistoryActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } else {
                    if (dataSnapshot != null && dataSnapshot.exists()) {
                        Accesorio accesorio = dataSnapshot.getValue(Accesorio.class);
                        if (accesorio != null) {
                            Toast.makeText(SellActivity.this, "NO HAY SUFICIENTE STOCK DISPONIBLE", Toast.LENGTH_LONG).show();
                            btnSell.setEnabled(true);
                        } else {
                            Toast.makeText(SellActivity.this, "ID NO ENCONTRADO EN LA BASE DE DATOS", Toast.LENGTH_LONG).show();
                            btnSell.setEnabled(true);
                        }
                    } else {
                        Toast.makeText(SellActivity.this, "ERROR AL REGISTRAR VENTA", Toast.LENGTH_LONG).show();
                        btnSell.setEnabled(true);
                    }
                }
            }
        });
    }
}