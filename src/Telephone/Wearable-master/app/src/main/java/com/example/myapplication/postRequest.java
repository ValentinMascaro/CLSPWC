//package com.example.myapplication;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.List;
//import android.util.Log;
//
//public class postRequest {
//    private String Url = null;
//    private List variables = null;
//    private String donnees = null;
//    private String Resultat = "";
//    private String Header = "";
//    private String pairs;
//
//    public postRequest(){
//    }
//    public void sendRequest(String urlPageContact, String donnees) {
//        this.Url = urlPageContact;
//        this.pairs = donnees;
//        this.doIt();
//    }
//    protected Void doIt() {
//        // Création du client http et du header
//        HttpClient httpclient = new DefaultHttpClient();
//        HttpPost httppost = new HttpPost(this.Url);
//        try {
//            // Ajout des données à la requête
//            httppost.setEntity(new UrlEncodedFormEntity(this.donnees));
//            // Excécution de la requête
//            HttpResponse response = httpclient.execute(httppost);
//            // Lecture de la réponse
//            if (response.getEntity().getContentLength() != 0) {
//                StringBuilder sb = new StringBuilder();
//                try {
//                    InputStream is = response.getEntity().getContent();
//                    int codeHeader = response.getStatusLine().getStatusCode();
//                    Log.i("Code Header : ", String.valueOf(codeHeader));
//                    Header = String.valueOf(codeHeader);
//                    BufferedReader reader = new BufferedReader(
//                            new InputStreamReader(is));
//                    String line = null;
//                    while ((line = reader.readLine()) != null) {
//                        sb.append(line);
//                    }
//                    Log.i("Resultat : ", sb.toString());
//                    Resultat = sb.toString();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Log.e("Resultat : ",
//                        "Pas de contenu affiché dans le body de la réponse");
//                Resultat = "Pas de contenu affiché dans le body de la réponse";
//            }
//        } catch (ClientProtocolException e) {
//            Resultat = "Impossible d'effectuer la tâche";
//        } catch (IOException e) {
//            Resultat = "Impossible d'effectuer la tâche";
//        }
//        return null;
//    }
//
//    public String getResultat(){
//        return this.Resultat;
//    }
//
//    public String getHeader(){
//        return this.Header;
//    }
//}
