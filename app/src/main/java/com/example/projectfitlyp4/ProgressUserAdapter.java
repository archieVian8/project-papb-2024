package com.example.projectfitlyp4;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfitlyp4.database.AppDatabase;
import com.example.projectfitlyp4.database.FirebaseProgressUserDao;
import com.example.projectfitlyp4.database.ProgressUser;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.List;

public class ProgressUserAdapter extends RecyclerView.Adapter<ProgressUserAdapter.ProgressUserViewHolder> {
    private List<ProgressUser> progressUserList;
    private Context context;
    private AppDatabase appDatabase;
    private FirebaseProgressUserDao firebaseProgressUserDao;
    private DecimalFormat decimalFormat;

    public ProgressUserAdapter(List<ProgressUser> progressUserList, Context context, FirebaseProgressUserDao firebaseProgressUserDao) {
        this.progressUserList = progressUserList;
        this.context = context;
        appDatabase = AppDatabase.getInstance(context);
        this.firebaseProgressUserDao = firebaseProgressUserDao;
    }

    public void setDecimalFormat(DecimalFormat format) {
        this.decimalFormat = format;
    }

    @NonNull
    @Override
    public ProgressUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_list, parent, false);
        return new ProgressUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressUserViewHolder holder, int position) {
        ProgressUser progressUser = progressUserList.get(position);
        holder.bind(progressUser);
    }

    @Override
    public int getItemCount() {
        return progressUserList.size();
    }

    public class ProgressUserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvWeight, tvHeight, tvBMI;
        private ImageButton btnDelete;

        public ProgressUserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvRecyclerview_date);
            tvWeight = itemView.findViewById(R.id.tvReyclerView_Weight);
            tvHeight = itemView.findViewById(R.id.tvRecyclerView_Height);
            tvBMI = itemView.findViewById(R.id.tvRecyclerView_BMI);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ProgressUser progressUserToDelete = progressUserList.get(position);
                        progressUserList.remove(position);
                        notifyItemRemoved(position);
                        new DeleteDataTask(context, appDatabase, progressUserToDelete).execute();
                        firebaseProgressUserDao.deleteProgressUserFromFirebase(progressUserToDelete);
                    }
                }
            });
        }

        public void bind(ProgressUser progressUser) {
            tvDate.setText(progressUser.getDate());
            tvWeight.setText(String.format("%s kg", decimalFormat.format(progressUser.getWeight())));
            tvHeight.setText(String.format("%s cm", decimalFormat.format(progressUser.getHeight())));
            tvBMI.setText(String.format("%s", decimalFormat.format(progressUser.getBmi())));
        }
    }

    private static class DeleteDataTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> contextRef;
        private AppDatabase appDatabase;
        private ProgressUser progressUser;

        DeleteDataTask(Context context, AppDatabase appDatabase, ProgressUser progressUser) {
            contextRef = new WeakReference<>(context);
            this.appDatabase = appDatabase;
            this.progressUser = progressUser;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            appDatabase.progressUserDao().deleteByFirebaseId(progressUser.getFirebaseId());
            return null;
        }
    }
}
