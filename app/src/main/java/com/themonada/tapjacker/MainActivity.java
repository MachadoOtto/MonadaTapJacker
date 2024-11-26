package com.themonada.tapjacker;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.graphics.Typeface;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SYSTEM_ALERT_WINDOW = 123;
    private boolean isOverlayRunning = false;
    private Button startOverlayButton;
    private EditText editText;
    private SeekBar opacitySeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView titleText = findViewById(R.id.titleText);
        TextView subtitleText = findViewById(R.id.subtitleText);
        TextView textOpacity = findViewById(R.id.textOpacity);
        editText = findViewById(R.id.editText);

        Typeface pixelFont = Typeface.createFromAsset(getAssets(), "PressStart2P-Regular.ttf");

        titleText.setTypeface(pixelFont);
        subtitleText.setTypeface(pixelFont);

        startOverlayButton = findViewById(R.id.startOverlayButton);
        opacitySeekBar = findViewById(R.id.opacitySeekBar);

        // Solicitar permiso SYSTEM_ALERT_WINDOW si es necesario
        requestSystemAlertWindowPermission();

        // Configurar el listener para el cambio de texto del EditText
        editText.setOnKeyListener((v, keyCode, event) -> {
            // Actualizar el texto del overlay cuando el usuario cambie el texto en el EditText
            if (isOverlayRunning) {
                updateOverlayText(editText.getText().toString());
            }
            return false;
        });

        // Configurar el listener para el cambio de posición del SeekBar
        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Actualizar la opacidad del overlay cuando el usuario cambie la posición del SeekBar
                if (isOverlayRunning) {
                    updateOverlayOpacity(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void requestSystemAlertWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Si el dispositivo ejecuta Android 6.0 o superior y el permiso SYSTEM_ALERT_WINDOW no está concedido
            // Solicitar permiso
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW);
        } else {
            // Permiso ya concedido o no es necesario en versiones anteriores a Android 6.0
            startOverlayButton.setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SYSTEM_ALERT_WINDOW) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // Permiso concedido
                startOverlayButton.setEnabled(true);
            } else {
                // Permiso no concedido, manejar según sea necesario
                startOverlayButton.setEnabled(false);
            }
        }
    }

    public void startOverlay(View view) {
        if (!isOverlayRunning) {
            startService(new Intent(this, OverlayService.class));
            isOverlayRunning = true;
            startOverlayButton.setText(getString(R.string.kill_overlay));
            // Actualizar el texto del overlay y la opacidad inicial
            updateOverlayText(editText.getText().toString());
            updateOverlayOpacity(opacitySeekBar.getProgress());
        } else {
            stopService(new Intent(this, OverlayService.class));
            isOverlayRunning = false;
            startOverlayButton.setText(getString(R.string.start_overlay));
        }
    }

    private void updateOverlayText(String text) {
        Intent intent = new Intent(this, OverlayService.class);
        intent.setAction(OverlayService.ACTION_UPDATE_TEXT);
        intent.putExtra(OverlayService.EXTRA_TEXT, text);
        startService(intent);
    }

    private void updateOverlayOpacity(int opacity) {
        Intent intent = new Intent(this, OverlayService.class);
        intent.setAction(OverlayService.ACTION_UPDATE_OPACITY);
        intent.putExtra(OverlayService.EXTRA_OPACITY, opacity);
        startService(intent);
    }
}
