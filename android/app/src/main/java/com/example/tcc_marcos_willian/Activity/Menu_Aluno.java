package com.example.tcc_marcos_willian.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.example.tcc_marcos_willian.Fragments.FragmentPagerAdapterAluno;
import com.example.tcc_marcos_willian.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Menu_Aluno extends AppCompatActivity {

    TabLayout tabLayout_menuAluno;
    ViewPager viewPager_menuAluno;
    DatabaseReference reference_notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu__aluno);

        vincularComponentes();
        inicializarFirebase();
        criarAdapter();
        verificarMudancasFirebase();
    }

    // vincular componentes da activity
    private void vincularComponentes() {
        tabLayout_menuAluno = findViewById(R.id.tabLayout_menuAluno);
        viewPager_menuAluno = findViewById(R.id.viewPager_menuAluno);
    }

    // inicializar variáveis do firebase
    private void inicializarFirebase() {
        reference_notification = FirebaseDatabase.getInstance().getReference();
    }

    // criar adaptador para exibir fragment na activity
    private void criarAdapter() {
        viewPager_menuAluno.setAdapter(new FragmentPagerAdapterAluno(getSupportFragmentManager(), getResources().getStringArray(R.array.titulos_menu)));
        tabLayout_menuAluno.setupWithViewPager(viewPager_menuAluno);
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


                        // criação da notificação
                        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(Menu_Aluno.this, notificationChannel.getId())
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

    // pegar nome guardado na memória da aplicação
    private String nomeSharedPreference() {
        SharedPreferences preferences = getSharedPreferences("dadosUsuario", Context.MODE_PRIVATE);
        String nome = preferences.getString("nome", "não encontrado");
        return nome;
    }
}