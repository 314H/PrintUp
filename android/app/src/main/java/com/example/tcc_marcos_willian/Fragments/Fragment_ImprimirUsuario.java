package com.example.tcc_marcos_willian.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tcc_marcos_willian.Activity.Menu_Aluno;
import com.example.tcc_marcos_willian.Activity.Menu_Professor;
import com.example.tcc_marcos_willian.Modelos.Upload_Arquivo;
import com.example.tcc_marcos_willian.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;

public class Fragment_ImprimirUsuario extends Fragment {

    LinearLayout layout_imprimirUsuario;
    TextInputLayout inputLayout_numeroCopias;
    Button button_selecionarPDF, button_selecionarDOCX;
    TextView textView_nomeUsuario;
    StorageReference reference_arquivo;
    DatabaseReference reference_impressao;
    String tipoDocumento, extensao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_imprimir_usuario, container, false);

        inicializarFirebase();

        button_selecionarDOCX = view.findViewById(R.id.button_selecionarDOCXUsuarioImprimir);
        button_selecionarPDF = view.findViewById(R.id.button_selecionarPDFUsuarioImprimir);
        textView_nomeUsuario = view.findViewById(R.id.textView_nomeUsuarioImprimir);
        inputLayout_numeroCopias = view.findViewById(R.id.inputLayout_numeroCopiasUsuarioImprimir);
        layout_imprimirUsuario = view.findViewById(R.id.layout_usuarioImprimir);

        textView_nomeUsuario.setText(nomeSharedPreference());

        button_selecionarPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipoDocumento = "application/pdf";
                extensao = ".pdf";
                verificarCampo();
            }
        });

        button_selecionarDOCX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipoDocumento = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                extensao = ".docx";
                verificarCampo();
            }
        });

        return view;
    }

    // inicializar variaveis do firebase
    private void inicializarFirebase() {
        reference_impressao = FirebaseDatabase.getInstance().getReference();
        reference_arquivo = FirebaseStorage.getInstance().getReference();
    }

    // pegar nome salvo na memoria da aplicação
    private String nomeSharedPreference() {
        SharedPreferences preferences = getActivity().getSharedPreferences("dadosUsuario", Context.MODE_PRIVATE);
        String nome = preferences.getString("nome", "não encontrado");
        return nome;
    }

    // verificar se campo foi preenchido corretamente
    private void verificarCampo() {
        String numeroCopias = inputLayout_numeroCopias.getEditText().getText().toString().trim();

        if (!TextUtils.isEmpty(numeroCopias) && !numeroCopias.equals("0") && !numeroCopias.equals("00")){
            inputLayout_numeroCopias.setError(null);
            abrirSelecaoArquivo();
        }else{
            inputLayout_numeroCopias.setError(getString(R.string.tx_erroCampo));
            inputLayout_numeroCopias.requestFocus();
        }
    }

    // abrir midia do celular para selecionar arquivo passando o tipo de documento que pode ser selecionado
    private void abrirSelecaoArquivo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(tipoDocumento);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.tx_selecionarArquivo)), 1);
    }

    // pegar arquivo que foi selecionado
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            enviarArquivo(data.getData());
        }
    }

    // enviar arquivo para o storage do firebase
    private void enviarArquivo(Uri data) {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(getString(R.string.tx_enviando));
        progressDialog.show();

        reference_arquivo.child("imprimir").child(tipoUsuarioSharedPreference()).child(System.currentTimeMillis()+extensao).putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        salvarImpressaoFirebaseDatabase(taskSnapshot);
                        progressDialog.dismiss();
                        abrirMenu();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                progressDialog.setMessage(getString(R.string.tx_progressoDialog)+" "+(int)progress+getString(R.string.tx_porcento));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), getString(R.string.tx_erroUpload)+" "+e.toString(), Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    // pegar tipo de usuario que está logado
    private String tipoUsuarioSharedPreference() {
        SharedPreferences preferences = getActivity().getSharedPreferences("DadosUsuario", Context.MODE_PRIVATE);
        String tipo = preferences.getString("tipo", "não encontrado");
        return tipo;
    }

    // salvar impressão no realtime do firebase
    private void salvarImpressaoFirebaseDatabase(UploadTask.TaskSnapshot taskSnapshot) {
        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
        while (!uri.isComplete());
        Uri url = uri.getResult();
        Upload_Arquivo upload = new Upload_Arquivo();
        upload.numeroCopias = inputLayout_numeroCopias.getEditText().getText().toString();
        upload.url = url.toString();
        upload.nomeUsuario = nomeSharedPreference();
        upload.status = "aguardando";
        reference_impressao.child("imprimir").child(tipoUsuarioSharedPreference()).child(reference_impressao.push().getKey()).setValue(upload);

        Toast.makeText(getContext(), getString(R.string.tx_UploadComSucesso), Toast.LENGTH_LONG).show();
    }

    // abrir o menu do usuario
    public void abrirMenu(){
        if (tipoUsuarioSharedPreference().equals("aluno")){
            Intent intent = new Intent(getContext(), Menu_Aluno.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else if (tipoUsuarioSharedPreference().equals("professor")){
            Intent intent = new Intent(getContext(), Menu_Professor.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }
}
