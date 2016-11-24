package com.radiostream.networking;

import com.radiostream.networking.models.PlaylistListResult;
import com.radiostream.networking.models.PlaylistResult;
import com.radiostream.networking.models.SongResult;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

@DebugLog
public class MetadataBackend {

    // TOOD: baseUrl, user, pass from injected Settings class
    String baseUrl = "***REMOVED***/5f707e4f-97cc-438e-90d8-1e5e35bd558a/";
    String user = "radio";
    String pass = "myman";

    @Inject
    public MetadataBackend() {

    }

    public Promise<PlaylistListResult, Exception, Void> fetchAllPlaylist() throws IOException {
        final Deferred<PlaylistListResult, Exception, Void> deferred = new DeferredObject<>();

        BackendMetadataClient client = HttpServiceFactory.createService(BackendMetadataClient.class, baseUrl, user, pass);
        Call<PlaylistListResult> playlistsCall = client.allPlaylists();
        playlistsCall.enqueue(new Callback<PlaylistListResult>() {
            @Override
            public void onResponse(Call<PlaylistListResult> call, Response<PlaylistListResult> response) {
                if (response.isSuccessful()) {
                    deferred.resolve(response.body());
                    return;
                } else {
                    deferred.reject(new IOException(String.format(Locale.ENGLISH,
                        "Playlist call failed - Returned status: %d", response.code())));
                }
            }

            @Override
            public void onFailure(Call<PlaylistListResult> call, Throwable t) {
                deferred.reject(new IOException(String.format(Locale.ENGLISH,
                    "Playlist call failed - %s", t.toString())));
            }
        });

        return deferred.promise();
    }

    public Promise<List<SongResult>, Exception, Void> fetchPlaylist(String playlistName) {
        final Deferred<List<SongResult>, Exception, Void> deferred = new DeferredObject<>();

        BackendMetadataClient client = HttpServiceFactory.createService(BackendMetadataClient.class, baseUrl, user, pass);
        Call<PlaylistResult> playlistCall = client.playlist(playlistName);
        playlistCall.enqueue(new Callback<PlaylistResult>() {
            @Override
            public void onResponse(Call<PlaylistResult> call, Response<PlaylistResult> response) {
                if (response.isSuccessful()) {
                    deferred.resolve(response.body().results);
                    return;
                } else {
                    deferred.reject(new IOException(String.format(Locale.ENGLISH,
                        "Playlist call failed - Returned status: %d", response.code())));
                }
            }

            @Override
            public void onFailure(Call<PlaylistResult> call, Throwable t) {
                deferred.reject(new IOException(String.format(Locale.ENGLISH,
                    "Playlist call failed - %s", t.toString())));
            }
        });

        return deferred.promise();
    }

    interface BackendMetadataClient {
        @GET("api/playlists")
        Call<PlaylistListResult> allPlaylists();

        @GET("api/playlists/{playlistName}")
        Call<PlaylistResult> playlist(@Path("playlistName") String playlistName);
    }
}