package com.angler.retro_fileupload;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.angler.retro_fileupload.model.ServerResponse;
import com.angler.retro_fileupload.service.RetrofitInstance;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import permission.auron.com.marshmallowpermissionhelper.ActivityManagePermission;
import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends ActivityManagePermission implements PermissionResult {

   Button mButton, mUploadImage, mCameraBUT;
   ImageView mImageView;
   AppCompatActivity myContext;

   String mPicturePath;
   private static int RESULT_LOAD_IMAGE = 1;
   static final int REQUEST_IMAGE_CAPTURE = 10;
   private RetrofitInstance myRetrofitInstance;

   private Boolean upflag = false;
   private Uri selectedImage = null;
   private Bitmap bitmap, bitmapRotate;
   String mCurrentPhotoPath = "";
   String fname;
   File file;

   @Override
   protected void onCreate( Bundle savedInstanceState ) {
      super.onCreate( savedInstanceState );
      setContentView( R.layout.activity_main );

      StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
      StrictMode.setVmPolicy( builder.build() );
      builder.detectFileUriExposure();

      myContext = this;

      if( this.myRetrofitInstance == null ) {
         myRetrofitInstance = new RetrofitInstance();
      }
      init();
   }

   private void init() {
      mButton = findViewById( R.id.button );
      mUploadImage = findViewById( R.id.upload_image );
      mCameraBUT = findViewById( R.id.camera_button );
      mImageView = findViewById( R.id.imageView );
      askPermission();

      mButton.setOnClickListener( new View.OnClickListener() {
         @Override
         public void onClick( View view ) {
            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );

            startActivityForResult( i, RESULT_LOAD_IMAGE );
         }
      } );

      mUploadImage.setOnClickListener( new View.OnClickListener() {
         @Override
         public void onClick( View view ) {
            //pass it like this
            File file = new File( mPicturePath );
            RequestBody requestFile = RequestBody.create( MediaType.parse( "multipart/form-data" ), file );

// MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData( "file", file.getName(), requestFile );

// add another part within the multipart request
            RequestBody fullName = RequestBody.create( MediaType.parse( "multipart/form-data" ), "Your Name" );

            myRetrofitInstance.getAPI().ImageSingleUpload( body ).enqueue( new Callback<ServerResponse>() {
               @Override
               public void onResponse( Call<ServerResponse> call, Response<ServerResponse> response ) {
                  Log.e( "Log", response.body().getMessage() );

               }

               @Override
               public void onFailure( Call<ServerResponse> call, Throwable t ) {
                  Log.e( "Log", t.getMessage().toString() );
               }
            } );
         }
      } );

      mCameraBUT.setOnClickListener( new View.OnClickListener() {
         @Override
         public void onClick( View view ) {
            Intent cameraIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
            if( cameraIntent.resolveActivity( getPackageManager() ) != null ) {
               // Create the File where the photo should go
               File photoFile = null;
               try {
                  photoFile = createImageFile();
               } catch( IOException ex ) {
                  // Error occurred while creating the File
                  Log.i( "TAG", "IOException" );
               }
               // Continue only if the File was successfully created
               if( photoFile != null ) {
                 // cameraIntent.putExtra( MediaStore.EXTRA_OUTPUT, Uri.fromFile( photoFile ) );
                  cameraIntent.putExtra( MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(MainActivity.this,
                          BuildConfig.APPLICATION_ID + ".provider",
                          photoFile) );
                  startActivityForResult( cameraIntent, REQUEST_IMAGE_CAPTURE );


               }
            }
         }
      } );
   }

   private File createImageFile() throws IOException {
      // Create an image file name
      String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss" ).format( new Date() );
      String imageFileName = "JPEG_" + timeStamp + "_";
      File storageDir = Environment.getExternalStoragePublicDirectory(
              Environment.DIRECTORY_PICTURES );
      File image = File.createTempFile(
              imageFileName,  // prefix
              ".jpg",         // suffix
              storageDir      // directory
      );

      // Save a file: path for use with ACTION_VIEW intents
     // mCurrentPhotoPath = "file:" + image.getAbsolutePath();
      mPicturePath=image.getAbsolutePath();
      return image;
   }

   @Override
   protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
      try {
         switch( requestCode ) {
            case 10:
               if( requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK ) {
                  try {
                     File aFile= new File( (mPicturePath) );
                     bitmap = MediaStore.Images.Media.getBitmap( this.getContentResolver(),  FileProvider.getUriForFile(MainActivity.this,
                             BuildConfig.APPLICATION_ID + ".provider",
                             aFile));
                     mImageView.setImageBitmap( bitmap );
                  } catch( IOException e ) {
                     e.printStackTrace();
                  }
               }
               break;
            default:
               if( requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data ) {
                  Uri selectedImage = data.getData();
                  String[] filePathColumn = { MediaStore.Images.Media.DATA };

                  Cursor cursor = getContentResolver().query( selectedImage,
                          filePathColumn, null, null, null );
                  cursor.moveToFirst();

                  int columnIndex = cursor.getColumnIndex( filePathColumn[ 0 ] );
                  mPicturePath = cursor.getString( columnIndex );
                  cursor.close();

                  //  ImageView imageView = ( ImageView ) findViewById( R.id.imgView );
                  mImageView.setImageBitmap( BitmapFactory.decodeFile( mPicturePath ) );


                  //service.updateProfile( id, fullName, body, other );

               }
               break;
         }
      } catch( Exception e ) {
         e.printStackTrace();
      }

      super.onActivityResult( requestCode, resultCode, data );
   }


   public static Bitmap rotateImage( Bitmap source, float angle ) {
      Bitmap retVal;

      Matrix matrix = new Matrix();
      matrix.postRotate( angle );
      retVal = Bitmap.createBitmap( source, 0, 0, source.getWidth(), source.getHeight(), matrix, true );

      return retVal;
   }

   //    In some mobiles image will get rotate so to correting that this code will help us
   private int getImageOrientation() {
      final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION };
      final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
      Cursor cursor = getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
              imageColumns, null, null, imageOrderBy );

      if( cursor.moveToFirst() ) {
         int orientation = cursor.getInt( cursor.getColumnIndex( MediaStore.Images.ImageColumns.ORIENTATION ) );
         System.out.println( "orientation===" + orientation );
         cursor.close();
         return orientation;
      } else {
         return 0;
      }
   }


   private void askPermission() {
      try {
         askCompactPermissions( new String[]{
                 PermissionUtils.Manifest_READ_PHONE_STATE,
                 PermissionUtils.Manifest_READ_EXTERNAL_STORAGE,
                 PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE,
                 PermissionUtils.Manifest_CAMERA
         }, this );
      } catch( Exception e ) {
         e.printStackTrace();
      }
   }


   @Override
   protected void onResume() {
      super.onResume();
      checkPermissionGranted();
   }

   /**
    * Check for permission in onResume
    * Return boolean
    */
   public boolean checkPermissionGranted() {
      boolean isGranted = false;
      try {
         isGranted = isPermissionsGranted( myContext, new String[]{ PermissionUtils.Manifest_READ_PHONE_STATE,
                 PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_SMS,
                 PermissionUtils.Manifest_RECEIVE_SMS, PermissionUtils.Manifest_CALL_PHONE, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION,
                 PermissionUtils.Manifest_ACCESS_FINE_LOCATION } );
      } catch( Exception e ) {
         e.printStackTrace();
      }
      return isGranted;
   }

   @Override
   public void permissionGranted() {

   }

   @Override
   public void permissionDenied() {

   }

   @Override
   public void permissionForeverDenied() {

   }
}
