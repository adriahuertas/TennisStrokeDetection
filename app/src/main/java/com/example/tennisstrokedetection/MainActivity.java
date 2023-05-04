package com.example.tennisstrokedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Accedim als elements de la pantalla
    private TextView textView;
    private Button button;
    private Boolean enregistrarDades;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mGyroscope;
    private FileOutputStream writerAcc;
    private FileOutputStream writerGyr;
    private String fileNameAcc, fileNameGyr, screenAcc, screenGyr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialitzem variables
        enregistrarDades = false;
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        fileNameAcc = "";
        fileNameGyr = "";
        screenAcc = "Acceleròmetre\nX: \nY: \nZ: \n";
        screenGyr = "Giroscopi\nX: \nY: \nZ:";
        button = findViewById(R.id.button);
        textView = findViewById(R.id.TextView);
        actualitzarPantalla();
        if (mGyroscope == null) textView.setText("NO HI HA GIROSCOPI");
    }

    public void onClick(View view) {
        enregistrarDades = !enregistrarDades;
        if (enregistrarDades) {
            // Canviem el text del botó
            button.setText("Parar enregistrament");

            // Obtenim el temps actual per crear el nom del fitxer
            Instant instant = Instant.now();
            fileNameAcc = instant.toString() + "_ACC.csv";
            fileNameGyr = instant.toString() + "_GYR.csv";

            // Obrim els fitxers
            writerAcc = openFile(fileNameAcc);
            writerGyr = openFile(fileNameGyr);

            // Afegim la capçalera al fitxer als dos fitxers
            String headAcc = "ACC_X,ACC_Y,ACC_Z,TIMESTAMP\n";
            writeToFile(writerAcc, headAcc);

            String headGyr = "GYR_X,GYR_Y,GIR_Z,TIMESTAMP\n";
            writeToFile(writerGyr, headGyr);

            // Comencem a enregistrar dades
            // Activem els sensors
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);


            // Mostrem missatge de feedback
            Toast.makeText(getApplicationContext(), "Enregistrament de dades activat!", Toast.LENGTH_SHORT).show();
        }
        else {
            // Canviem el text del botó
            button.setText("Enregistrar dades");

            // Desactivem lectura de sensors
            mSensorManager.unregisterListener(this);

            // Mostrem missatge de feedback
            Toast.makeText(getApplicationContext(), "Enregistrament finalitzat. Creat el fitxer: "+ fileNameAcc, Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "Enregistrament finalitzat. Creat el fitxer: "+ fileNameGyr, Toast.LENGTH_SHORT).show();

            // Tanquem fitxers
            closeFile(writerAcc);
            closeFile(writerGyr);

            // Reiniciem el text de la pantalla
            textView.setText("Acceleròmetre\nX: \nY: \nZ:" + "\n\n\nGiroscopi\nX: \nY:\nZ:");
        }
    }

    /*
     * actualitzarPantalla
     * Actualitza la pantalla amb les dades de l'accelerometre i el giroscopi
     */
    public void actualitzarPantalla() {
        textView.setText(screenAcc + "\n\n" + screenGyr);
    }

    /*
     * openFile
     * Obre el fitxer y retorna el writer
     */
    public FileOutputStream openFile(String fileName) {
        FileOutputStream writer = null;
        try {
            // Per defecte guardem els fitxers a la carpeta de l'aplicacio
            writer = new FileOutputStream(new File(getApplicationContext().getExternalFilesDir("") , fileName), true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return writer;
    }

    /*
     * closeFile
     * Tanca el fitxer
     */
    public void closeFile(FileOutputStream writer) {
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * writeToFile
     * Escriu al fitxer
     */
    public void writeToFile(FileOutputStream writer, String content) {
        try {
            writer.write(content.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * onSensorChanged
     * S'encarrega d'obtenir les dades del sensor i d'escriure-les al fitxer
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Declaracio de variables locals
        String xAcc="", yAcc="", zAcc="", fileContentAcc="";
        String xGyr="", yGyr="", zGyr="", fileContentGyr="";

        // Obtenim el tipus de sensor
        int sensorType = event.sensor.getType();

        if (sensorType == Sensor.TYPE_LINEAR_ACCELERATION) {
            // Obtenim els valors dels tres eixos
            xAcc = String.valueOf(event.values[0]);
            yAcc = String.valueOf(event.values[1]);
            zAcc = String.valueOf(event.values[2]);

            // Obtenim timestamp
            String timestamp = Instant.now().toString();

            // Construim el contingut per escriure al fitxer
            fileContentAcc = xAcc + "," + yAcc + "," + zAcc + "," + timestamp + "\n";

            // Escrivim al fitxer
            writeToFile(writerAcc, fileContentAcc);

            // Construim el contingut per mostrar per pantalla
            screenAcc = "Acceleròmetre\nX: " + xAcc + "\nY: " + yAcc + "\nZ: " + zAcc + "\n";


        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {
            // Obtenim els tres valors del giroscopi
            xGyr = String.valueOf(event.values[0]);
            yGyr = String.valueOf(event.values[1]);
            zGyr = String.valueOf(event.values[2]);

            // Obtenim timestamp
            String timestamp = Instant.now().toString();

            // Construim el contingut per escriure al fitxer
            fileContentGyr = xGyr + "," + yGyr + "," + zGyr + "," + timestamp + "\n";

            // Escrivim al fitxer
            writeToFile(writerGyr, fileContentGyr);

            // Construim el contingut per mostrar per pantalla
            screenGyr = "Giroscopi\nX: " + xGyr + "\nY: " + yGyr + "\nZ: " + zGyr + "\n";
        }

        // Actualitzem el contingut de la pantalla
        actualitzarPantalla();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}