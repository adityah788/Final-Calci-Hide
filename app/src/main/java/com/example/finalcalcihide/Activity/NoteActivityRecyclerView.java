package com.example.finalcalcihide.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalcalcihide.Adapter.NotesAdapter;
import com.example.finalcalcihide.Database.NoteDao;
import com.example.finalcalcihide.Database.NotesDatabase;
import com.example.finalcalcihide.Model.Note;
import com.example.finalcalcihide.R;

import java.util.List;
import java.util.concurrent.Executors;

public class NoteActivityRecyclerView extends AppCompatActivity implements NotesAdapter.OnNoteClickListener {

    private RecyclerView noteRecyclerView;
    private NotesAdapter notesAdapter;
    private NoteDao noteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);

        noteRecyclerView = findViewById(R.id.note_selfie_gallery_recycler);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FrameLayout addNoteFab = findViewById(R.id.note_recycler_gallary_fab_container);
        addNoteFab.setOnClickListener(v -> startActivity(new Intent(this, NotesActivity.class)));

        noteDao = NotesDatabase.getInstance(this).noteDao();
        loadNotes();
    }

    private void loadNotes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Note> notesList = noteDao.getAllNotes();
            runOnUiThread(() -> {
                notesAdapter = new NotesAdapter(notesList, this);
                noteRecyclerView.setAdapter(notesAdapter);
            });
        });
    }

    @Override
    public void onNoteClick(Note note) {
        // Open NotesActivity and pass the note ID
        Intent intent = new Intent(this, NotesActivity.class);
        intent.putExtra("note_id", note.id);
        startActivity(intent);
    }
}
