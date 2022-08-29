package com.muratcakin.yemektariflerikitabi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_row.view.*

class ListRecyclerAdapter(val mealList: ArrayList<String>, val idList: ArrayList<Int>): RecyclerView.Adapter<ListRecyclerAdapter.MealHolder>() {
    class MealHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealHolder {
        val inflater =LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row,parent,false)
        return MealHolder(view)
    }

    override fun onBindViewHolder(holder: MealHolder, position: Int) {
        holder.itemView.recycler_row_text.text = mealList[position]
        holder.itemView.setOnClickListener() {
            val action = ListFragmentDirections.actionListFragmentToRecipeFragment("fromrecycler", idList[position])
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return mealList.size
    }
}