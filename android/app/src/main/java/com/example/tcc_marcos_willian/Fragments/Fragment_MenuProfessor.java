package com.example.tcc_marcos_willian.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tcc_marcos_willian.Activity.Inserir_Arquivo;
import com.example.tcc_marcos_willian.Activity.Lista_Impressao;
import com.example.tcc_marcos_willian.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Fragment_MenuProfessor extends Fragment {

    DatabaseReference reference;
    Button bt_inserirArquivo, bt_listaImpressao;
    TextView tx_nomeProfessor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_professor, container, false);

        reference = FirebaseDatabase.getInstance().getReference();

        tx_nomeProfessor = view.findViewById(R.id.txt_NomeMenuProfessor);
        bt_inserirArquivo = view.findViewById(R.id.btn_InserirArquivoMenuProfessor);
        bt_listaImpressao = view.findViewById(R.id.btn_AbrirListaImpressaoProfessor);


        tx_nomeProfessor.setText(getString(R.string.tx_bemVindo) + " " + pegarNomeSharedPreference());

        bt_inserirArquivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirInsercao();
            }
        });

        bt_listaImpressao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirListaImpressao();
            }
        });

        return view;
    }

    private void abrirListaImpressao() {
        Intent intent = new Intent(getContext(), Lista_Impressao.class);
        intent.putExtra("tipo", "professor");
        startActivity(intent);
    }

    private String pegarNomeSharedPreference() {
        SharedPreferences preferences = getActivity().getSharedPreferences("DadosUsuario", Context.MODE_PRIVATE);
        String nome = preferences.getString("nome", "não encontrado");
        return nome;
    }

    public void abrirInsercao() {
        Intent intent = new Intent(getContext(), Inserir_Arquivo.class);
        startActivity(intent);
    }
}
