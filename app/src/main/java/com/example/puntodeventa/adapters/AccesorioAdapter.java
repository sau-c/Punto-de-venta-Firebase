package com.example.puntodeventa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puntodeventa.R;
import com.example.puntodeventa.models.Accesorio;

import java.util.List;

public class AccesorioAdapter extends RecyclerView.Adapter<AccesorioAdapter.AccesorioViewHolder> {
    Context context;
    List<Accesorio> accesorioList;
    private int layoutResId;

    public void setCustomLayout(int layoutResId) {
        this.layoutResId = layoutResId;
    }
    
    @NonNull
    @Override
    public AccesorioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccesorioViewHolder(LayoutInflater.from(context)
                .inflate(layoutResId, parent, false)); // Usa el diseño personalizado aquí
    }

    public AccesorioAdapter(Context context, List<Accesorio> accesorioList) {
        this.context = context;
        this.accesorioList = accesorioList;
    }

    public static class AccesorioViewHolder extends RecyclerView.ViewHolder {
        TextView tId, tFullName, tPrecio, tStock, tHora, tDate;

        public AccesorioViewHolder(@NonNull View itemView) {
            super(itemView);
            tId = itemView.findViewById(R.id.tvId);
            tFullName = itemView.findViewById(R.id.tvFullName);
            tPrecio = itemView.findViewById(R.id.tvPrecio);
            tStock = itemView.findViewById(R.id.tvStock);
            tHora = itemView.findViewById(R.id.tvHora);
            tDate = itemView.findViewById(R.id.tvDate);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final AccesorioViewHolder holder, int position) {
        Accesorio pos = accesorioList.get(holder.getAdapterPosition());

        String full_name = pos.getProducto() + " " + pos.getMarca();
        holder.tId.setText(pos.getId());
        holder.tFullName.setText(full_name);
        holder.tPrecio.setText(String.valueOf(pos.getPrecio()));
        holder.tStock.setText(String.valueOf(pos.getStock())); //leer de String a entero
        holder.tHora.setText(pos.getTiempo());
        holder.tDate.setText(pos.getTiempo());
    }

    public void setAccesorioList(List<Accesorio> newList) {
        accesorioList = newList;
    }

    @Override
    public int getItemCount() {
        return accesorioList.size();
    }
}