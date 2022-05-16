package com.example.noteapp;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteHolder> {
    private OnItemClickListener listener;


    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options) {
        super(options);
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        ImageView icLock, previewImg;
        ImageView icPin;
        TextView title, content;
        CardView noteLayout;
        Context context;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            content = itemView.findViewById(R.id.note_content);
            icPin = itemView.findViewById(R.id.note_ic_pin);
            icLock = itemView.findViewById(R.id.note_ic_lock);
            context = itemView.getContext();
            previewImg = itemView.findViewById(R.id.previewImg);

            noteLayout = itemView.findViewById(R.id.noteLayout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteHolder holder, int position, @NonNull Note model) {
        holder.title.setText(model.getTitle());
        holder.content.setText(model.getContent());

        if(model.getImgUri() !=  null){
            holder.previewImg.setVisibility(View.VISIBLE);
            Picasso.get().load(model.getImgUri()).into(holder.previewImg);
        }else{
            holder.previewImg.setVisibility(View.GONE);
        }
    }
    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_grid, parent, false);

        return new NoteHolder(view);
    }

    public interface  OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

}

