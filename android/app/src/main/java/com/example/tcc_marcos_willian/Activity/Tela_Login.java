package com.example.tcc_marcos_willian.Activity;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.tcc_marcos_willian.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class Tela_Login extends AppCompatActivity{

    TextInputLayout inputLayout_email, inputLayout_cpf;
    Button button_logar;
    TextView textView_criarConta;
    FirebaseAuth auth_login;
    DatabaseReference reference_professor, reference_aluno, reference_messaging;
    LinearLayout layout_telaLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela__login);

        vincularComponentes();
        criarMascara();
        inicializarFirebase();

        button_logar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarCampos();
            }
        });

        textView_criarConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCadastro();
            }
        });

    }

    // vincular todos os componentes da activity
    private void vincularComponentes(){
        inputLayout_email = findViewById(R.id.inputLayout_emailLogin);
        inputLayout_cpf = findViewById(R.id.inputLayout_cpfLogin);
        button_logar = findViewById(R.id.button_logar);
        textView_criarConta = findViewById(R.id.textView_criarConta);
        layout_telaLogin = findViewById(R.id.layout_telaLogin);
    }

    // criar mascara de cpf no campo de cpf da activity
    private void criarMascara() {
        SimpleMaskFormatter simpleMaskFormatter = new SimpleMaskFormatter("NNN.NNN.NNN-NN");
        MaskTextWatcher maskTextWatcher = new MaskTextWatcher(inputLayout_cpf.getEditText(), simpleMaskFormatter);
        inputLayout_cpf.getEditText().addTextChangedListener(maskTextWatcher);
    }

    // inicializar variáveis do firebase
    private void inicializarFirebase() {
        reference_professor = FirebaseDatabase.getInstance().getReference();
        reference_aluno = FirebaseDatabase.getInstance().getReference();
        reference_messaging = FirebaseDatabase.getInstance().getReference();
        auth_login = FirebaseAuth.getInstance();
    }

    // verificar se campos estão preenchidos
    private void verificarCampos() {
        String email = inputLayout_email.getEditText().getText().toString().trim();
        String cpf01 = inputLayout_cpf.getEditText().getText().toString().trim();
        String cpf02 = cpf01.replace(".", "");
        String cpf03 = cpf02.replace(".", "");
        String cpf = cpf03.replaceAll("-", "");

        if ((!TextUtils.isEmpty(email)) && (email.contains("@")) && (email.contains("."))){
            inputLayout_email.setError(null);
            if ((!TextUtils.isEmpty(cpf)) && (cpf.length()==11)){
                inputLayout_cpf.setError(null);
                logarUsuario(email, cpf);
            }else{
                inputLayout_cpf.setError(getString(R.string.tx_erroCampo));
                inputLayout_cpf.requestFocus();
            }
        }else{
            inputLayout_email.setError(getString(R.string.tx_erroCampo));
            inputLayout_email.requestFocus();
        }
    }

    // fazer login na aplicação
    private void logarUsuario(String AuthEmail, final String AuthSenha) {
        auth_login.signInWithEmailAndPassword(AuthEmail, AuthSenha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    procurarAlunoFirebase(AuthSenha);
                }else{
                    abrirSnackbar(getString(R.string.tx_erroLogin));
                }
            }
        });
    }

    // procurar aluno no sistema
    private void procurarAlunoFirebase(final String cpfUsuario) {
        reference_aluno.child("usuarios").child("aluno").child(cpfUsuario).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    if(!dataSnapshot.child("nome").getValue().equals("") && !dataSnapshot.child("email").getValue().equals("")){
                        String tipoUsuario = "aluno";
                        String nomeUsuario = dataSnapshot.child("nome").getValue().toString();
                        salvarDadosUsuarioSharedPreference(nomeUsuario, cpfUsuario, tipoUsuario);
                    }else{
                        abrirSnackbar(getString(R.string.tx_erroLogin));
                    }
                }else{
                    procurarProfessorFirebase(cpfUsuario);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // procurar profesor no firebase
    private void procurarProfessorFirebase(final String cpfUsuario) {
        reference_professor.child("usuarios").child("professor").child(cpfUsuario).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    if (!dataSnapshot.child("nome").getValue().equals("") && !dataSnapshot.child("email").getValue().equals("")){
                        String tipoUsuario = "professor";
                        String nomeUsuario = dataSnapshot.child("nome").getValue().toString();
                        salvarDadosUsuarioSharedPreference(nomeUsuario, cpfUsuario, tipoUsuario);
                    }else{
                        abrirSnackbar(getString(R.string.tx_erroLogin));
                    }
                }else{
                    String tipoUsuario = "null";
                    abrirMenu(tipoUsuario);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // salvar dados do usuário na memória da aplicação
    private void salvarDadosUsuarioSharedPreference(String nome, String cpf, String tipoUsuario) {
        SharedPreferences preferences = getSharedPreferences("dadosUsuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("nome", nome);
        editor.putString("cpf", cpf);
        editor.putString("tipo", tipoUsuario);
        editor.apply();

        criarTokenCloudMessaging(tipoUsuario, cpf);
    }

    // criar token para envio de notificação
    private void criarTokenCloudMessaging(String tipo, String cpf) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            abrirSnackbar(getString(R.string.tx_token));
                        }

                        String token = task.getResult().getToken();
                        reference_messaging.child("usuarios").child(tipo).child(cpf).child("tokenNotification").setValue(token)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        abrirMenu(tipo);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        abrirSnackbar(getString(R.string.tx_token));
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        abrirSnackbar(getString(R.string.tx_token));
                    }
                });
    }

    // saber qual menu abrir
    private void abrirMenu(String tipoUsuario) {
        if (tipoUsuario.equals("professor")){
            abrirMenuProf();
        }else if (tipoUsuario.equals("aluno")) {
            abrirMenuAluno();
        } else if (tipoUsuario.equals("null")) {
            abrirSnackbar(getString(R.string.tx_erroLogin));
        }
    }

    // abrir menu do professor
    private void abrirMenuProf(){
        finish();
        Intent intent = new Intent(getApplicationContext(),Menu_Professor.class);
        startActivity(intent);
    }

    //abrir menu do alunp
    private void abrirMenuAluno(){
        finish();
        Intent intent = new Intent(getApplicationContext(),Menu_Aluno.class);
        startActivity(intent);
    }

    // abrir cadastro de usuário
    private void abrirCadastro() {
        Intent intent = new Intent(getApplicationContext(), Cadastro_Usuario.class);
        startActivity(intent);
    }

    // abrir mensagem no layout da activity
    private void abrirSnackbar(String mensagem) {
        InputMethodManager teclado = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(inputLayout_email.getEditText().getWindowToken(), 0);
        Snackbar snackbar = Snackbar.make(layout_telaLogin, mensagem, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}