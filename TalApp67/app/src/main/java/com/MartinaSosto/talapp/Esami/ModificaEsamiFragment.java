package com.MartinaSosto.talapp.Esami;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.MartinaSosto.talapp.Allegati.ImageAdapter;
import com.MartinaSosto.talapp.R;
import com.MartinaSosto.talapp.Utils.SnackbarRimuoviEvento;
import com.MartinaSosto.talapp.Utils.Util;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static android.app.Activity.RESULT_OK;
import static com.MartinaSosto.talapp.Allegati.ImageAdapter.DOWNLOAD;
import static com.MartinaSosto.talapp.Allegati.ImageAdapter.UPLOAD;
import static com.MartinaSosto.talapp.Esami.AnalisiListAdapter.KEY_MODIFICA;
import static com.MartinaSosto.talapp.Esami.EsamiFragment.setTipoEsami;
import static com.MartinaSosto.talapp.HomeActivity.actionBar;
import static com.MartinaSosto.talapp.Notification.ForegroundService.esamiRef;
import static com.MartinaSosto.talapp.Utils.Util.ESAMI_LABORATORIO;
import static com.MartinaSosto.talapp.Utils.Util.KEY_ALLEGATI;
import static com.MartinaSosto.talapp.Utils.Util.KEY_ANALISI;
import static com.MartinaSosto.talapp.Utils.Util.KEY_DATA;
import static com.MartinaSosto.talapp.Utils.Util.KEY_ESITO;
import static com.MartinaSosto.talapp.Utils.Util.KEY_NOME;
import static com.MartinaSosto.talapp.Utils.Util.KEY_NOTE;
import static com.MartinaSosto.talapp.Utils.Util.Utente;


public class ModificaEsamiFragment extends Fragment {

    private static Context mContext;
    private static Map<String, Object> esame_old;
    private static String id;
    private static final int ID_IMAGE = 1;
    private static List<String> mUriListEsame;
    private MutableLiveData<List<Uri>> mUriListUpload;
    private static MutableLiveData<List<Uri>> mUriListDownload;
    private static MutableLiveData<List<Map<String, Object>>> mAnalisi;
    private static StorageReference mStorageRef;
    private ProgressBar progressBarAllegato;
    private Button buttonUploadAllegato;
    private View root;


    public static String getIdEsame() {
        return id;
    }
    public static List<String> getListAllegati(){ return mUriListEsame;}
    public static void setListAllegati(List<String> list){
        mUriListEsame = list;
        mUriListDownload.setValue(new ArrayList<>());
        DownloadUri(0, mContext);
    }
    public static void removeFromListDownload(Uri uri){
        List<Uri> list = mUriListDownload.getValue();
        list.remove(uri);
        mUriListDownload.setValue(list);
    }

