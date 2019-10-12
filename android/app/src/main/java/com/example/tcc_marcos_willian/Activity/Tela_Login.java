package com.example.tcc_marcos_willian.Activity;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tcc_marcos_willian.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class Tela_Login extends AppCompatActivity{

    TextInputLayout lt_email, lt_senha;
    Button bt_logar;
    TextView tx_criarConta;
    FirebaseAuth firebaseAuth;
    DatabaseReference referenceProfessor, referenceAluno, referenceMessaging;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela__login);

        vincularComponentes();

        criarMascara();

        inicializarFirebase();

        bt_logar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarCampos();
            }
        });

        tx_criarConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCadastro();
            }
        });

    }

    private void vincularComponentes(){
        lt_email = findViewById(R.id.lyt_EmailLogin);
        lt_senha = findViewById(R.id.lyt_CPFLogin);
        bt_logar = findViewById(R.id.btn_Logar);
        tx_criarConta = findViewById(R.id.txt_CriarConta);
        linearLayout = findViewById(R.id.layoutTelaLogin);
    }

    private void criarMascara() {
        SimpleMaskFormatter simpleMaskFormatter = new SimpleMaskFormatter("NNN.NNN.NNN-NN");
        MaskTextWatcher maskTextWatcher = new MaskTextWatcher(lt_senha.getEditText(), simpleMaskFormatter);
        lt_senha.getEditText().addTextChangedListener(maskTextWatcher);
    }

    private void inicializarFirebase() {
        referenceProfessor = FirebaseDatabase.getInstance().getReference();
        referenceAluno = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        referenceMessaging = FirebaseDatabase.getInstance().getReference();
    }

    private void verificarCampos() {
        String email = lt_email.getEditText().getText().toString().trim();
        String senha01 = lt_senha.getEditText().getText().toString().trim();
        String senha02 = senha01.replace(".", "");
        String senha03 = senha02.replace(".", "");
        String senha = senha03.replaceAll("-", "");

        if ((!TextUtils.isEmpty(email)) && (email.contains("@")) && (email.contains("."))){
            lt_email.setError(null);
            if ((!TextUtils.isEmpty(senha)) && (senha.length()==11)){
                lt_senha.setError(null);
                logarUsuario(email, senha);
            }else{
                lt_senha.setError(getString(R.string.tx_erroCampo));
                lt_senha.requestFocus();
            }
        }else{
            lt_email.setError(getString(R.string.tx_erroCampo));
            lt_email.requestFocus();
        }
    }

    private void logarUsuario(String AuthEmail, final String AuthSenha) {
        firebaseAuth.signInWithEmailAndPassword(AuthEmail, AuthSenha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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

    private void procurarAlunoFirebase(final String cpfUsuario) {
        referenceAluno.child("usuarios").child("aluno").child(cpfUsuario).addValueEventListener(new ValueEventListener() {
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

    private void procurarProfessorFirebase(final String cpfUsuario) {
        referenceProfessor.child("usuarios").child("professor").child(cpfUsuario).addValueEventListener(new ValueEventListener() {
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

    private void salvarDadosUsuarioSharedPreference(String nome, String cpf, String tipoUsuario) {
        SharedPreferences preferences = getSharedPreferences("DadosUsuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("nome", nome);
        editor.putString("cpf", cpf);
        editor.putString("tipo", tipoUsuario);
        editor.apply();

        pegarTokenCloudMessaging(tipoUsuario, cpf);
    }

    private void abrirMenu(String tipoUsuario) {
        if (tipoUsuario.equals("professor")){
            abrirMenuProf();
        }else if (tipoUsuario.equals("aluno")) {
            abrirMenuAluno();
        } else if (tipoUsuario.equals("null")) {
            abrirSnackbar(getString(R.string.tx_erroLogin));
        }
    }

    private void abrirMenuProf(){
        finish();
        Intent intent = new Intent(getApplicationContext(),Menu_Professor.class);
        startActivity(intent);
    }

    private void abrirMenuAluno(){
        finish();
        Intent intent = new Intent(getApplicationContext(),Menu_Aluno.class);
        startActivity(intent);
    }

    private void abrirCadastro() {
        Intent intent = new Intent(getApplicationContext(), Cadastro_Usuario.class);
        startActivity(intent);
    }

    private void abrirSnackbar(String mensagem) {
        InputMethodManager teclado = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(lt_email.getEditText().getWindowToken(), 0);
        Snackbar snackbar = Snackbar.make(linearLayout, mensagem, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void pegarTokenCloudMessaging(String tipo, String cpf) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.tx_token), Toast.LENGTH_LONG).show();
                        }

                        String token = task.getResult().getToken();
                        referenceMessaging.child("usuarios").child(tipo).child(cpf).child("tokenNotification").setValue(token)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        abrirMenu(tipo);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.tx_token), Toast.LENGTH_LONG).show();
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
}