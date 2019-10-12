package com.example.tcc_marcos_willian.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tcc_marcos_willian.Modelos.Upload_Arquivo;
import com.example.tcc_marcos_willian.Modelos.Upload_Sistema;
import com.example.tcc_marcos_willian.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;

public class Arquivo_Professor extends AppCompatActivity implements Caixa_Dialogo.pegarNumeroCopias{

    TextView tx_nomeProfessor;
    ListView lv_listaArquivos;
    Button bt_download, bt_imprimir;
    int posicao;
    String url, nome;
    DatabaseReference databaseReference, referenceDownload, referenceImprimir, referenceMudanca;
    ArrayList<String> arrayList = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    static final int PERMISSION_STORAGE_CODE = 1000;
    String nomeArquivo;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arquivo__professor);

        posicao=-1;

        vincularComponentes();

        inicializarFirebase();

        preencherArrayList();
        criarArrayAdapter();
        verificarMudancasFirebase();

        tx_nomeProfessor.setText(pegarSharedPreference());

        lv_listaArquivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                posicao = position;
                nomeArquivo = arrayList.get(position);
            }
        });

        bt_imprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (posicao!=-1){
                    pegarURL(nomeArquivo, "imprimir");
                }else {
                    abrirSnackbar(getString(R.string.tx_escolherArquivoDownload));
                }

            }
        });

        bt_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(posicao!=-1){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                            ActivityCompat.requestPermissions(Arquivo_Professor.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE_CODE);
                        }else {
                            pegarURL(nomeArquivo, "download");
                        }
                    }else {
                        pegarURL(nomeArquivo, "download");
                    }
                }else {
                    abrirSnackbar(getString(R.string.tx_escolherArquivo));
                }
            }
        });
    }

    private void verificarMudancasFirebase() {
        referenceMudanca.child("imprimir").child("aluno").orderByChild("nome_usuario").equalTo(pegarSharedPreference())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @androidx.annotation.Nullable String s) {

                    }

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @androidx.annotation.Nullable String s) {


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


                        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(Arquivo_Professor.this, notificationChannel.getId())
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
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @androidx.annotation.Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void vincularComponentes() {
        tx_nomeProfessor = findViewById(R.id.txt_NomeProfessorArquivoProfessor);
        lv_listaArquivos = findViewById(R.id.ltv_ArquivoProfessor);
        bt_download = findViewById(R.id.btn_DownloadArquivoProfessor);
        bt_imprimir = findViewById(R.id.btn_ImprimirArquivoProfessor);
        linearLayout = findViewById(R.id.layoutArquivoProfessor);
    }

    private void inicializarFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        referenceImprimir = FirebaseDatabase.getInstance().getReference();
        referenceDownload = FirebaseDatabase.getInstance().getReference();
        referenceMudanca = FirebaseDatabase.getInstance().getReference();
    }

    private void criarArrayAdapter() {
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, arrayList);
        lv_listaArquivos.setAdapter(arrayAdapter);
    }

    private void preencherArrayList() {
        databaseReference.child("sistema").child(pegarSharedPreference()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String value = dataSnapshot.getKey();
                arrayList.add(value);
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
                abrirSnackbar(R.string.tx_erro+databaseError.toString());
            }
        });
    }

    private String pegarSharedPreference() {
        Intent intent = getIntent();
        String nome = intent.getStringExtra("nomeProfessor");
        return nome;
    }

    private void pegarURL(String nomeArquivo, final String tipoOperacao) {
        referenceDownload.child("sistema").child(tx_nomeProfessor.getText().toString()).child(nomeArquivo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    url = dataSnapshot.child("url").getValue().toString();
                    String extensao = pegarExtensaoArquivo(url);
                    if (tipoOperacao.equals("imprimir")){
                        if(extensao.equals(".pptx") || extensao.equals(".jpg")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.tx_tipoArquivoImpressao), Toast.LENGTH_SHORT).show();
                        } else {
                            abrirCaixaDialogo();
                        }
                    }else if (tipoOperacao.equals("download")){
                        iniciarDownload();
                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                    abrirSnackbar(getString(R.string.tx_erro)+" "+databaseError.toString());
            }
        });
    }

    private void abrirCaixaDialogo() {
        Caixa_Dialogo caixa_dialogo = new Caixa_Dialogo();
        caixa_dialogo.show(getSupportFragmentManager(), tx_nomeProfessor.getText().toString());
    }

    @Override
    public void retornaNumeroCopias(String numeroCopias) {
        Upload_Arquivo upload = new Upload_Arquivo();
        upload.numero_copias = numeroCopias;
        upload.url = url;
        upload.nome_usuario = pegarNomeAluno();
        upload.status = "aguardando";
        uploadArquivo(upload);
    }

    private String pegarNomeAluno() {
        SharedPreferences preferences = getSharedPreferences("DadosUsuario", Context.MODE_PRIVATE);
        nome = preferences.getString("nome", "não encontrado");
        return nome;
    }

    private void uploadArquivo(Object upload) {
        referenceImprimir.child("imprimir").child("aluno").child(referenceDownload.push().getKey()).setValue(upload)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        abrirSnackbar(getString(R.string.tx_UploadComSucesso));
                        abrirMenu();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        abrirSnackbar(getString(R.string.tx_erroUpload)+" "+e.toString());
                    }
            });
    }

    private void iniciarDownload() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(getString(R.string.tx_download));
        request.setDescription(getString(R.string.tx_baixando));

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nomeArquivo+pegarExtensaoArquivo(url));

        DownloadManager manager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        abrirMenu();
    }

    private String pegarExtensaoArquivo(String url) {
        String extensao = "";
        if (url.contains(".docx")){
            extensao = ".docx";
        }else if (url.contains(".pdf")){
            extensao = ".pdf";
        }else if (url.contains(".pptx")){
            extensao = ".pptx";
        }else if (url.contains(".jpg")) {
            extensao = ".jpg";
        }
        return extensao;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_STORAGE_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    iniciarDownload();
                }else {
                    abrirSnackbar(getString(R.string.tx_erroPermissao));
                }
            }
        }
    }

    private void abrirMenu(){
        Intent intent = new Intent(getApplicationContext(), Menu_Aluno.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void abrirSnackbar(String mensagem) {
        InputMethodManager teclado = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(lv_listaArquivos.getWindowToken(), 0);

        Snackbar snackbar = Snackbar.make(linearLayout, mensagem, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
