package com.example.puntodeventa.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import com.example.puntodeventa.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setupButtonClicks();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_sell) {
//            startActivity(new Intent(this, SellActivity.class)
//                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private void setupButtonClicks() {
        binding.buttonSell.setOnClickListener(v -> {
            Intent intent = new Intent(this, SellActivity.class);
            startActivity(intent);
        });

        binding.buttonHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        });

        binding.buttonInventory.setOnClickListener(v -> {
            Intent intent = new Intent(this, InventoryActivity.class);
            startActivity(intent);
        });
    }
}