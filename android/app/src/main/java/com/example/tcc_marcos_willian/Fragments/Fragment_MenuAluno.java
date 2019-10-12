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

import com.example.tcc_marcos_willian.Activity.Lista_Impressao;
import com.example.tcc_marcos_willian.Activity.Ver_Arquivos;
import com.example.tcc_marcos_willian.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Fragment_MenuAluno extends Fragment {

    DatabaseReference reference;
    TextView tx_nomeAluno;
    Button bt_imprimir, bt_listaImpressao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_aluno, container, false);

        reference = FirebaseDatabase.getInstance().getReference();

        tx_nomeAluno = view.findViewById(R.id.txt_NomeMenuAluno);
        bt_imprimir = view.findViewById(R.id.btn_ProcurarApostila);
        bt_listaImpressao = view.findViewById(R.id.btn_AbrirListaImpressaoAluno);

        tx_nomeAluno.setText(getString(R.string.tx_bemVindo)+" "+pegarSharedPreference());

        bt_imprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirArquivos();
            }
        });

        bt_listaImpressao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirListaArquivos();
            }
        });

        return view;
    }

    private void abrirListaArquivos() {
        Intent intent = new Intent(getContext(), Lista_Impressao.class);
        intent.putExtra("tipo", "aluno");
        startActivity(intent);
    }

    private String pegarSharedPreference() {
        SharedPreferences preferences = getActivity().getSharedPreferences("DadosUsuario", Context.MODE_PRIVATE);
        String nome = preferences.getString("nome", "não encontrado");
        return nome;
    }

    private void abrirArquivos() {
        Intent intent = new Intent(getContext(), Ver_Arquivos.class);
        startActivity(intent);
    }
}
