package com.angler.retro_fileupload.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

   private Retrofit retrofit = null;

   /**
    * This method creates a new instance of the API interface.
    *
    * @return The API interface
    */
   public TIRotogroAPI getAPI() {

      if( retrofit == null ) {
         retrofit = new Retrofit
                 .Builder()
                 .baseUrl( "http://192.168.0.13/file_upload/" )
                // .baseUrl( BuildConfig.Base_url )
                 .addConverterFactory( GsonConverterFactory.create() )
                 .build();
      }

      return retrofit.create( TIRotogroAPI.class );
   }
}