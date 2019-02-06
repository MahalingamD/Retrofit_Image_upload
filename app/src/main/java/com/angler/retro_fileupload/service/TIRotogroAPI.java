package com.angler.retro_fileupload.service;

import com.angler.retro_fileupload.model.ServerResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * This class represents the Countries API, all endpoints can stay here.
 *
 * @author Mahalingam (Github: @jeancsanchez)
 * @date 30/01/19.
 */
public interface TIRotogroAPI {

   @Multipart
   @POST("http://192.168.0.13/file_upload/upload.php")
   Call<ServerResponse> ImageUpload( @Part MultipartBody.Part file, @Part("file") RequestBody name );

   @Multipart
   @POST("http://192.168.0.13/file_upload/upload.php")
   Call<ServerResponse> ImageSingleUpload( @Part MultipartBody.Part file );

   @GET("http://192.168.0.13/file_upload/upload.php")
   Call<ServerResponse> getAppUpdate( @Part MultipartBody.Part file1, @Part MultipartBody.Part file2 );


}
