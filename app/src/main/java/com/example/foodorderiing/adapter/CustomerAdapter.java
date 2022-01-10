package com.example.foodorderiing.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderiing.R;
import com.example.foodorderiing.activity.customer.AddNewCustomerActivity;
import com.example.foodorderiing.database.DatabaseHelper;
import com.example.foodorderiing.database.dao.CustomerDao;
import com.example.foodorderiing.database.dao.OrderDao;
import com.example.foodorderiing.database.dao.OrderDetailDao;
import com.example.foodorderiing.model.Customer;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> implements Filterable {
    Context context;
    List<Customer> list;
    List<Customer> list_search;
    Listener listener;
    DatabaseHelper database;
    CustomerDao customerDao;
    OrderDao orderDao;
    OrderDetailDao orderDetailDao;


    public CustomerAdapter(List<Customer> list, Context context, Listener listener) {
        this.list_search = list;
        this.list = new ArrayList<>(list_search);
        this.context = context;
        this.listener = listener;
    }


    public interface Listener{
        void onClickListener(Customer customer , int pos , String name);
    }


    @Override
    public CustomerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_item_customer,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(CustomerAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Customer customer = list.get(position);
        holder.tv_name_customer.setText(customer.name);
        holder.tv_phone.setText(customer.phone);
        holder.tv_address.setText(customer.address);
        holder.cardSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickListener(customer , position , list.get(position).name);
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name_customer, tv_phone, tv_address;
        CardView cardSwipe;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_name_customer = itemView.findViewById(R.id.tv_name_customer);
            tv_phone = itemView.findViewById(R.id.tv_phone_customer);
            tv_address = itemView.findViewById(R.id.tv_address_customer);
            cardSwipe = itemView.findViewById(R.id.card_FG);
        }
    }


        public void showDialogSheet(int pos, String name) {
            final Dialog dialog_sheet = new Dialog(context);
            dialog_sheet.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog_sheet.setContentView(R.layout.bottom_sheet_customer);

            LinearLayout edit = dialog_sheet.findViewById(R.id.linear_edit_c);
            LinearLayout delete = dialog_sheet.findViewById(R.id.linear_delete_c);
            TextView title = dialog_sheet.findViewById(R.id.name_sheet_c);
            title.setText(name);


            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AddNewCustomerActivity.class);
                    intent.putExtra("customer", new Gson().toJson(list.get(pos)));
                    context.startActivity(intent);
                    dialog_sheet.dismiss();
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(context)
                            .setTitle("حذف")
                            .setMessage("ایا مایلید این مورد را حذف کنید؟")
                            .setPositiveButton("بله", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    database = DatabaseHelper.getInstance(context.getApplicationContext());
                                    customerDao = database.customerDao();
                                    customerDao.deleteCustomer(list.get(pos));
                                    list.remove(pos);
                                    notifyItemRemoved(pos);
                                    notifyItemRangeChanged(pos, list.size());
                                    notifyDataSetChanged();
                                    dialog_sheet.dismiss();
                                    Toast.makeText(context, "با موفقیت حذف شد 😉 ", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton("خیر", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    dialog_sheet.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
            });
            dialog_sheet.show();
            dialog_sheet.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog_sheet.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog_sheet.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationSheet;
            dialog_sheet.getWindow().setGravity(Gravity.BOTTOM);
        }


        public void addList(List<Customer> arraylist) {
            list_search.clear();
            list_search.addAll(arraylist);
            list = new ArrayList<>(list_search);
            notifyDataSetChanged();
        }


    //  For search
    @Override
    public Filter getFilter() {
        return newsFilter;
    }

    private final Filter newsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<Customer> filterdNewList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                filterdNewList.addAll(list_search);
            }else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(Customer customer : list_search){

                    if(customer.name.toLowerCase().contains(filterPattern))
                        filterdNewList.add(customer);

                    if(customer.phone.toLowerCase().contains(filterPattern))
                        filterdNewList.add(customer);
                }
            }
            FilterResults results = new FilterResults();
            results.values = filterdNewList;
            results.count = filterdNewList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };
}