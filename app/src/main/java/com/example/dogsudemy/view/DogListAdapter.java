package com.example.dogsudemy.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogsudemy.R;
import com.example.dogsudemy.databinding.ListItemBinding;
import com.example.dogsudemy.model.DogBreed;
import com.example.dogsudemy.util.Util;

import java.util.ArrayList;
import java.util.List;

public class DogListAdapter extends RecyclerView.Adapter<DogListAdapter.DogViewHolder> implements DogClickListener {

    private ArrayList<DogBreed> dogsList;

    public DogListAdapter(ArrayList<DogBreed> dogsList) {
        this.dogsList = dogsList;
    }

    public void updateDogsList(List<DogBreed> newDogsList) {

        dogsList.clear();
        dogsList.addAll(newDogsList);
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public DogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemBinding view = DataBindingUtil.inflate(inflater, R.layout.list_item, parent, false);

        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        return new DogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DogViewHolder holder, int position) {

        holder.itemView.setDog(dogsList.get(position));
        holder.itemView.setListener(this );


//        ImageView image = holder.itemView.findViewById(R.id.imageViewListItem);
//        TextView name = holder.itemView.findViewById(R.id.textviewDogTitle);
//        TextView lifespan = holder.itemView.findViewById(R.id.textviewDogSubtitle);
//        LinearLayout linearLayout = holder.itemView.findViewById(R.id.dogListItemLayout);
//
//        name.setText(dogsList.get(position).dogBreed);
//        lifespan.setText(dogsList.get(position).lifeSpan);
//
//        Util.loadImage(image, dogsList.get(position).imageUrl, Util.getProgressDrawable(image.getContext()));
//        linearLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ListFragmentDirections.ActionDetails action = ListFragmentDirections.actionDetails();
//                action.setDogsId(dogsList.get(position).uuid);
//                Navigation.findNavController(linearLayout).navigate(action);
//            }
//        });


    }

    @Override
    public void onDogClicked(View view) {

        String uuidString = ((TextView)view.findViewById(R.id.dogId)).getText().toString();
        int uuid = Integer.valueOf(uuidString);
        ListFragmentDirections.ActionDetails action = ListFragmentDirections.actionDetails();
        action.setDogsId(uuid);
        Navigation.findNavController(view).navigate(action);

    }

    @Override
    public int getItemCount() {
        return dogsList.size();
    }

    class DogViewHolder extends RecyclerView.ViewHolder {

        public ListItemBinding itemView;

        public DogViewHolder(@NonNull ListItemBinding itemView) {
            super(itemView.getRoot());
            this.itemView = itemView;
        }
    }

}
