package com.example.tcc_marcos_willian.Activity;

import com.example.tcc_marcos_willian.Modelos.Usuario;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.tcc_marcos_willian.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Cadastro_Usuario extends AppCompatActivity {

    TextInputLayout inputLayout_nome, inputLayout_email, inputLayout_cpf;
    Button button_cadastrar;
    DatabaseReference reference_aluno, reference_professor, reference_cadastro;
    FirebaseAuth auth_criarAutenticacao;
    LinearLayout layout_cadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro__usuario);

        vincularComponentes();
        criarMascara();
        inicializarFirebase();


        button_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarCampos();
            }
        });
    }

    // vincular componentes da activity
    private void vincularComponentes() {
        inputLayout_nome = findViewById(R.id.inputLayout_nomeCadastro);
        inputLayout_email = findViewById(R.id.inputLayout_emailCadastro);
        inputLayout_cpf = findViewById(R.id.inputLayout_cpfCadastro);
        button_cadastrar = findViewById(R.id.button_cadastrar);
        layout_cadastro = findViewById(R.id.layout_telaCadastro);
    }

    // criar mascara do campo cpf na activity
    private void criarMascara() {
        SimpleMaskFormatter simpleMaskFormatter = new SimpleMaskFormatter("NNN.NNN.NNN-NN");
        MaskTextWatcher maskTextWatcher = new MaskTextWatcher(inputLayout_cpf.getEditText(), simpleMaskFormatter);
        inputLayout_cpf.getEditText().addTextChangedListener(maskTextWatcher);
    }

    // inicializar variáveis do firebase
    private void inicializarFirebase() {
        reference_aluno = FirebaseDatabase.getInstance().getReference();
        reference_professor = FirebaseDatabase.getInstance().getReference();
        reference_cadastro = FirebaseDatabase.getInstance().getReference();
        auth_criarAutenticacao = FirebaseAuth.getInstance();
    }

    // verificar se todos os campos foram preenchidos corretamente
    private void verificarCampos() {
        String nome = inputLayout_nome.getEditText().getText().toString().trim();
        String email = inputLayout_email.getEditText().getText().toString().trim();
        String cpf01 = inputLayout_cpf.getEditText().getText().toString().trim();
        String cpf02 = cpf01.replace(".", "");
        String cpf03 = cpf02.replace(".", "");
        String cpf = cpf03.replaceAll("-", "");

        if (!TextUtils.isEmpty(nome)){
            inputLayout_nome.setError(null);
            if ((!TextUtils.isEmpty(email)) && (email.contains("@")) && (email.contains("."))){
                inputLayout_email.setError(null);
                if ((!TextUtils.isEmpty(cpf)) && (cpf.length() == 11)){
                    inputLayout_cpf.setError(null);
                    verificarCadastroProfessor(cpf);
                }else{
                    inputLayout_cpf.setError(getString(R.string.tx_erroCampo));
                    inputLayout_cpf.requestFocus();
                }
            }else{
                inputLayout_email.setError(getString(R.string.tx_erroCampo));
                inputLayout_email.requestFocus();
            }
        }else{
            inputLayout_nome.setError(getString(R.string.tx_erroCampo));
            inputLayout_nome.requestFocus();
        }
    }

    // verificar se o usuario é um professor
    private void verificarCadastroProfessor(String cpf) {
        reference_professor.child("usuarios").child("professor").child(cpf)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null){
                        if(dataSnapshot.child("nome").getValue().toString().equals("")) {
                            String email = inputLayout_email.getEditText().getText().toString().trim();
                            criarAutenticacao(email, cpf, "professor");
                        } else {
                            abrirSnackbar(getString(R.string.tx_erroCadastro));
                        }
                    }else {
                        verificarCadastroAluno(cpf);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    abrirSnackbar(getString(R.string.tx_erro)+databaseError.toString());
                }
        });
    }

    // verificar se o usuario é um aluno
    private void verificarCadastroAluno(String cpf) {
        reference_aluno.child("usuarios").child("aluno").child(cpf)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null){
                            if(dataSnapshot.child("nome").getValue().toString().equals("")) {
                                String email = inputLayout_email.getEditText().getText().toString().trim();
                                criarAutenticacao(email, cpf, "aluno");
                            } else {
                                abrirSnackbar(getString(R.string.tx_erroCadastro));
                            }
                        }else {
                            abrirSnackbar(getString(R.string.tx_erroCPF));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        abrirSnackbar(getString(R.string.tx_erro)+databaseError.toString());
                    }
                });
    }

    // criar autenticação do firebase para login do usuario
    private void criarAutenticacao(String email, String cpf, String tipoUsuario) {
        auth_criarAutenticacao.createUserWithEmailAndPassword(email, cpf).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    completarCadastroUsuario(cpf, tipoUsuario);
                }else{
                    if (task.getException() instanceof FirebaseAuthUserCollisionException){
                        abrirSnackbar(getString(R.string.tx_emailCadastrado));
                    }else{
                        abrirSnackbar(getString(R.string.tx_usuarioFalha));
                    }
                }
            }
        });
    }

    // completar cadastro do usuario no firebase
    private void completarCadastroUsuario(String cpf, String tipoUsuario) {

        Usuario usuario = new Usuario();
        usuario.nome = inputLayout_nome.getEditText().getText().toString().trim();
        usuario.email = inputLayout_email.getEditText().getText().toString().trim();
        usuario.cpf = cpf;
        usuario.nomeLowerCase = usuario.nome.toLowerCase();

        reference_cadastro.child("usuarios").child(tipoUsuario).child(usuario.cpf).child("email").setValue(usuario.email);
        reference_cadastro.child("usuarios").child(tipoUsuario).child(usuario.cpf).child("nome").setValue(usuario.nome);
        reference_cadastro.child("usuarios").child(tipoUsuario).child(usuario.cpf).child("nomeLowerCase").setValue(usuario.nomeLowerCase);
        abrirLogin();
    }

    // abrir tela de login para o usuario e excluir 'pilha de intents' abertas anteriormente
    private void abrirLogin() {
        Toast.makeText(getApplicationContext(), getString(R.string.tx_cadastroSucesso), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), Tela_Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // abrir mensagem no layout da activity
    private void abrirSnackbar(String mensagem) {
        InputMethodManager teclado = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(inputLayout_cpf.getEditText().getWindowToken(), 0);

        Snackbar snackbar = Snackbar.make(layout_cadastro, mensagem, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

}
