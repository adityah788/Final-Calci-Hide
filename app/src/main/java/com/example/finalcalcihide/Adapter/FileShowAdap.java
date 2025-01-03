    package com.example.finalcalcihide.Adapter;

    import android.content.Context;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.RelativeLayout;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.core.content.ContextCompat;
    import androidx.recyclerview.widget.RecyclerView;

    import com.bumptech.glide.Glide;
    import com.example.finalcalcihide.R;

    import java.io.File;
    import java.text.DecimalFormat;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Locale;

    // ... [imports]

    public class FileShowAdap extends RecyclerView.Adapter<FileShowAdap.ViewHolder> {

        private final Context context;
        private final ArrayList<String> filePaths;
        private final OnItemSelectedListener listener;
        private final HashSet<Integer> hashSetselectedItems = new HashSet<>();

        public interface OnItemSelectedListener {
            void onItemSelected(int position);
            void onSelectionChanged(boolean isSelected);
        }

        public FileShowAdap(Context context, ArrayList<String> filePaths, OnItemSelectedListener listener) {
            this.context = context;
            this.filePaths = filePaths;
            this.listener = listener;
        }

        @NonNull
        @Override
        public FileShowAdap.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
            return new FileShowAdap.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FileShowAdap.ViewHolder holder, int position) {
//            String filePath = filePaths.get(position);
//            File file = new File(filePath);
//
//            // Set the file details
//            holder.textViewtitle.setText(file.getName());
//            holder.textViewsize.setText(getFileSize(file.length()));
//            holder.textViewtime.setText(getFormattedDate(file.lastModified()));
//
//            // Determine file type
//            boolean isAudio = isAudioFile(file);
//            boolean isDocument = isDocumentFile(file);
//            boolean isImage = isImageFile(file);
//            boolean isVideo = isVideoFile(file);
//
//            // Set image based on file type
//            if (isAudio) {
//                holder.imageViewimage.setImageResource(R.drawable.baseline_audiotrack_24);
//            } else if (isDocument) {
//                holder.imageViewimage.setImageResource(R.drawable.baseline_insert_drive_file_24);
//            } else if (isImage || isVideo) {
//                Glide.with(context).load(file).into(holder.imageViewimage);
//            } else {
//                holder.imageViewimage.setImageResource(R.color.browser_title_color);
//            }
//
//            // Set selection state
//            holder.containertick.setVisibility(View.VISIBLE);
//            holder.imageViewtick.setVisibility(View.GONE);
//            holder.imageViewtickblank.setVisibility(View.GONE);
//            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
//
//            if (hashSetselectedItems.contains(position)) {
//                holder.imageViewtick.setVisibility(View.VISIBLE);
//                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.overlayColor));
//            } else if (!hashSetselectedItems.isEmpty()) {
//                holder.imageViewtickblank.setVisibility(View.VISIBLE);
//            }
//
//            holder.itemView.setOnLongClickListener(v -> {
//                toggleSelection(position);
//                return true;
//            });
//
//            holder.itemView.setOnClickListener(v -> {
//                if (hashSetselectedItems.contains(position)) {
//                    toggleSelection(position);
//                } else {
//                    listener.onItemSelected(position);
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return filePaths.size();
        }

        public void toggleSelection(int position) {
            if (hashSetselectedItems.contains(position)) {
                hashSetselectedItems.remove(position);
            } else {
                hashSetselectedItems.add(position);
            }
            notifyItemChanged(position);
            listener.onSelectionChanged(!hashSetselectedItems.isEmpty());
        }

        public void clearSelection() {
            hashSetselectedItems.clear();
            notifyDataSetChanged();
            listener.onSelectionChanged(false);
        }

        public void selectAll(boolean selectAll) {
            hashSetselectedItems.clear();
            if (selectAll) {
                for (int i = 0; i < getItemCount(); i++) {
                    hashSetselectedItems.add(i);
                }
            }
            notifyDataSetChanged();
            listener.onSelectionChanged(!hashSetselectedItems.isEmpty());
        }

        public int getSelectedItemCount() {
            return hashSetselectedItems.size();
        }

        public boolean isSelectedAny() {
            return !hashSetselectedItems.isEmpty();
        }

        public List<String> getSelectedImagePaths() {
            List<String> selectedPaths = new ArrayList<>();
            for (int position : hashSetselectedItems) {
                selectedPaths.add(filePaths.get(position));
            }
            return selectedPaths;
        }

        // Helper method to get the formatted file size
        private String getFileSize(long size) {
            if (size <= 0) return "0 B";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

        // Helper method to get the formatted date
        private String getFormattedDate(long lastModified) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(lastModified));
        }

        // Static helper methods for file type checking
        public static boolean isVideoFile(File file) {
            String[] videoExtensions = {".mp4", ".mkv", ".avi", ".mov"};
            for (String extension : videoExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isAudioFile(File file) {
            String[] audioExtensions = {".mp3", ".wav", ".flac", ".m4a"};
            for (String extension : audioExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isImageFile(File file) {
            String[] imageExtensions = {".png", ".jpg", ".jpeg"};
            for (String extension : imageExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isDocumentFile(File file) {
            String[] documentExtensions = {".doc", ".pdf", ".docx", ".ppt", ".pptx", ".txt"};
            for (String extension : documentExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageViewtick, imageViewimage, imageViewtickblank;
            TextView textViewtitle, textViewtime, textViewsize;
            RelativeLayout containertick;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                imageViewtick = itemView.findViewById(R.id.file_tickMarkImageView);
//                imageViewtickblank = itemView.findViewById(R.id.item_file_tick_blank);
                imageViewimage = itemView.findViewById(R.id.file_image);
                textViewsize = itemView.findViewById(R.id.file_size);
//                textViewdate = itemView.findViewById(R.id.file_date);
                textViewtitle = itemView.findViewById(R.id.file_details);
//                containertick = itemView.findViewById(R.id.container_file_tick);
            }
        }

        public void updateImagePaths(ArrayList<String> newImagePaths) {
            filePaths.clear();
            filePaths.addAll(newImagePaths);
            // Clear selections as the data has changed
            hashSetselectedItems.clear();
            notifyDataSetChanged();
            listener.onSelectionChanged(false);
        }
    }
