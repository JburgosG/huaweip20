package com.testacuant.p20;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.acuant.acuantcamera.camera.AcuantCameraActivity;
import com.acuant.acuantcamera.constant.Constants;
import com.acuant.acuantcommon.initializer.AcuantInitializer;
import com.acuant.acuantcommon.initializer.IAcuantPackage;
import com.acuant.acuantcommon.initializer.IAcuantPackageCallback;
import com.acuant.acuantcommon.model.Error;
import com.acuant.acuantcommon.model.ErrorCodes;
import com.acuant.acuantcommon.model.Image;
import com.acuant.acuantimagepreparation.AcuantImagePreparation;
import com.acuant.acuantimagepreparation.initializer.ImageProcessorInitializer;
import com.acuant.acuantimagepreparation.model.CroppingData;
import com.testacuant.p20.utils.AbstractOption;
import com.testacuant.p20.utils.DataContainer;
import com.testacuant.p20.utils.ImageUtil;
import com.testacuant.p20.utils.KYCManager;
import com.testacuant.p20.utils.PermissionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static TextView mess;
    static TextView title;
    static Button buttonTry;
    static Dialog epicDialog;

    private static boolean mFrontDocument = true;
    private static final int REQUEST_ID_DOC_SCAN = 1;
    private static AbstractOption.DocumentType mDocumentType = AbstractOption.DocumentType.IDCARD;

    private static final int GLARE_THRESHOLD = 50;
    private static final int SHARPNESS_THRESHOLD = 50;
    private static final int MANDATORY_RESOLUTION_THRESHOLD_SMALL = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        findViewById(R.id.btnTakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndInit();
            }
        });
    }

    private void checkPermissionsAndInit() {

        if (!checkMandatoryPermissions(false)) {
            return;
        }

        // Init all required SDK's
        KYCManager.getInstance().initialise(this);

        final List<IAcuantPackage> list = new ArrayList<>();
        list.add(new ImageProcessorInitializer());
        try {
            AcuantInitializer.initialize("acuant.config.xml", this.getApplicationContext(), list, new IAcuantPackageCallback() {
                @Override
                public void onInitializeFailed(List<? extends Error> list) {

                }

                @Override
                public void onInitializeSuccess() {
                    scanFrontSide(mDocumentType);
                }
            });
        } catch (final Exception e) {

        }
    }

    /**
     * Checks the required runtime permissions.
     *
     * @param askForThem {@code True} if dialog application should request missing permissions, else {@code false}.
     * @return {@code True} if all permissions are present, else {@code false}.
     */
    private boolean checkMandatoryPermissions(final boolean askForThem) {
        return PermissionManager.checkPermissions(this,
                askForThem,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET);
    }

    /**
     * Begins the scanning of the front side of the document.
     *
     * @param docType Document type to scan.
     */
    private void scanFrontSide(final AbstractOption.DocumentType docType) {
        mDocumentType = docType;
        mFrontDocument = true;
        openDocScanActivity();
    }

    /**
     * Opens the activity for document scanning.
     * Document type. AbstractOption.DocumentType.IDCARD
     */
    private void openDocScanActivity() {
        final Intent cameraIntent = new Intent(this, AcuantCameraActivity.class);
        cameraIntent.putExtra(Constants.ACUANT_EXTRA_IS_AUTO_CAPTURE, true);
        cameraIntent.putExtra(Constants.ACUANT_EXTRA_BORDER_ENABLED, true);
        this.startActivityForResult(cameraIntent, REQUEST_ID_DOC_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ID_DOC_SCAN) {
            onActivityResultDocument(resultCode, data);
        }
    }

    /**
     * Handles the onActivityResult method in case of scanning of document.
     *
     * @param resultCode Result code from document scanning.
     * @param data       Data received from document scanning.
     */
    public void onActivityResultDocument(final int resultCode, final Intent data) {
        if (resultCode == AcuantCameraActivity.RESULT_SUCCESS_CODE) {
            final String fileUrl = data.getStringExtra(Constants.ACUANT_EXTRA_IMAGE_URL);
            byte[] imageBytes = new byte[0];

            try {
                imageBytes = ImageUtil.readFile(fileUrl);
            } catch (IOException e) {
                // Do nothing
            }

            final CroppingData image = ImageUtil.cropImage(imageBytes);
            final Image croppedImage = AcuantImagePreparation.INSTANCE.crop(image);

            if (croppedImage.image == null || (croppedImage.error != null && croppedImage.error.errorCode == ErrorCodes.ERROR_LowResolutionImage)) {
                String mensajeError = "";

                int height = image.image.getHeight();
                int width = image.image.getWidth();

                mensajeError = "Error " + croppedImage.error.errorCode + ": " + croppedImage.error.errorDescription;
                mensajeError += "\n\n\n Data size image:" + "\n\n Density: " + croppedImage.dpi;
                mensajeError += "\n Width: " + width + "\n Height: " + height;

                tryAgainWithMessage(mensajeError);
            } else {
                imageValidations(croppedImage);
            }

        } else {
            Log.i("Error", "resultCode :" + resultCode);
        }
    }

    private void imageValidations(Image croppedImage) {

        final Integer sharpness = AcuantImagePreparation.INSTANCE.sharpness(croppedImage.image);
        final Integer glare = AcuantImagePreparation.INSTANCE.glare(croppedImage.image);

        String message = "";

        if (sharpness < SHARPNESS_THRESHOLD || glare < GLARE_THRESHOLD ||
                croppedImage.dpi < MANDATORY_RESOLUTION_THRESHOLD_SMALL) {
            if (sharpness < SHARPNESS_THRESHOLD && glare < GLARE_THRESHOLD) {
                message += MainActivity.this.getString(R.string.error_glare_share);
            } else if (sharpness < SHARPNESS_THRESHOLD) {
                message += MainActivity.this.getString(R.string.error_sharpness);
            } else if (glare < GLARE_THRESHOLD) {
                message += MainActivity.this.getString(R.string.error_glare);
            } else if (croppedImage.dpi < MANDATORY_RESOLUTION_THRESHOLD_SMALL) {
                message += MainActivity.this.getString(R.string.error_dpi);
            }
            tryAgainWithMessage(message);
        } else {
            saveImage(croppedImage);
        }
    }

    private void saveImage(Image croppedImage) {
        Bitmap croppedBmp = croppedImage.image;

        if (croppedBmp.getWidth() > KYCManager.getInstance().getMaxImageWidth()) {
            croppedBmp = ImageUtil.resize(croppedBmp, KYCManager.getInstance().getMaxImageWidth());
        }
        final byte[] imageBytesResized = ImageUtil.bitmapToBytes(croppedBmp);
        if (mFrontDocument) {
            DataContainer.instance().setmDocFront(imageBytesResized);
            if (mDocumentType == AbstractOption.DocumentType.IDCARD) {
                System.out.println("DPI");
                System.out.println(croppedBmp.getDensity());
                scanBackSide(croppedBmp.getWidth(), croppedBmp.getHeight(), croppedBmp.getDensity(), true);
            }
        } else {
            scanBackSide(croppedBmp.getWidth(), croppedBmp.getHeight(), croppedBmp.getDensity(), false);
            DataContainer.instance().setmDocBack(imageBytesResized);
        }
    }

    /**
     * @param message
     */
    private void tryAgainWithMessage(final String message) {
        epicDialog = new Dialog(MainActivity.this);
        epicDialog.setContentView(R.layout.custom_alert);
        epicDialog.setCancelable(false);
        epicDialog.setCanceledOnTouchOutside(false);
        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        buttonTry = epicDialog.findViewById(R.id.button1);
        mess = epicDialog.findViewById(R.id.tv_message);
        mess.setText(message);
        buttonTry.setText(R.string.btn_try_again);

        buttonTry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();
                openDocScanActivity();
            }
        });

        epicDialog.show();
    }

    /**
     * Begins the scanning of the back side of the document.
     */
    private void scanBackSide(int width, int height, int density, boolean takeBack) {
        epicDialog = new Dialog(MainActivity.this);
        epicDialog.setContentView(R.layout.custom_alert);
        epicDialog.setCancelable(false);
        epicDialog.setCanceledOnTouchOutside(false);
        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        buttonTry = epicDialog.findViewById(R.id.button1);
        mess = epicDialog.findViewById(R.id.tv_message);
        title = epicDialog.findViewById(R.id.tv_title);

        if (takeBack) {
            mess.setText("Data size image front: " + "\n\n Density: " + density + "\n Width: " + width + "\n Height: " + height);
            buttonTry.setText(R.string.message_scan_back);

            buttonTry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    epicDialog.dismiss();
                    mFrontDocument = false;
                    openDocScanActivity();
                }
            });
        } else {
            mess.setText("Data size image back: " + "\n\n Density: " + density + "\n Width: " + width + "\n Height: " + height);
            buttonTry.setText("Finish");

            buttonTry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    epicDialog.dismiss();
                }
            });
        }

        epicDialog.show();
    }

    /* --------------------------------------------------------------- */

    void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_LOGS}, 1);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_LOGS}, 2);
            }
            if (checkSelfPermission(Manifest.permission.READ_LOGS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_LOGS}, 3);
            }
        }
    }
}