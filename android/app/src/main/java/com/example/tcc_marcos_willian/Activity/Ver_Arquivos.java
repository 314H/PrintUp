package com.example.tcc_marcos_willian.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tcc_marcos_willian.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;

public class Ver_Arquivos extends AppCompatActivity {

    ListView listView_professores;
    DatabaseReference reference_professores, reference_notification;
    ArrayList<String> arrayProfessores = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ver__arquivos);

        vincularComponente();
        inicializarFirebase();
        criarArrayAdapter();
        preencherArrayList();
        verificarMudancasFirebase();

        listView_professores.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String professor = arrayProfessores.get(position);
                arquivoProf(professor);
            }
        });
    }

    // vincular componentes da activity
    private void vincularComponente() {
        listView_professores = findViewById(R.id.listView_professoresVerArquivos);
    }

    // inicializar variáveis do firebase
    private void inicializarFirebase() {
        reference_professores = FirebaseDatabase.getInstance().getReference();
        reference_notification = FirebaseDatabase.getInstance().getReference();
    }

    // criar adapter para preencher o listView
    private void criarArrayAdapter() {
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayProfessores);
        listView_professores.setAdapter(arrayAdapter);
    }

    // preencher lista de professores com nomes do firebase
    private void preencherArrayList() {
        reference_professores.child("sistema").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String value = dataSnapshot.getKey();
                arrayProfessores.add(value);
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

    // monitorar mudanças no status de alguma impressão e notificar o usuario caso mude
    private void verificarMudancasFirebase() {
        reference_notification.child("imprimir").child("aluno").orderByChild("nomeUsuario").equalTo(nomeSharedPreference())
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

                        // criação da notificação
                        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(Ver_Arquivos.this, notificationChannel.getId())
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
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @androidx.annotation.Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    // pegar nome do aluno para ser usado na verificação de mudanças de arquivos de impressão do firebase
    private String nomeSharedPreference() {
        SharedPreferences preferences = getSharedPreferences("dadosUsuario", Context.MODE_PRIVATE);
        String nome = preferences.getString("nome", "não encontrado");
        return nome;
    }

    // intent para abrir activity com arquivos do professor selecionado
    public void arquivoProf(String professor){
        Intent intent = new Intent(getApplicationContext(), Arquivo_Professor.class);
        intent.putExtra("nomeProfessor", professor);
        startActivity(intent);
    }
}
