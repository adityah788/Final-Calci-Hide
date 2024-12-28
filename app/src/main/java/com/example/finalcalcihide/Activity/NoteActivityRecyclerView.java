package com.example.finalcalcihide.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalcalcihide.Adapter.NotesAdapter;
import com.example.finalcalcihide.Database.NoteDao;
import com.example.finalcalcihide.Database.NotesDatabase;
import com.example.finalcalcihide.Model.Note;
import com.example.finalcalcihide.R;

import java.util.List;
import java.util.concurrent.Executors;

public class NoteActivityRecyclerView extends AppCompatActivity {

    private RecyclerView noteRecyclerView;
    private NotesAdapter notesAdapter;
    private NoteDao noteDao;
    private ImageView deleteicon,backbtn;
    private List<Note> notesList;
    private FrameLayout addNoteFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));


        noteRecyclerView = findViewById(R.id.note_selfie_gallery_recycler);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deleteicon = findViewById(R.id.notes_delete);
        backbtn =findViewById(R.id.notes_main_toolbar_back_arrow);

        addNoteFab = findViewById(R.id.note_recycler_gallary_fab_container);
        addNoteFab.setOnClickListener(v -> startActivity(new Intent(this, NotesActivity.class)));

        // Get the NoteDao from the database instance
        noteDao = NotesDatabase.getInstance(this).noteDao();

        // Load notes initially
        loadNotes();

        // Delete icon click listener
        deleteicon.setOnClickListener(v -> deleteSelectedNotes());

        backbtn.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the RecyclerView when the activity is resumed
        loadNotes();
    }

    private void loadNotes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            notesList = noteDao.getAllNotes();

            runOnUiThread(() -> {
                if (notesAdapter == null) {
                    // Initialize the adapter if it's not already initialized
                    notesAdapter = new NotesAdapter(notesList, new NotesAdapter.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(int position, Note note) {
                            handleItemClick(position, note);
                        }

                        @Override
                        public void onSelectionChanged(boolean isSelected) {
                            addNoteFab.setVisibility(isSelected ? View.GONE : View.VISIBLE);
                            deleteicon.setVisibility(isSelected ? View.VISIBLE : View.GONE);
                        }
                    });

                    noteRecyclerView.setAdapter(notesAdapter);
                } else {
                    // If the adapter is already initialized, just update the data
                    notesAdapter.updateNotes(notesList);
                }
            });
        });
    }

    private void handleItemClick(int position, Note note) {
        if (notesAdapter.isSelectedAny()) {
            notesAdapter.toggleSelection(position);
        } else {
            Intent intent = new Intent(this, NotesActivity.class);
            intent.putExtra("note_id", note.id);
            startActivity(intent);
        }
    }

    private void deleteSelectedNotes() {
        // Get the selected notes
        List<Note> selectedNotes = notesAdapter.getSelectedNotes();

        if (!selectedNotes.isEmpty()) {
            // Delete selected notes from the database in the background
            Executors.newSingleThreadExecutor().execute(() -> {
                for (Note note : selectedNotes) {
                    noteDao.delete(note); // Delete each selected note
                }

                // After deletion, refresh the notes list and update the RecyclerView
                runOnUiThread(() -> {
                    notesList.removeAll(selectedNotes); // Remove selected notes from the list
                    notesAdapter.notifyDataSetChanged(); // Notify adapter that data has changed
                });
            });
        }
    }
}
