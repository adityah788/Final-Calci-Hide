package com.example.finalcalcihide.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalcalcihide.Model.Note;
import com.example.finalcalcihide.R;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private final List<Note> notesList;
    private final OnNoteClickListener onNoteClickListener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public NotesAdapter(List<Note> notesList, OnNoteClickListener onNoteClickListener) {
        this.notesList = notesList;
        this.onNoteClickListener = onNoteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = notesList.get(position);
        holder.bind(note, onNoteClickListener);
    }

    @Override
    public int getItemCount() {
        return notesList != null ? notesList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, bodyTextView, dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notes_Title);
            bodyTextView = itemView.findViewById(R.id.notes_body);
            dateTextView = itemView.findViewById(R.id.notes_date);
        }

        // Bind the note data and set the click listener
        public void bind(Note note, OnNoteClickListener onNoteClickListener) {
            titleTextView.setText(note.title);
            bodyTextView.setText(note.content);
            dateTextView.setText(note.date);

            itemView.setOnClickListener(v -> onNoteClickListener.onNoteClick(note));
        }
    }
}
