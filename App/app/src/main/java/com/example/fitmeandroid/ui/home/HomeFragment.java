package com.example.fitmeandroid.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.fitmeandroid.CalorieActivity;
import com.example.fitmeandroid.R;
import com.example.fitmeandroid.databinding.FragmentHomeBinding;
import com.example.fitmeandroid.ml.Model;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class HomeFragment extends Fragment {


    private FragmentHomeBinding binding;

    private CardView card;
    private TextureView texture;
    private Button cameraBtn;
    private TextView topChoice;
    private TableLayout topChoices;
    private ImageView[] addChoices = {null, null, null, null, null};
    private ImageView add1, add2, add3, add4, add5;
    private TextView choice1, choice2, choice3, choice4, choice5;
    private List<String> top5;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int STANDARD_INPUT_DIMENSIONS = 128;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimensions;
    private ImageReader imageReader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Handler refresh;

    private String[] classes;

    private ProgressBar progressBar;
    private ProgressBar cyclicProgressBar;
    private int progressStatus = 0;
    private Handler progressHandler = new Handler(Looper.getMainLooper());


    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        texture = root.findViewById(R.id.textureView);
        cameraBtn = root.findViewById(R.id.cameraBtn);
        topChoice = root.findViewById(R.id.top_choice);
        card = root.findViewById(R.id.cardView);
        card.setVisibility(View.GONE);
        topChoices = root.findViewById(R.id.top_choices);
        choice1 = root.findViewById(R.id.choice_1);
        choice2 = root.findViewById(R.id.choice_2);
        choice3 = root.findViewById(R.id.choice_3);
        choice4 = root.findViewById(R.id.choice_4);
        choice5 = root.findViewById(R.id.choice_5);

        add1 = root.findViewById(R.id.add_1);
        add2 = root.findViewById(R.id.add_2);
        add3 = root.findViewById(R.id.add_3);
        add4 = root.findViewById(R.id.add_4);
        add5 = root.findViewById(R.id.add_5);
        assert texture != null;

        texture.setSurfaceTextureListener(textureListener);
        cameraBtn.setOnClickListener(view -> takePicture());

        classes = getResources().getStringArray(R.array.model_classes);

        progressBar = root.findViewById(R.id.progressBar);
        cyclicProgressBar = root.findViewById(R.id.progressBar_cyclic);
        addChoices[0] = add1;
        addChoices[1] = add2;
        addChoices[2] = add3;
        addChoices[3] = add4;
        addChoices[4] = add5;
        for (int i=0; i<2; i++){ //TODO: replace 2 with addChoices.length
            ImageView choice = addChoices[i];
            int finalI = i;
            choice.setOnClickListener(view -> changeActivity(finalI));
        }
        topChoices.setVisibility(View.GONE);
        return root;
    }

    private void changeActivity(Integer i) {
        String toSend = top5.get(i);
        Intent intent = new Intent();
        intent.setClass(getActivity(), CalorieActivity.class);
        intent.putExtra("foodChoice", toSend);
        getActivity().startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void takePicture() {
        if(cameraDevice == null){
            return;
        }
        CameraManager manager = (CameraManager)getActivity().getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (cameraDevice != null){
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;

            if(jpegSizes != null && jpegSizes.length > 0){
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(texture.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener(){
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try{
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        byte[] clonedBytes = bytes.clone();
                        mBackgroundHandler = new Handler(Looper.getMainLooper());
                        mBackgroundHandler.post(() -> {
                            try {
                                predict(clonedBytes);
//                                removeCardVisibility();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                    } finally{
                        if(image != null){
                            image.close();
                        }
                    }
                }
                private void predict(byte[] bytes) throws InterruptedException {
                    if (bytes == null) {
                        return;
                    }
                    Bitmap img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (img == null) {
                        Toast.makeText(getActivity(), "Null image!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    img = Bitmap.createScaledBitmap(img, STANDARD_INPUT_DIMENSIONS, STANDARD_INPUT_DIMENSIONS, true);
                    try {
                        Model model = Model.newInstance(getActivity());
                        // Creates inputs for reference.
                        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, STANDARD_INPUT_DIMENSIONS, STANDARD_INPUT_DIMENSIONS, 3}, DataType.FLOAT32);
                        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                        tensorImage.load(img);
                        ByteBuffer byteBuffer = tensorImage.getBuffer();
                        inputFeature0.loadBuffer(byteBuffer);
                        // Runs model inference and gets result.
                        Model.Outputs outputs = model.process(inputFeature0);
                        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                        // Releases model resources if no longer used.
                        model.close();
                        card.setVisibility(View.VISIBLE); // show the card where the result(s) are to be displayed.
                        updateCard(outputFeature0);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback(){
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NotNull CaptureRequest request, @NotNull TotalCaptureResult result){
                    super.onCaptureCompleted(session, request, result);
                    //Toast.makeText(MainActivity.this, "Captured!", Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        cameraCaptureSession.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);

        }catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void updateCard(TensorBuffer outputFeature0) {
        String cls = "";
        double score = 0.0;
        Map<String, Double> cls2score = new HashMap<>();
        for (int i=0; i<outputFeature0.getFloatArray().length; i++){
            cls = classes[i];
            score = outputFeature0.getFloatArray()[i];
            cls2score.put(cls, score);
        }
        Map<String, Double> sorted_cls2score;
        sorted_cls2score = sortByValue(cls2score);
        top5 = getTopFiveChoices(sorted_cls2score);
        Map.Entry<String, Double> entry = sorted_cls2score.entrySet().iterator().next();
        String className = entry.getKey();
        Double classScore = entry.getValue() * 100;

        new Thread(() -> {
            while (classScore >= progressStatus) {
                progressStatus += 1;
                progressHandler.post(() -> {
                    progressBar.setProgress(progressStatus);
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressHandler.post (() -> {
                    if (progressStatus == classScore) {
                        cyclicProgressBar.setVisibility(View.INVISIBLE);
                        topChoice.setText(className);
                        progressHandler.postDelayed(() -> topChoices.setVisibility(View.VISIBLE), 1000);
                        choice1.setText(top5.get(0));
                        choice2.setText(top5.get(1));
//                        choice3.setText(top5.get(2));
//                        choice4.setText(top5.get(3));
//                        choice5.setText(top5.get(4));
                    }
                });

            }
            }).start();

    }

    private Map<String, Double> sortByValue(Map<String, Double> cls2score) {
        List<Map.Entry<String, Double>> lst = new LinkedList<>(cls2score.entrySet());
        lst.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry:lst){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    private List<String> getTopFiveChoices(Map<String, Double> sorted_cls2score){
        List<String> keys = sorted_cls2score.entrySet().stream()
                .map(Map.Entry::getKey)
                .sorted()
                .limit(2) // TODO: change for top five classes
                .collect(Collectors.toList());
        return keys;
    }


    private void createCameraPreview() {
        try{
            SurfaceTexture tex = texture.getSurfaceTexture();
            assert tex != null;
            tex.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());
            Surface surface = new Surface(tex);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null){
                        return;
                    }
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getActivity(), "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void removeCardVisibility() throws InterruptedException {
        refresh = new Handler(Looper.getMainLooper());
        refresh.postDelayed(() -> {
            topChoice.setText("");
//            statsView.setText("");
            card.setVisibility(View.GONE);
        }, 2000);
    }

    private void updatePreview() {
        if(cameraDevice == null) {
            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        }catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {

        CameraManager manager = (CameraManager)getActivity().getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageDimensions = map.getOutputSizes(SurfaceTexture.class)[0];
            if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.CAMERA
                }, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getActivity(), "Camera Permission Required", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (texture.isAvailable()) {
            openCamera();
        } else {
            texture.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

}