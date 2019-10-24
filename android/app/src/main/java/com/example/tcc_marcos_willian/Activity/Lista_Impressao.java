package com.example.tcc_marcos_willian.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tcc_marcos_willian.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Lista_Impressao extends AppCompatActivity {

    ListView listView_arquivos;
    ArrayList<String> arrayList = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    DatabaseReference reference_arquivos, reference_notification;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista__impressao);

        vincularComponentes();
        inicializarFirebase();
        preencherArrayList();
        criarArrayAdapter();
        verificarMudancasFirebase();
    }

    // vincular componentes da activity
    private void vincularComponentes() {
        listView_arquivos = findViewById(R.id.listView_arquivosImpressao);
    }

    // inicializar variaveis firebase
    private void inicializarFirebase() {
        reference_arquivos = FirebaseDatabase.getInstance().getReference();
        reference_notification = FirebaseDatabase.getInstance().getReference();
    }

    // preencher lista de arquivos enviados para impressão firebase
    private void preencherArrayList() {
        String tipo = pegarTipoIntent();
        String nomeUsuario = nomeSharedPreference();

        reference_arquivos.child("imprimir").child(tipo).orderByChild("nomeUsuario").equalTo(nomeUsuario)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        reference_arquivos.child("imprimir").child(tipo).child(dataSnapshot.getKey().toString())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String nome = dataSnapshot.child("nomeUsuario").getValue().toString();
                                        String copias = dataSnapshot.child("numeroCopias").getValue().toString();
                                        String status = dataSnapshot.child("status").getValue().toString();
                                        String url = dataSnapshot.child("url").getValue().toString();
                                        String ext = "";

                                        if(url.contains(".docx")) {
                                            ext = ".docx";
                                        } else {
                                            ext = ".pdf";
                                        }

                                        if(nomeUsuario.equals(nome)){
                                            i = i +1;
                                            String nomeLista = "arquivo"+ i +ext+" -- "+status+" - "+copias+" cópias";
                                            arrayList.add(nomeLista);
                                            arrayAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
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

                    }
                });
    }

    // pegar tipo de usuario que chamou a intent(aluno ou professor)
    private String pegarTipoIntent() {
        Intent intent = getIntent();
        String tipo = intent.getStringExtra("tipo");
        return tipo;
    }

    // pegar nome do usuario salvo na memoria da aplicação
    private String nomeSharedPreference() {
        SharedPreferences preferences = getSharedPreferences("dadosUsuario", Context.MODE_PRIVATE);
        String nome = preferences.getString("nome", "não encontrado");
        return nome;
    }

    // criar adapter para prencher listiView
    private void criarArrayAdapter() {
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, arrayList);
        listView_arquivos.setAdapter(arrayAdapter);
    }

    // monitorar mudanças no status de alguma impressão e notificar o aluno caso mude
    private void verificarMudancasFirebase() {
        reference_notification.child("imprimir").child("aluno").orderByChild("nomeUsuario").equalTo(nomeSharedPreference())
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
                        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(Lista_Impressao.this, notificationChannel.getId())
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle(getString(R.string.tx_tituloNotification))
                                .setDefaults(Notification.DEFAULT_SOUND)
                                .setContentText(getString(R.string.tx_corpoNotification))
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
}
