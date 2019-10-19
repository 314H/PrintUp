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

    TextView textView_nomeAluno;
    Button button_procurarApostila, button_listaImpressao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_aluno, container, false);

        // vincular componentes do fragment
        textView_nomeAluno = view.findViewById(R.id.textView_nomeMenuAluno);
        button_procurarApostila = view.findViewById(R.id.button_procurarApostila);
        button_listaImpressao = view.findViewById(R.id.buttonn_listaImpressoesAluno);

        // setar nome do aluno no textView
        textView_nomeAluno.setText(getString(R.string.tx_bemVindo)+" "+nomeSharedPreference());

        button_procurarApostila.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirListaProfessores();
            }
        });

        button_listaImpressao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirListaArquivosImpressao();
            }
        });

        return view;
    }

    // pegar nome salvo no Shared Preference
    private String nomeSharedPreference() {
        SharedPreferences preferences = getActivity().getSharedPreferences("dadosUsuario", Context.MODE_PRIVATE);
        String nome = preferences.getString("nome", "não encontrado");
        return nome;
    }

    // abrir listView com professores do sistema
    private void abrirListaProfessores() {
        Intent intent = new Intent(getContext(), Ver_Arquivos.class);
        startActivity(intent);
    }

    // abrir listView com arquivos enviados para impressão passando o tipo de usuario
    private void abrirListaArquivosImpressao() {
        Intent intent = new Intent(getContext(), Lista_Impressao.class);
        intent.putExtra("tipo", "aluno");
        startActivity(intent);
    }
}
