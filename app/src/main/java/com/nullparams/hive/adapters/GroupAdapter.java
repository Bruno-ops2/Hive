package com.nullparams.hive.adapters;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nullparams.hive.R;
import com.nullparams.hive.models.Group;
import com.squareup.picasso.Picasso;

public class GroupAdapter extends FirestoreRecyclerAdapter<Group, GroupAdapter.ListHolder> {

    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;
    private boolean darkModeOn;
    private Context mContext;

    public GroupAdapter(@NonNull FirestoreRecyclerOptions<Group> options, SharedPreferences sharedPreferences, Context context) {
        super(options);

        darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ListHolder holder, int position, @NonNull Group model) {

        if (darkModeOn) {
            holder.textViewGroupName.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
        }

        holder.textViewGroupName.setText(model.getGroupName());

        if (model.getGroupPicUrl() != null) {
            Picasso.get().load(model.getGroupPicUrl()).into(holder.imageViewGroupPic);
        }
    }

    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item,
                parent, false);
        return new ListHolder(v);
    }

    class ListHolder extends RecyclerView.ViewHolder {

        TextView textViewGroupName;
        ImageView imageViewGroupPic;

        public ListHolder(View itemView) {
            super(itemView);

            textViewGroupName = itemView.findViewById(R.id.text_view_group_name);
            imageViewGroupPic = itemView.findViewById(R.id.image_view_group_pic);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        longListener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                    }
                    return true;
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }
}
