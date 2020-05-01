  package com.example.fotomarshmellow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Path;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.security.KeyStore;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private final String CARPETA_RAIZ="misImagenesPrueba/";//Directorio principal
    private final String RUTA_IMAGEN=CARPETA_RAIZ+"misFotos";//Carpeta donde se guarda las fotos
    final int COD_SELECCIONAR=10;
    final int COD_FOTO=20;

    ImageView imagenFoto;
    Button btnCargarfoto;
    String path; //almacena la ruta de la imagen


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagenFoto = (ImageView)findViewById(R.id.idImagen);
        btnCargarfoto = (Button)findViewById(R.id.btnCargarImg);

        if(validadPermisos()){
            btnCargarfoto.setEnabled(true);

        }else {
            btnCargarfoto.setEnabled(false);

        }
    }

    private boolean validadPermisos() {
        //Validamos la version del dispositivo
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return true;
        }
        //Checamos que los permisos esten vigentes, tanto de camara como de almacenamiento
        if ((checkSelfPermission(CAMERA)== PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)){
            return true;
        }
        if((shouldShowRequestPermissionRationale(CAMERA))||(shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE))){
            cargarDialogoRecomendacion();

        }else{
            // en caso de que sea una version menor a la de MarshMellow
            requestPermissions( new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
        }
        return  false;
    }

    //Se sobreEscribe el metodo onREquestPermissionResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==100){
            if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                btnCargarfoto.setEnabled(true);
            }else{
                solicitarPermisosManual();


            }

        }
    }
    // En caso de no dar los permisos de primera instancia se pondra un alert para que lo haga de forma manual el usuario
    private void solicitarPermisosManual() {
        final CharSequence[] opciones = {"Si","No"}; //cargamos las opciones
        //Se crea el cuadro de dialogo en el mismo Activity y tambien se crea el objeto alertaOpciones
        final AlertDialog.Builder alertaOpciones = new AlertDialog.Builder(MainActivity.this);
        //Titulo del mensaje del cuadro de dialogo
        alertaOpciones.setTitle("Desea configurar los permisos de manera manual?");
        //Se carga el onClick listener a las opciones de nuestro alertDialog
        alertaOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Wich es el dato que recibimos como parametro
                if(opciones[which].equals("Si")){
                    Intent intent = new Intent();
                    //Asignamos a nuestro objeto la accion
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);

                }else{
                    Toast.makeText(getApplicationContext(),"No se aceptaron los permisos",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                }

            }
        });
        //Se enseña alerDialog
        alertaOpciones.show();
    }

    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(MainActivity.this);
        dialogo.setTitle("Permisos desactivados");
        dialogo.setMessage("Debe de aceptar los permisos para el correcto funcionamiento de la App");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
            }
        });
        dialogo.show(); //muestra el activityResult con las especificaciones previas

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

                    tomarFotografia();
                }else{
                    if(opciones[which].equals("Cargar imagen")){

                        //ACTION_PICK o ACTION_GET_CONTENT
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent.createChooser(intent,"Selecciona una opcion:"),COD_SELECCIONAR);
                    }else{
                        dialog.dismiss();
                    }
                }

            }
        });
        alertaOpciones.show();
    }

    private void tomarFotografia() {
        // Se crea el objeto fileImagen de tipo File
        File fileImagen=new File(Environment.getExternalStorageDirectory(),RUTA_IMAGEN);
        //.exists(); genera un booleano true or false, dependera si la variable contiene informacion sera true
        boolean isCreada=fileImagen.exists();
        //Se inicializa nuestra variable nombre vacia
        String nombreImagen="";
        if(isCreada==false){
            isCreada=fileImagen.mkdirs();
        }

        if(isCreada==true){
            nombreImagen=(System.currentTimeMillis()/1000)+".jpg";
        }


        path=Environment.getExternalStorageDirectory()+
                File.separator+RUTA_IMAGEN+File.separator+nombreImagen;

        File imagen=new File(path);

        Intent intent=null;
        intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ////
        //SE comprueba la version de android
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
        {
            //Si es version superior a Nugat para guardar imagen son estas lineas
            String authorities=getApplicationContext().getPackageName()+".provider";
            Uri imageUri=FileProvider.getUriForFile(this,authorities,imagen);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }else
        {
            //En caso de que la version sea anterior a la Nugat
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagen));
        }
        //Se cargar el activityResult con el intent y el requestCode
        startActivityForResult(intent,COD_FOTO);

        ////
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){

            switch (requestCode){
                case COD_SELECCIONAR:
                    Uri miPath=data.getData();
                    imagenFoto.setImageURI(miPath);
                    break;

                case COD_FOTO:
                    //Cuando el caso es foto o codigo, es el cod 20
                    MediaScannerConnection.scanFile(this, new String[]{path}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                        //Se hace y asigana la ruta de almacenamiento
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("Ruta de almacenamiento","Path: "+path);
                                }
                            });
//Se instancia el objeto Bitmap con la direccion que es path
                    Bitmap bitmap= BitmapFactory.decodeFile(path);
                    // Se asigna la imagen bitmap a nuestro ImageView imagenFoto
                    imagenFoto.setImageBitmap(bitmap);

                    break;
            }


        }
    }
}