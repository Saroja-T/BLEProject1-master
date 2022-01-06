package com.psirc.dev.bleproject;

/**
 * Created by dev on 11/06/17.
 */


import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by dev on 31/01/17.
 */

public abstract class ServerConnect {

    private SyncPost syncPost;
    private SyncGet syncGet;
    private SyncPostJson syncPostJson;


    public ServerConnect(RecyclerView.ViewHolder viewHolder){
        syncGet = new SyncGet(viewHolder);
    }
    public ServerConnect(Context context, HashMap<String,String> payloads, RecyclerView.ViewHolder view){
        syncPost = new SyncPost(context,getUrlEncoder(payloads),view);
    }

    public ServerConnect(Context context, JSONObject payloads, RecyclerView.ViewHolder view){
        syncPostJson = new SyncPostJson(context,getJsonEncoding(payloads),view);
    }

    public void execute(String url){
        syncPost.execute(url);
    }
    public void executeGet(String url){
        syncGet.execute(url);
    }
    public void executeJsonPayLoad(String url){
        syncPostJson.execute(url);
    }




    private String getUrlEncoder(HashMap<String,String> payloads){
        Uri.Builder builder = new Uri.Builder();

        for (HashMap.Entry<String,String> temp: payloads.entrySet()){
            Log.w("HM", temp.getKey() + "," + temp.getValue());
            builder.appendQueryParameter(temp.getKey(),temp.getValue());
        }

        return builder.build().getEncodedQuery();

    }

    private String getJsonEncoding(JSONObject jsonObject){
        return String.valueOf(jsonObject);
    }

    public abstract void getResult(String result, RecyclerView.ViewHolder view);
    //public abstract void getResult(String result);


    private class SyncPostJson extends AsyncTask<String,Void,String> {
        private String urlEncodedPayLoads;
        private RecyclerView.ViewHolder view;

        //private ProgressDialog progressDialog;
        public SyncPostJson(Context context, String urlEncodedPayLoads, RecyclerView.ViewHolder mView){
            //   this.progressDialog = ProgressDialog.show(context,"Wait","Connecting Server");
            this.urlEncodedPayLoads = urlEncodedPayLoads;
            this.view = mView;
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder stringBuilder = new StringBuilder();
            //    progressDialog.setMessage("Connected, Please wait");
            try {
                URL url = new URL(params[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(10000);
                httpURLConnection.setReadTimeout(15000);
                //httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();

                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTf-8"));
                bufferedWriter.write(urlEncodedPayLoads);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //    progressDialog.dismiss();
            getResult(s,view);
        }
    }

    private class SyncPost extends AsyncTask<String,Void,String> {
        private String urlEncodedPayLoads;
        private RecyclerView.ViewHolder view;
        //private ProgressDialog progressDialog;
        public SyncPost(Context context, String urlEncodedPayLoads, RecyclerView.ViewHolder view){
            //   this.progressDialog = ProgressDialog.show(context,"Wait","Connecting Server");
            this.urlEncodedPayLoads = urlEncodedPayLoads;
            this.view = view;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder stringBuilder = new StringBuilder();
            //    progressDialog.setMessage("Connected, Please wait");
            try {
                URL url = new URL(params[0]);
                HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(10000);
                httpURLConnection.setReadTimeout(15000);
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();

                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTf-8"));
                bufferedWriter.write(urlEncodedPayLoads);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                //httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return stringBuilder.toString();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //    progressDialog.dismiss();
            getResult(s,view);
        }
    }

    private class SyncGet extends AsyncTask<String,Void,String> {

        private RecyclerView.ViewHolder viewHolder;

        public SyncGet(RecyclerView.ViewHolder viewHolder){
            this.viewHolder = viewHolder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder stringBuilder = new StringBuilder();
            //    progressDialog.setMessage("Connected, Please wait");
            try {
                URL url = new URL(params[0]);
                HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(10000);
                httpURLConnection.setReadTimeout(15000);
                //httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                /*
                OutputStream outputStream = httpURLConnection.getOutputStream();

                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTf-8"));
                bufferedWriter.write(urlEncodedPayLoads);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                */

                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return stringBuilder.toString();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //    progressDialog.dismiss();
            getResult(s,viewHolder);
        }
    }
}
