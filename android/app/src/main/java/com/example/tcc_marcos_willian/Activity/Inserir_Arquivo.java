package com.example.tcc_marcos_willian.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tcc_marcos_willian.Modelos.Upload_Sistema;
import com.example.tcc_marcos_willian.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class Inserir_Arquivo extends AppCompatActivity {

    TextInputLayout inputLayout_nomeArquivo;
    Button button_selecionarPDF, button_selecionarDOCX, button_selecionarPPTX, button_selecionarIMG, button_apagarArquivo;
    StorageReference reference_arquivo;
    DatabaseReference reference_InserirSistema, reference_lista, reference_notification, reference_apagarArquivo;
    LinearLayout layout_inserirArquivo;
    ListView listView_arquivosSistema;
    ArrayList<String> arrayList = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    String tipoDocumento, extensao, ext;
    int posicao;
    String nomeArquivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inserir__arquivo);

        posicao = -1;

        vincularComponentes();
        inicializarFirebase();
        criarArrayAdapter();
        preencherArrayList();
        verificarMudancasFirebase();

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

        button_selecionarPPTX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipoDocumento = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                extensao = ".pptx";
                verificarCampo();
            }
        });

        button_selecionarIMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipoDocumento = "image/*";
                extensao = ".jpg";
                verificarCampo();
            }
        });

        listView_arquivosSistema.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                posicao = position;
                nomeArquivo = arrayList.get(position);
            }
        });

        button_apagarArquivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (posicao!=-1) {
                    String nome = nomeSharedPreference();
                    apagarArquivoFirebase(nomeArquivo, nome);
                } else {
                    abrirSnackbar(getString(R.string.tx_selecionarArquivoSistema));
                }
            }
        });
    }

    // vincular componentes da activity
    private void vincularComponentes() {
        inputLayout_nomeArquivo = findViewById(R.id.inputLayout_nomeArquivoInserirArquivo);
        button_selecionarPDF = findViewById(R.id.button_selecionarPDFInserirArquivo);
        button_selecionarDOCX = findViewById(R.id.button_selecionarDOCXInserirArquivo);
        button_selecionarPPTX = findViewById(R.id.button_selecionarPPTXInserirArquivo);
        button_selecionarIMG = findViewById(R.id.button_selecionarIMGInserirArquivo);
        layout_inserirArquivo = findViewById(R.id.layout_inserirArquivo);
        listView_arquivosSistema = findViewById(R.id.listView_arquivosCadastradosProfessor);
        button_apagarArquivo = findViewById(R.id.button_apagarArquivoProfessor);
    }

    // inicializar variaveis do firebase
    private void inicializarFirebase() {
        reference_InserirSistema = FirebaseDatabase.getInstance().getReference();
        reference_lista = FirebaseDatabase.getInstance().getReference();
        reference_notification = FirebaseDatabase.getInstance().getReference();
        reference_apagarArquivo = FirebaseDatabase.getInstance().getReference();
        reference_arquivo = FirebaseStorage.getInstance().getReference();
    }

    // criar adapter para preencher listView
    private void criarArrayAdapter() {
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, arrayList);
        listView_arquivosSistema.setAdapter(arrayAdapter);
    }

    // prencher arraylist de arquivos do professor
    private void preencherArrayList() {
        arrayList.clear();
        String nome = nomeSharedPreference();

        reference_lista.child("sistema").child(nome).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String nomeArquivoFirebase = dataSnapshot.getKey();
                arrayList.add(nomeArquivoFirebase);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Erro: "+databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // pegar nome do usuario logado
    private String nomeSharedPreference() {
        SharedPreferences preferences = getSharedPreferences("dadosUsuario", Context.MODE_PRIVATE);
        String nome = preferences.getString("nome", "não encontrado");
        return nome;
    }

    // monitorar mudanças no status de alguma impressão e notificar o professor caso mude
    private void verificarMudancasFirebase() {
        reference_notification.child("imprimir").child("professor").orderByChild("nomeUsuario").equalTo(nomeSharedPreference())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        String channelId = "some_channel_id";
                        CharSequence channelName = "Some Channel";
                        int importance = NotificationManager.IMPORTANCE_LOW;
                        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(Color.RED);
                        notificationChannel.enableVibration(true);
                        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                        notificationManager.createNotificationChannel(notificationChannel);

                        // criar notificação para o usuario
                        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(Inserir_Arquivo.this, notificationChannel.getId())
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle("Printup")
                                .setDefaults(Notification.DEFAULT_SOUND)
                                .setContentText("Seu arquivo já foi impresso, pode pegá-lo no xerox")
                                .setAutoCancel(true);

                        NotificationManager mNotifymgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        mNotifymgr.notify(1, mBuilder.build());
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    // verificar se campo foi preenchido
    private void verificarCampo() {
        String nomeArquivo = inputLayout_nomeArquivo.getEditText().getText().toString().trim();

        if (!TextUtils.isEmpty(nomeArquivo)){
            if((nomeArquivo.contains(".")) || (nomeArquivo.contains("#")) || (nomeArquivo.contains("$")) || (nomeArquivo.contains("[")) || (nomeArquivo.contains("]"))) {
                inputLayout_nomeArquivo.setError(getString(R.string.tx_nomeArquivo));
                inputLayout_nomeArquivo.requestFocus();
            } else {
                inputLayout_nomeArquivo.setError(null);
                abrirSelecaoArquivo();
            }
        }else{
            inputLayout_nomeArquivo.setError(getString(R.string.tx_erroCampo));
            inputLayout_nomeArquivo.requestFocus();
        }
    }

    // abrir midia do celular para selecionar arquivo
    private void abrirSelecaoArquivo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(tipoDocumento);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.tx_selecionarArquivo)), 1);
    }

    // pegar arquivo selecionado
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            enviarArquivo(data.getData());
        }
    }

    // salvar arquivo no storage do firebase
    private void enviarArquivo(Uri data) {
        String nome = nomeSharedPreference();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.tx_enviando));
        progressDialog.show();

        reference_arquivo.child("sistema").child(nome).child(inputLayout_nomeArquivo.getEditText().getText().toString()+extensao).putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    salvarArquivoFirebaseDatabase(taskSnapshot);
                    Abrirmenu();
                    progressDialog.dismiss();
                }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage(getString(R.string.tx_progressoDialog)+" "+(int)progress+getString(R.string.tx_porcento));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        abrirSnackbar(getString(R.string.tx_erroUpload)+" "+e.toString());
                        progressDialog.dismiss();
                    }
                });
    }

    // salvar arquivo no realtime do firebase
    private void salvarArquivoFirebaseDatabase(UploadTask.TaskSnapshot taskSnapshot) {
        String nome = nomeSharedPreference();

        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
        while (!uri.isComplete());
        Uri url = uri.getResult();
        Upload_Sistema upload = new Upload_Sistema();
        upload.url = url.toString();
        upload.nomeArquivo = inputLayout_nomeArquivo.getEditText().getText().toString();

        reference_InserirSistema.child("sistema").child(nome).child(inputLayout_nomeArquivo.getEditText().getText().toString()).setValue(upload);
        abrirSnackbar(getString(R.string.tx_UploadComSucesso));
    }

    // apagar arquivo cadastrado pelo professor do firebase database e storage
    private void apagarArquivoFirebase(String nomeArquivo, String nome) {

        // pegar extensão do arquivo
        reference_apagarArquivo.child("sistema").child(nome).child(nomeArquivo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String url = dataSnapshot.child("url").getValue().toString();

                        if(url.contains(".docx")) {
                            ext = ".docx";
                        } else if(url.contains(".pdf")) {
                            ext = ".pdf";
                        } else if(url.contains(".pptx")) {
                            ext = ".pptx";
                        } else {
                            ext = ".jpg";
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        // apagar arquivo do database
        reference_apagarArquivo.child("sistema").child(nome).child(nomeArquivo).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String nomeCompletoArquivo = nomeArquivo+ext;

                        // apagar arquivo do storage
                        reference_arquivo.child("sistema").child(nome).child(nomeCompletoArquivo).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        criarArrayAdapter();
                                        preencherArrayList();
                                        abrirSnackbar(getString(R.string.tx_apagadoSucesso));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        abrirSnackbar(getString(R.string.tx_apagarArquivoStorage));
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        abrirSnackbar(getString(R.string.tx_apagarArquivoStorage));
                    }
                });
    }

    // abrir menu do professor e apagar 'pilha de activities' anteriores
    private void Abrirmenu() {
        Intent intent = new Intent(getApplicationContext(), Menu_Professor.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // mostrar mensagem no layout da activity
    private void abrirSnackbar(String mensagem) {
        InputMethodManager teclado = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(inputLayout_nomeArquivo.getEditText().getWindowToken(), 0);

        Snackbar snackbar = Snackbar.make(layout_inserirArquivo, mensagem, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
