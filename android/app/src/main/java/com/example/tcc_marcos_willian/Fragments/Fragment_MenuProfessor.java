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

    Button button_inserirArquivo, button_listaImpressao;
    TextView textView_nomeProfessor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_professor, container, false);


        textView_nomeProfessor = view.findViewById(R.id.textView_nomeMenuProfessor);
        button_inserirArquivo = view.findViewById(R.id.button_inserirArquivoMenuProfessor);
        button_listaImpressao = view.findViewById(R.id.button_listaImpressoesProfessor);


        textView_nomeProfessor.setText(getString(R.string.tx_bemVindo) + " " + pegarNomeSharedPreference());

        button_inserirArquivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirInsercao();
            }
        });

        button_listaImpressao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirListaImpressao();
            }
        });

        return view;
    }

    // pegar nome do usuario que está logado
    private String pegarNomeSharedPreference() {
        SharedPreferences preferences = getActivity().getSharedPreferences("dadosUsuario", Context.MODE_PRIVATE);
        String nome = preferences.getString("nome", "não encontrado");
        return nome;
    }

    // abrir activity para professor inserir um arquivo no sistema
    private void abrirInsercao() {
        Intent intent = new Intent(getContext(), Inserir_Arquivo.class);
        startActivity(intent);
    }

    // abrir lista de arquivos para impressão do usuario logado passando o tipo de usuario
    private void abrirListaImpressao() {
        Intent intent = new Intent(getContext(), Lista_Impressao.class);
        intent.putExtra("tipo", "professor");
        startActivity(intent);
    }
}
