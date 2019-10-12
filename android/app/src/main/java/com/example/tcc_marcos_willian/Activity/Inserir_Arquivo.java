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

    TextInputLayout lt_nomeArquivo;
    Button bt_selecionarPDF, bt_selecionarDOCX, bt_selecionarPPTX, bt_selecionarIMG, bt_apagarArquivo;
    StorageReference storageReference, referenceArquivo;
    DatabaseReference databaseReference, referenceArquivos, referenceMudanca;
    String tipoDocumento, extensao, ext;
    LinearLayout linearLayout;
    ListView lv_arquivos;
    ArrayList<String> arrayArquivos = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
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

        bt_selecionarPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipoDocumento = "application/pdf";
                extensao = ".pdf";
                verificarCampo();
            }
        });

        bt_selecionarDOCX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipoDocumento = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                extensao = ".docx";
                verificarCampo();
            }
        });

        bt_selecionarPPTX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipoDocumento = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                extensao = ".pptx";
                verificarCampo();
            }
        });

        bt_selecionarIMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipoDocumento = "image/*";
                extensao = ".jpg";
                verificarCampo();
            }
        });

        lv_arquivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                posicao = position;
                nomeArquivo = arrayArquivos.get(position);
            }
        });

        bt_apagarArquivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (posicao!=-1) {
                    String nome = pegarSharedPreference();
                    apagarArquivoFirebase(nomeArquivo, nome);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.tx_selecionarArquivoSistema), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verificarMudancasFirebase() {
        referenceMudanca.child("imprimir").child("professor").orderByChild("nome_usuario").equalTo(pegarSharedPreference())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        String channelId = "some_channel_id";
                        CharSequence channelName = "Some Channel";
                        int importance = NotificationManager.IMPORTANCE_LOW;
                        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(Color.RED);
                        notificationChannel.enableVibration(true);
                        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                        notificationManager.createNotificationChannel(notificationChannel);


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

    private void apagarArquivoFirebase(String nomeArquivo, String nome) {
        referenceArquivos.child("sistema").child(nome).child(nomeArquivo)
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

        referenceArquivos.child("sistema").child(nome).child(nomeArquivo).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String nomeCompletoArquivo = nomeArquivo+ext;

                        referenceArquivo.child("sistema").child(nome).child(nomeCompletoArquivo).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        criarArrayAdapter();
                                        preencherArrayList();
                                        Toast.makeText(getApplicationContext(), getString(R.string.tx_apagadoSucesso), Toast.LENGTH_LONG).show();
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

    private void preencherArrayList() {
        arrayArquivos.clear();
        String nome = pegarSharedPreference();
        referenceArquivos.child("sistema").child(nome).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                arrayArquivos.add(key);
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

    private void criarArrayAdapter() {
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, arrayArquivos);
        lv_arquivos.setAdapter(arrayAdapter);
    }

    private void vincularComponentes() {
        lt_nomeArquivo = findViewById(R.id.lyt_NomeArquivoInserirArquivo);
        bt_selecionarPDF = findViewById(R.id.btn_SelecionarPDFInserirArquivo);
        bt_selecionarDOCX = findViewById(R.id.btn_SelecionarDOCXInserirArquivo);
        bt_selecionarPPTX = findViewById(R.id.btn_SelecionarPPTXInserirArquivo);
        bt_selecionarIMG = findViewById(R.id.btn_SelecionarIMGInserirArquivo);
        linearLayout = findViewById(R.id.layoutInserirArquivo);
        lv_arquivos = findViewById(R.id.ltv_ArquivosCadastradosProfessor);
        bt_apagarArquivo = findViewById(R.id.btn_ApagarArquivoProfessor);
    }

    private void inicializarFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        referenceArquivos = FirebaseDatabase.getInstance().getReference();
        referenceMudanca = FirebaseDatabase.getInstance().getReference();
        referenceArquivo = FirebaseStorage.getInstance().getReference();
    }

    private String pegarSharedPreference() {
        SharedPreferences preferences = getSharedPreferences("DadosUsuario", Context.MODE_PRIVATE);
        String nome = preferences.getString("nome", "não encontrado");
        return nome;
    }

    private void verificarCampo() {
        String nomeArquivo = lt_nomeArquivo.getEditText().getText().toString().trim();

        if (!TextUtils.isEmpty(nomeArquivo)){
            if((nomeArquivo.contains(".")) || (nomeArquivo.contains("#")) || (nomeArquivo.contains("$")) || (nomeArquivo.contains("[")) || (nomeArquivo.contains("]"))) {
                lt_nomeArquivo.setError(getString(R.string.tx_erroCampo));
                lt_nomeArquivo.requestFocus();
            } else {
                lt_nomeArquivo.setError(null);
                abrirSelecaoArquivo();
            }
        }else{
            lt_nomeArquivo.setError(getString(R.string.tx_erroCampo));
            lt_nomeArquivo.requestFocus();
        }
    }

    private void abrirSelecaoArquivo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(tipoDocumento);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.tx_selecionarArquivo)), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            enviarArquivo(data.getData());
        }
    }

    private void enviarArquivo(Uri data) {
        String nome = pegarSharedPreference();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.tx_enviando));
        progressDialog.show();

        storageReference.child("sistema").child(nome)
                .child(lt_nomeArquivo.getEditText().getText().toString()+extensao)
                .putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    salvarImpressaoNoFirebaseDatabase(taskSnapshot);
                    Abrirmenu();
                    progressDialog.dismiss();
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
                    abrirSnackbar(getString(R.string.tx_erroUpload)+" "+e.toString());
                    progressDialog.dismiss();
                }
            });
    }

    private void salvarImpressaoNoFirebaseDatabase(UploadTask.TaskSnapshot taskSnapshot) {
        String nome = pegarSharedPreference();
        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
        while (!uri.isComplete());
        Uri url = uri.getResult();
        Upload_Sistema upload = new Upload_Sistema();
        upload.url = url.toString();
        upload.nome_Arquivo = lt_nomeArquivo.getEditText().getText().toString();
        databaseReference.child("sistema").child(nome)
                .child(lt_nomeArquivo.getEditText().getText().toString()).setValue(upload);
        abrirSnackbar(getString(R.string.tx_UploadComSucesso));
    }

    private void Abrirmenu() {
        Intent intent = new Intent(getApplicationContext(), Menu_Professor.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void abrirSnackbar(String mensagem) {
        InputMethodManager teclado = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(lt_nomeArquivo.getEditText().getWindowToken(), 0);

        Snackbar snackbar = Snackbar.make(linearLayout, mensagem, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
