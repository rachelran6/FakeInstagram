package com.example.fakeinsta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import androidx.recyclerview.widget.RecyclerView;

public class CommentViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    ArrayList<Comments> comments;
    private Context mContext;
    private String photoUrl;
    private String caption;

    public CommentViewAdapter(Context context, ArrayList<Comments> commentsList, String caption, String photoUrl){
        this.mContext = context;
        this.comments = commentsList;
        this.photoUrl = photoUrl;
        this.caption = caption;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            return new VHItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view, parent, false));
        } else if (viewType == TYPE_HEADER) {
            //inflate your layout and pass it to view holder
            return new VHHeader(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_header, parent, false));
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHItem) {
            //cast holder to VHItem and set data
            VHItem itemViewHolder = (VHItem) holder;
            itemViewHolder.bind(comments.get(position-1).getUsername(), comments.get(position-1).getComment(), comments.get(position-1).getProfileRef());
        }
        else if (holder instanceof VHHeader) {
            //cast holder to VHHeader and set data for header.
            VHHeader headerViewHolder = (VHHeader) holder;
            headerViewHolder.bind(caption, photoUrl);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    class VHItem extends RecyclerView.ViewHolder {
        TextView username;
        TextView comments;
        ImageView imageView;

        void bind(String usernameStr, String commentsStr, String urlStr) {
            username.setText(usernameStr);
            comments.setText(commentsStr);
            Picasso.get().load(urlStr).into(imageView);
        }


        public VHItem(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.usernameComment);
            comments = itemView.findViewById(R.id.comment);
            imageView = itemView.findViewById(R.id.profileCommentImage);
        }
    }

    class VHHeader extends RecyclerView.ViewHolder {
        ImageView photoImage;
        TextView caption;

        public VHHeader(View itemView) {
            super(itemView);
            photoImage = itemView.findViewById(R.id.imageHeaderView);
            caption = itemView.findViewById(R.id.captionHeader);
        }

        void bind(String captionStr, String urlStr) {
            caption.setText(captionStr);
            Picasso.get().load(urlStr).into(photoImage);
        }
    }
}
