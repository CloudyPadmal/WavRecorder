package lk.padmal.audiorecorder;

import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

/**
 * Created by Padmal on 11/2/17.
 */

public class RecordedAudioListAdapter extends RecyclerView.Adapter<RecordedAudioListAdapter.ViewHolder> {

    private List<String> filePaths;
    private List<String> uniqueNames;
    private MediaPlayer mPlayer = null;

    public RecordedAudioListAdapter(List<String> filePaths, List<String> uniqueNames) {
        this.filePaths = filePaths;
        this.uniqueNames = uniqueNames;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.fileName.setText(filePaths.get(position));
        holder.uniqueName.setText(uniqueNames.get(position));
    }

    @Override
    public int getItemCount() {
        return filePaths.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageButton playStopButton;
        public TextView fileName, uniqueName;
        public boolean playing;

        public ViewHolder(View itemView) {
            super(itemView);
            playStopButton = itemView.findViewById(R.id.play_stop_button);
            fileName = itemView.findViewById(R.id.file_name);
            uniqueName = itemView.findViewById(R.id.unique_name);

            playing = true;

            playStopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onPlay(playing);
                }
            });
        }

        private void onPlay(boolean start) {
            if (start) {
                startPlaying();
            } else {
                stopPlaying();
            }
        }

        private void startPlaying() {
            playing = false;
            playStopButton.setImageResource(R.drawable.ic_stop_now);

            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(uniqueName.getText().toString());
                mPlayer.prepare();
                mPlayer.start();
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        playStopButton.setImageResource(R.drawable.ic_play_now);
                        playing = true;
                    }
                });
            } catch (IOException e) {
                Log.e("ABCD", "prepare failed");
            }
        }

        private void stopPlaying() {
            playing = true;
            playStopButton.setImageResource(R.drawable.ic_play_now);
            try {
                mPlayer.release();
                mPlayer = null;
            } catch (NullPointerException e) {
                Log.e("ABCD", "closing failed");
            }
        }
    }
}
