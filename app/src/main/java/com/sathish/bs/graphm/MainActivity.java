package com.sathish.bs.graphm;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mindorks.paracamera.Camera;
import com.sathish.bs.graphm.processor.ImageProcessor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;

    BaseLoaderCallback baseLoaderCallback;

    ImageProcessor imageProcessor;

    Camera camera;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.cameraViewer);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);*/
        camera = new Camera.Builder()
                .resetToCorrectOrientation(true)
                .setTakePhotoRequestCode(1)
                .setDirectory("pics")
                .setName("ali_" + System.currentTimeMillis())
                .setImageFormat(Camera.IMAGE_JPEG)
                .setCompression(75)
                .setImageHeight(1000)
                .build(MainActivity.this);

        imageView = (ImageView) findViewById(R.id.image);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        Log.v("GraphM", "OpenCV load success");
                        imageProcessor = ImageProcessor.getImageProcessor(MainActivity.this);
                        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    camera.takePicture();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/in.jpg");
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90);
                                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                                processBitmap(rotatedBitmap);
                            }
                        });
//                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Camera.REQUEST_TAKE_PHOTO) {
            Bitmap bitmap = camera.getCameraBitmap();
            processBitmap(bitmap);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void processBitmap(final Bitmap bitmap) {
        new ProcessAsyncTask(bitmap).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class ProcessAsyncTask extends AsyncTask<Void, Bitmap, Bitmap> {
        ProgressDialog progressDialog;
        Map<String, String> results;
        Bitmap bitmap;

        private ProcessAsyncTask(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageView.setImageBitmap(bitmap);
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Processing...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Void... bitmaps) {
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            results = imageProcessor.source(mat, this).detect();
            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(mat, bitmap);
            return bitmap;
        }

        @Override
        public void onProgressUpdate(Bitmap... values) {
            imageView.setImageBitmap(values[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            progressDialog.dismiss();
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);

                StringBuilder stringBuilder = new StringBuilder();

                for (Map.Entry<String, String> m : results.entrySet()) {
                    stringBuilder.append(m.getKey()).append("\n");
                }

                new AlertDialog.Builder(MainActivity.this).setTitle("Results")
                        .setMessage(stringBuilder.toString())
                        .setCancelable(false)
                        .setPositiveButton("Close", null)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(), "Picture not taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat dst = inputFrame.rgba();
//        imageProcessor.source(dst, this).detect();
        return dst;
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "OpenCV failed", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }
}