package com.example.tcc_marcos_willian.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.tcc_marcos_willian.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class Caixa_Dialogo extends AppCompatDialogFragment {

    private TextInputLayout lt_numeroCopias;
    pegarNumeroCopias numeroCopias;
    LinearLayout linearLayout;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.caixa__dialogo, null);

        builder.setView(view).setTitle(getString(R.string.tx_confirmarNumeroCopias));
        builder.setNegativeButton(getString(R.string.tx_cancelar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(getString(R.string.tx_enviar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            String numCopias = lt_numeroCopias.getEditText().getText().toString().trim();

            if (!TextUtils.isEmpty(numCopias)){
                if(numCopias.equals("0")) {
                    Toast.makeText(getContext(), getString(R.string.tx_erroCampo), Toast.LENGTH_LONG).show();
                } else if(numCopias.equals("00")) {
                    Toast.makeText(getContext(), getString(R.string.tx_erroCampo), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.tx_enviando), Toast.LENGTH_LONG).show();
                    numeroCopias.retornaNumeroCopias(numCopias);
                }
            }else{
                Toast.makeText(getContext(), getString(R.string.tx_erroCampo), Toast.LENGTH_LONG).show();
            }
            }
        });

        lt_numeroCopias = view.findViewById(R.id.lyt_NumeroCopiasCaixaDialogo);
        linearLayout = view.findViewById(R.id.layoutArquivoProfessor);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            numeroCopias = (pegarNumeroCopias) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+getString(R.string.tx_erro)+e.toString());
        }
    }

    public interface pegarNumeroCopias{
        void retornaNumeroCopias(String numeroCopias);
    }
}
