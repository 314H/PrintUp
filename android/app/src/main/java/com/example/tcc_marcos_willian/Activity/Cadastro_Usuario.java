package com.example.tcc_marcos_willian.Activity;

import com.example.tcc_marcos_willian.Modelos.Usuario;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.tcc_marcos_willian.Modelos.Aluno;
import com.example.tcc_marcos_willian.Modelos.Professor;
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

    TextInputLayout lt_nome, lt_email, lt_confirmarCPF;
    Button bt_cadastrar;
    DatabaseReference referenceAluno, referenceProfessor, referenceCadastro;
    FirebaseAuth firebaseAuth;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro__usuario);

        vincularComponentes();

        criarMascara();

        inicializarFirebase();


        bt_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarCampos();
            }
        });
    }

    private void vincularComponentes() {
        lt_nome = findViewById(R.id.lyt_NomeCadastroUsuario);
        lt_email = findViewById(R.id.lyt_EmailCadastroUsuario);
        lt_confirmarCPF = findViewById(R.id.lyt_ConfirmarCPFCadastroUsuario);
        bt_cadastrar = findViewById(R.id.btn_CadastrarUsuario);
        linearLayout = findViewById(R.id.layoutCadastroUsuario);
    }

    private void criarMascara() {
        SimpleMaskFormatter simpleMaskFormatter = new SimpleMaskFormatter("NNN.NNN.NNN-NN");
        MaskTextWatcher maskTextWatcher = new MaskTextWatcher(lt_confirmarCPF.getEditText(), simpleMaskFormatter);
        lt_confirmarCPF.getEditText().addTextChangedListener(maskTextWatcher);
    }

    private void inicializarFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        referenceProfessor = FirebaseDatabase.getInstance().getReference();
        referenceAluno = FirebaseDatabase.getInstance().getReference();
        referenceCadastro = FirebaseDatabase.getInstance().getReference();
    }

    private void verificarCampos() {
        String nome = lt_nome.getEditText().getText().toString().trim();
        String email = lt_email.getEditText().getText().toString().trim();
        String senha01 = lt_confirmarCPF.getEditText().getText().toString().trim();
        String senha02 = senha01.replace(".", "");
        String senha03 = senha02.replace(".", "");
        String cpf = senha03.replaceAll("-", "");

        if (!TextUtils.isEmpty(nome)){
            lt_nome.setError(null);
            if ((!TextUtils.isEmpty(email)) && (email.contains("@")) && (email.contains("."))){
                lt_email.setError(null);
                if ((!TextUtils.isEmpty(cpf)) && (cpf.length() == 11)){
                    lt_confirmarCPF.setError(null);
                    verificarCadastroProfessor(cpf);
                }else{
                    lt_confirmarCPF.setError(getString(R.string.tx_erroCampo));
                    lt_confirmarCPF.requestFocus();
                }
            }else{
                lt_email.setError(getString(R.string.tx_erroCampo));
                lt_email.requestFocus();
            }
        }else{
            lt_nome.setError(getString(R.string.tx_erroCampo));
            lt_nome.requestFocus();
        }
    }


    private void verificarCadastroProfessor(String cpf) {
        referenceProfessor.child("usuarios").child("professor").child(cpf)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null){
                        if(dataSnapshot.child("nome").getValue().toString().equals("")) {
                            completarCadastroUsuario(cpf, "professor");
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

    private void verificarCadastroAluno(String cpf) {
        referenceAluno.child("usuarios").child("aluno").child(cpf)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null){
                            if(dataSnapshot.child("nome").getValue().toString().equals("")) {
                                completarCadastroUsuario(cpf, "aluno");
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

    private void completarCadastroUsuario(String cpf, String tipoUsuario) {
        Usuario usuario = new Usuario();
        usuario.nome = lt_nome.getEditText().getText().toString().trim();
        usuario.email = lt_email.getEditText().getText().toString().trim();
        usuario.cpf = cpf;
        usuario.nomeLowerCase = usuario.nome.toLowerCase();
        referenceCadastro.child("usuarios").child(tipoUsuario).child(usuario.cpf).child("email").setValue(usuario.email);
        referenceCadastro.child("usuarios").child(tipoUsuario).child(usuario.cpf).child("nome").setValue(usuario.nome);
        referenceCadastro.child("usuarios").child(tipoUsuario).child(usuario.cpf).child("nomeLowerCase").setValue(usuario.nomeLowerCase);
        criarAutenticacao(usuario.email, usuario.cpf);
    }

    private void criarAutenticacao(String email, String cpf) {
        firebaseAuth.createUserWithEmailAndPassword(email, cpf).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    abrirLogin();
                }else{
                    if (task.getException() instanceof FirebaseAuthUserCollisionException){
                        abrirSnackbar(getString(R.string.tx_erroUsuario));
                    }else{
                        abrirSnackbar(getString(R.string.tx_usuarioFalha));
                    }
                }
            }
        });
    }

    private void abrirLogin() {
        Toast.makeText(getApplicationContext(), getString(R.string.tx_cadastroSucesso), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), Tela_Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void abrirSnackbar(String mensagem) {
        InputMethodManager teclado = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(lt_confirmarCPF.getEditText().getWindowToken(), 0);

        Snackbar snackbar = Snackbar.make(linearLayout, mensagem, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

}
