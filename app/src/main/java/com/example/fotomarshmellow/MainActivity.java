package com.example.fotomarshmellow;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ImageView imagenFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imagenFoto = (ImageView)findViewById(R.id.idImagen);
    }
    public void onClick(View view) {
        cargarImagen();
    }

    private void cargarImagen() {

        final CharSequence[] opciones = {"Tomar una fotoku","Cargar imagen","Cancelar"};
        final AlertDialog.Builder alertaOpciones = new AlertDialog.Builder(MainActivity.this);
        alertaOpciones.setTitle("Selecione una opción");
        alertaOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(opciones[which].equals("Tomar una fotoku")){

                    Toast.makeText(getApplicationContext(),"Tomar foto",Toast.LENGTH_SHORT).show();
                }else{
                    if(opciones[which].equals("Cargar imagen")){

                        //ACTION_PICK o ACTION_GET_CONTENT
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent.createChooser(intent,"Selecciona una opcion:"),10);
                    }else{
                        dialog.dismiss();
                    }
                }

            }
        });
        alertaOpciones.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            Uri patch = data.getData();
            imagenFoto.setImageURI(patch);
        }
    }
}