    public static void addInListDownload(Uri uri){
        List<Uri> list = mUriListDownload.getValue();
        list.add(uri);
        mUriListDownload.setValue(list);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.Indipendence)));
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
               esame_old = null;
               setTipoEsami(ESAMI_LABORATORIO);
               remove();
            }
        };

        mContext = getContext();
        mUriListUpload = new MutableLiveData<>();
        mUriListUpload.setValue(new ArrayList<>());
        mUriListDownload = new MutableLiveData<>();
        mUriListDownload.setValue(new ArrayList<>());
        mUriListEsame = new ArrayList<>();
        mAnalisi = new MutableLiveData<>();
        mAnalisi.setValue(new ArrayList<>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_modifica_esami, container, false);
        this.root = root;
        id = getArguments().getString("ID");
        mStorageRef = FirebaseStorage.getInstance().getReference(Utente+"/"+ id + "/");
        progressBarAllegato = root.findViewById(R.id.progressBarAllegato);

        //RecyclerView degli upload
        recyclerViewUpload();

        //Recyclerview dei vecchi allegati
        recyclerViewDownload();

        //Prende le informazioni dell'esame
        getInfoEsame();

        //RecyclerView allegati
        AnalisiListAdapter analisiListAdapter = new AnalisiListAdapter(getContext(), KEY_MODIFICA);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view_analisi);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(analisiListAdapter);
        mAnalisi.observe(getViewLifecycleOwner(), analisis -> {
            //Log.d("LISTA ESAMI", String.valueOf(analisis));
            analisiListAdapter.setAnalisi(analisis);
        });

        //Bottone aggiungi allegato
        Button buttonAggiungiAllegato = root.findViewById(R.id.buttonAggiungiAllegato);
        ConnectivityManager cm = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE); //Controllo se sono connessa a internet
        if(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()) buttonAggiungiAllegato.setVisibility(View.VISIBLE);
        else buttonAggiungiAllegato.setVisibility(View.GONE);
        buttonAggiungiAllegato.setOnClickListener(v -> aggiungiAllegato());

        //Bottone per fare l'upload dell'allegato
        buttonUploadAllegato = root.findViewById(R.id.buttonUploadAllegato);
        buttonUploadAllegato.setOnClickListener(v -> uploadFile(0));

        //Bottone salva esame
        root.findViewById(R.id.buttonModificaTerapia).setOnClickListener(v -> salvaEsame());

        //Bottone elimina esame
        root.findViewById(R.id.buttonEliminaTerapia).setOnClickListener(v -> eliminaEsame());

        return root;
    }

    private void recyclerViewDownload() {
        RecyclerView recyclerViewDownload = root.findViewById(R.id.recyclerViewAllegati);
        ImageAdapter imageAdapterDownload = new ImageAdapter(getContext(), DOWNLOAD);
        recyclerViewDownload.setAdapter(imageAdapterDownload);
        recyclerViewDownload.setLayoutManager(new GridLayoutManager(getContext(), 1));
        mUriListDownload.observe(getViewLifecycleOwner(), uris -> {
            Log.d("Aggiorno Download", "");
            if(uris.size() > 0 ){
                imageAdapterDownload.setImage(uris);
                recyclerViewDownload.setVisibility(View.VISIBLE);
            }
            else recyclerViewDownload.setVisibility(View.GONE);
        });
    }

    private void recyclerViewUpload() {
        RecyclerView recyclerViewUploads = root.findViewById(R.id.recyclerViewUploads);
        ImageAdapter imageAdapterUpload = new ImageAdapter(getContext(), UPLOAD);
        recyclerViewUploads.setAdapter(imageAdapterUpload);
        recyclerViewUploads.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mUriListUpload.observe(getViewLifecycleOwner(), uris -> {
            Log.d("Aggiorno Upload", "");
            if(uris.size() > 0 ){
                imageAdapterUpload.setImage(uris);
                recyclerViewUploads.setVisibility(View.VISIBLE);
            }
            else recyclerViewUploads.setVisibility(View.GONE);
        });
    }

    private void aggiungiAllegato() {
        if(mUriListUpload.getValue().size() <= 3) {
            //Apre il file chooser e scelgo l'immagine
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, ID_IMAGE);
        }
        else Toast.makeText(getContext(), "Puoi aggiungere al massimo 3 file", Toast.LENGTH_SHORT).show();
    }

    private void eliminaEsame() {
        esamiRef.document(id).delete();
        Navigation.findNavController(root).popBackStack();
        SnackbarRimuoviEvento SU = new SnackbarRimuoviEvento();
        SU.rimuovi(esame_old, esamiRef);
        Snackbar snackbar = Snackbar.make(root, "Esame rimosso", BaseTransientBottomBar.LENGTH_LONG);
        snackbar.setAction("Cancella operazione", SU);
        snackbar.show();
        Navigation.findNavController(root).popBackStack();
    }

    private void salvaEsame() {
        TextInputEditText ETEsito = root.findViewById(R.id.editTextEsito);
        if (!ETEsito.getText().toString().isEmpty()) {
            esame_old.put(KEY_ESITO, Double.parseDouble(ETEsito.getText().toString()));
        }
        else esame_old.put(KEY_ESITO, null);

        TextInputEditText ETNote = root.findViewById(R.id.ETNoteModEsame);
        if (!ETNote.getText().toString().isEmpty()) {
            esame_old.put(KEY_NOTE, ETNote.getText().toString());
        } else {
            esame_old.put(KEY_NOTE, FieldValue.delete());
        }

        esamiRef.document(id).update(esame_old);
        Toast.makeText(getContext(), "Esame aggiornato", Toast.LENGTH_SHORT).show();
        esame_old = null;
        Navigation.findNavController(root).popBackStack();
    }



    private void getInfoEsame() {
        Log.d("getInfoEsame", "");
        //Prendo le informazioni dell'esame
        esamiRef.document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                esame_old = task.getResult().getData();

                //Prendo i vecchi esami
                if(!esame_old.containsKey(KEY_ALLEGATI)){
                    Log.d("getInfoEsame", "Non ci sono vecchi esami");
                    mUriListEsame = new ArrayList<>();
                    esamiRef.document(id).update(KEY_ALLEGATI, mUriListEsame);
                }
                else {
                    Log.d("getInfoEsame", "Prendo i vecchi esami");
                    mUriListEsame = (List<String>) esame_old.get(KEY_ALLEGATI);
                }
                Log.d("getInfoEsame", "Lista allegati: "+ mUriListEsame);
                DownloadUri(0, getContext());

                TextView TXVdata = root.findViewById(R.id.TXVDataEsame);
                TXVdata.setText(esame_old.get(KEY_NOME)+" del " + Util.TimestampToStringData((Timestamp) esame_old.get(KEY_DATA)) + " ore " +  Util.TimestampToOrario((Timestamp) esame_old.get(KEY_DATA)));

                TextInputLayout TILEsito = root.findViewById(R.id.TILEsito);
                TextInputEditText editTextEsito = root.findViewById(R.id.editTextEsito);
                if(esame_old.containsKey(KEY_ANALISI)) {
                    mAnalisi.setValue((List<Map<String, Object>>) esame_old.get(KEY_ANALISI));
                    TILEsito.setVisibility(View.GONE);
                }
                else {
                    TILEsito.setVisibility(View.VISIBLE);
                    if(esame_old.get(KEY_ESITO) != null) editTextEsito.setText(String.valueOf(esame_old.get(KEY_ESITO)));
                }

                EditText ETNote = root.findViewById(R.id.ETNoteModEsame);
                if (esame_old.containsKey(KEY_NOTE)) {
                    ETNote.setText((CharSequence) esame_old.get(KEY_NOTE));
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ID_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){

            add(mUriListUpload, data.getData());

            buttonUploadAllegato.setVisibility(View.VISIBLE);
            progressBarAllegato.setVisibility(View.INVISIBLE);
        }
    }


    //Carica il file
    private void uploadFile(int tot) {
        if(mUriListUpload.getValue().size() > 0) {
            String idAllegato = String.valueOf(System.currentTimeMillis());
            Uri uriFile = mUriListUpload.getValue().get(tot);
            StorageReference fileReference = mStorageRef.child(idAllegato+"."+ Util.getFileExtension(uriFile, getActivity()));
            //Log.d("ADD", mStorageRef.toString());
            //Log.d("ADD", fileReference.toString());
            //Log.d("ADD", String.valueOf(uriFile));
            fileReference.putFile(uriFile).addOnSuccessListener(taskSnapshot -> {
                Handler handler = new Handler();
                handler.postDelayed(() -> progressBarAllegato.setProgress(0), 5000);

                mUriListEsame.add(idAllegato+"."+ Util.getFileExtension(uriFile, getActivity()));

                if(tot+1 < mUriListUpload.getValue().size()){
                    uploadFile(tot+1);
                }
                else {
                    esamiRef.document(id).update(KEY_ALLEGATI, mUriListEsame);
                    mUriListUpload.setValue(new ArrayList<>());

                    progressBarAllegato.setVisibility(View.GONE);
                    buttonUploadAllegato.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Allegato caricato", Toast.LENGTH_SHORT).show();

                    DownloadUri(0, getContext());
                }
            }).addOnProgressListener(snapshot -> {
                progressBarAllegato.setVisibility(View.VISIBLE);
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressBarAllegato.setProgress((int) progress);
            });
        }
    }

    public static void DownloadUri(int tot, Context context){
        if(mUriListEsame.size() > 0) {
            //Log.d("DownloadUri", mUriListEsame.get(tot));
            StorageReference storageReference = mStorageRef.child(mUriListEsame.get(tot));
            //Log.d("DownloadUri", "StorageReference: "+ storageReference);
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                add(mUriListDownload, uri);
                //Log.d("DownloadUri", "mUriListDownload: "+ mUriListDownload.getValue());

                if(tot+1 < mUriListEsame.size()){
                    DownloadUri(tot+1, context);
                }
                else Toast.makeText(context, "Allegati aggiornati", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private static void add(MutableLiveData<List<Uri>> mutableLiveData, Uri uri){
        List<Uri> tmp = mutableLiveData.getValue();
        Objects.requireNonNull(tmp).add(uri);
        mutableLiveData.setValue(tmp);
    }

    //Dato un nome e un double esito, aggiorna l'analisi con il nome dato aggiornando il suo esito con il valore dato
    public static void updateEsito(String nome, Double esito){
        List<Map<String, Object>> list = (List<Map<String, Object>>) esame_old.get(KEY_ANALISI);
        for(int i = 0; i < list.size(); i++){
            String nome1 = (String) list.get(i).get(KEY_NOME);
            if(nome1.compareTo(nome) == 0){
                list.get(i).put(KEY_ESITO, esito);
            }
        }
        //mAnalisi.setValue(list);
    }
}
