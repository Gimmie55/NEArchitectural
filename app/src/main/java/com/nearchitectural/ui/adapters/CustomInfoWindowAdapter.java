package com.nearchitectural.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nearchitectural.GlideApp;
import com.nearchitectural.R;

/**author: Kristiyan Doykov
 * since: TODO: Fill in date
 * version: 1.0
 * purpose: Handles the retrieval of information for and rendering of a custom information
 * window on the Map Activity when a marker is tapped
 */
public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final String TAG = "CustomInfoWindowAdapter"; // Tag used for logging status of application

    private final View mWindow; // The window view itself
    private final Context mContext; // The context (activity) in which to display
    private FirebaseFirestore db; // Database reference for retrieving information/image to display
    private ImageView thumbnailImage; // View holding the thumbnail image
    private String thumbnailURL; // The URL hosting the thumbnail image

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    private void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    // Constructor initialises necessary attributes
    public CustomInfoWindowAdapter(Context mContext) {
        this.mContext = mContext;
        db = FirebaseFirestore.getInstance();
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_info_panel, null);
    }

    private void renderWindowText(final Marker marker, final View view) {

        String title = marker.getTitle(); // Get marker title (location name)
        final Context context = view.getContext(); // Initialise view

        // Initialise UI layout references
        TextView textViewTitle = view.findViewById(R.id.title);
        thumbnailImage = view.findViewById(R.id.picture);

        // Get location thumbnail using marker name and display image
        db.collection("locations").whereEqualTo("name", marker.getTitle())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                thumbnailURL = (String) document.getData().get("thumbnail");
                                setThumbnailURL(thumbnailURL);
                                displayImage(task, context, marker);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        // Set window information
        textViewTitle.setText(title);
        String snippet = marker.getSnippet();
        TextView textViewSnippet = view.findViewById(R.id.snippet);

        // Display empty string for snippet if no snippet found
        if (snippet != null && !snippet.equals("")) {
            textViewSnippet.setText(snippet);
        } else {
            textViewSnippet.setText("");
        }

        thumbnailImage.setContentDescription(title);
    }

    private void displayImage(@NonNull Task<QuerySnapshot> task, Context context, final Marker marker) {

        for (QueryDocumentSnapshot document : task.getResult()) {

            thumbnailURL = (String) document.getData().get("thumbnail");

            setThumbnailURL(thumbnailURL);

            GlideApp.with(context)
                    .load(thumbnailURL)
                    .override(300, 300)
                    .error(R.drawable.ic_launcher_background)
                    .signature(new ObjectKey(thumbnailURL.hashCode()))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (marker.isInfoWindowShown()) {
                                marker.showInfoWindow();
                            }
                            return false;
                        }
                    })
                    .into(thumbnailImage);
        }

    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }
}