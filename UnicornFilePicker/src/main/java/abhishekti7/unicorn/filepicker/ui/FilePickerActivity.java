package abhishekti7.unicorn.filepicker.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import abhishekti7.unicorn.filepicker.R;
import abhishekti7.unicorn.filepicker.adapters.DirectoryAdapter;
import abhishekti7.unicorn.filepicker.adapters.DirectoryStackAdapter;
import abhishekti7.unicorn.filepicker.databinding.UnicornActivityFilePickerBinding;
import abhishekti7.unicorn.filepicker.models.Config;
import abhishekti7.unicorn.filepicker.models.DirectoryModel;
import abhishekti7.unicorn.filepicker.utils.UnicornSimpleItemDecoration;

public class FilePickerActivity extends AppCompatActivity {

    private static final String TAG = "FilePickerActivity";
    private UnicornActivityFilePickerBinding filePickerBinding;

    private File rootDir;
    private ArrayList<String> selectedFiles;
    private ArrayList<DirectoryModel> arrDirStack;
    private ArrayList<DirectoryModel> arrFiles;

    private DirectoryStackAdapter stackAdapter;
    private DirectoryAdapter directoryAdapter;

    private final String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
    };

    private Config config;
    private ArrayList<String> filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = Config.getInstance();
        setTheme(config.getThemeId());
        filePickerBinding = UnicornActivityFilePickerBinding.inflate(getLayoutInflater());
        View view = filePickerBinding.getRoot();
        setContentView(view);

        initConfig();
    }

    private void initConfig() {
        filters = config.getExtensionFilters();

        setSupportActionBar(filePickerBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (config.getRootDir() != null) {
            rootDir = new File(config.getRootDir());
        } else {
            rootDir = Environment.getExternalStorageDirectory();
        }

        selectedFiles = new ArrayList<>();
        arrDirStack = new ArrayList<>();
        arrFiles = new ArrayList<>();

        setUpDirectoryStackView();
        setUpFilesView();

        if (allPermissionsGranted()) {
            fetchDirectory(new DirectoryModel(
                    true,
                    rootDir.getAbsolutePath(),
                    rootDir.getName(),
                    rootDir.lastModified(),
                    rootDir.listFiles() == null ? 0 : rootDir.listFiles().length
            ));
        } else {
            Log.e(TAG, "Storage permissions not granted. You have to implement it before starting the file picker");
            finish();
        }

        filePickerBinding.fabSelect.setOnClickListener(v -> {
            Intent intent = new Intent();
            if (config.showOnlyDirectory()) {
                selectedFiles.clear();
                selectedFiles.add(arrDirStack.get(arrDirStack.size() - 1).getPath());
            }
            intent.putStringArrayListExtra("filePaths", selectedFiles);
            setResult(config.getReqCode(), intent);
            setResult(RESULT_OK, intent);
            finish();
        });

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.unicorn_fabColor, typedValue, true);
        if (typedValue.data != 0) {
            filePickerBinding.fabSelect.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
        } else {
            filePickerBinding.fabSelect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.unicorn_colorAccent)));
        }
    }

    private void setUpFilesView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(FilePickerActivity.this);
        filePickerBinding.rvFiles.setLayoutManager(layoutManager);
        directoryAdapter = new DirectoryAdapter(FilePickerActivity.this, arrFiles, false, new DirectoryAdapter.onFilesClickListener() {
            @Override
            public void onClicked(DirectoryModel model) {
                fetchDirectory(model);
            }

            @Override
            public void onFileSelected(DirectoryModel fileModel) {
                if (config.isSelectMultiple()) {
                    if (selectedFiles.contains(fileModel.getPath())) {
                        selectedFiles.remove(fileModel.getPath());
                    } else {
                        selectedFiles.add(fileModel.getPath());
                    }
                } else {
                    selectedFiles.clear();
                    selectedFiles.add(fileModel.getPath());
                }
                directoryAdapter.notifyDataSetChanged();
            }
        });
        filePickerBinding.rvFiles.setAdapter(directoryAdapter);
        directoryAdapter.notifyDataSetChanged();
        if (config.addItemDivider()) {
            filePickerBinding.rvFiles.addItemDecoration(new UnicornSimpleItemDecoration(FilePickerActivity.this));
        }
    }

    private void setUpDirectoryStackView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(FilePickerActivity.this, RecyclerView.HORIZONTAL, false);
        filePickerBinding.rvDirPath.setLayoutManager(layoutManager);
        stackAdapter = new DirectoryStackAdapter(FilePickerActivity.this, arrDirStack, model -> {
            arrDirStack = new ArrayList<>(arrDirStack.subList(0, arrDirStack.indexOf(model) + 1));
            setUpDirectoryStackView();
            fetchDirectory(arrDirStack.remove(arrDirStack.size() - 1));
        });

        filePickerBinding.rvDirPath.setAdapter(stackAdapter);
        stackAdapter.notifyDataSetChanged();
    }

    private void fetchDirectory(DirectoryModel model) {
        filePickerBinding.rlProgress.setVisibility(View.VISIBLE);
        selectedFiles.clear();

        arrFiles.clear();
        File dir = new File(model.getPath());
        File[] filesList = dir.listFiles();
        if (filesList != null) {
            for (File file : filesList) {
                DirectoryModel directoryModel = new DirectoryModel();
                directoryModel.setDirectory(file.isDirectory());
                directoryModel.setName(file.getName());
                directoryModel.setPath(file.getAbsolutePath());
                directoryModel.setLast_modif_time(file.lastModified());

                if (config.showHidden() || (!config.showHidden() && !file.isHidden())) {
                    if (file.isDirectory()) {
                        if (file.listFiles() != null)
                            directoryModel.setNum_files(file.listFiles().length);
                        arrFiles.add(directoryModel);
                    } else {
                        if (!config.showOnlyDirectory()) {
                            if (filters != null && !filters.isEmpty()) {
                                try {
                                    String fileName = file.getName();
                                    String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
                                    for (String filter : filters) {
                                        if (extension.toLowerCase().contains(filter)) {
                                            arrFiles.add(directoryModel);
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error filtering file: ", e);
                                }
                            } else {
                                arrFiles.add(directoryModel);
                            }
                        }
                    }
                }
            }
            Collections.sort(arrFiles, new CustomFileComparator());

            arrDirStack.add(model);
            filePickerBinding.rvDirPath.scrollToPosition(arrDirStack.size() - 1);
            filePickerBinding.toolbar.setTitle(model.getName());
        }

        if (arrFiles.isEmpty()) {
            filePickerBinding.rlNoFiles.setVisibility(View.VISIBLE);
        } else {
            filePickerBinding.rlNoFiles.setVisibility(View.GONE);
        }

        filePickerBinding.rlProgress.setVisibility(View.GONE);
        stackAdapter.notifyDataSetChanged();
        directoryAdapter.notifyDataSetChanged();
    }

    public static class CustomFileComparator implements Comparator<DirectoryModel> {
        @Override
        public int compare(DirectoryModel o1, DirectoryModel o2) {
            if (o1.isDirectory() && o2.isDirectory()) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            } else if (o1.isDirectory() && !o2.isDirectory()) {
                return -1;
            } else if (!o1.isDirectory() && o2.isDirectory()) {
                return 1;
            } else {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.unicorn_menu_file_picker, menu);

        MenuItem itemSearch = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) itemSearch.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                directoryAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    private boolean allPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(FilePickerActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (arrDirStack.size() > 1) {
            arrDirStack.remove(arrDirStack.size() - 1);
            DirectoryModel model = arrDirStack.get(arrDirStack.size() - 1);
            fetchDirectory(model);
        } else {
            Intent intent = new Intent();
            setResult(config.getReqCode(), intent);
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
