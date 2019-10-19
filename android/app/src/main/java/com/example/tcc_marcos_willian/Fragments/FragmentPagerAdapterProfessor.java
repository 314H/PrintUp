package com.example.tcc_marcos_willian.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FragmentPagerAdapterProfessor extends androidx.fragment.app.FragmentPagerAdapter {

    private String[] titulos;

    public FragmentPagerAdapterProfessor(@NonNull FragmentManager fm, String[] titulos) {
        super(fm);
        this.titulos = titulos;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new Fragment_MenuProfessor();
            case 1:
                return new Fragment_ImprimirUsuario();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return this.titulos.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return this.titulos[position];
    }
}
