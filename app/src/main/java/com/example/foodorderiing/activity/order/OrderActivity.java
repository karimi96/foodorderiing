package com.example.foodorderiing.activity.order;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.foodorderiing.R;
import com.example.foodorderiing.activity.customer.CustomerActivity;
import com.example.foodorderiing.activity.product.ProductActivity;
import com.example.foodorderiing.adapter.OrderAdapter;
import com.example.foodorderiing.database.DatabaseHelper;
import com.example.foodorderiing.database.dao.OrderDao;
import com.example.foodorderiing.database.dao.OrderDetailDao;
import com.example.foodorderiing.helper.Tools;
import com.example.foodorderiing.model.Customer;
import com.example.foodorderiing.model.Order;
import com.example.foodorderiing.model.OrderDetail;
import com.example.foodorderiing.model.Product;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.spi.FileTypeDetector;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ir.hamsaa.persiandatepicker.PersianDatePickerDialog;
import ir.hamsaa.persiandatepicker.api.PersianPickerDate;
import ir.hamsaa.persiandatepicker.api.PersianPickerListener;
import ir.hamsaa.persiandatepicker.util.PersianCalendarUtils;
import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;


public class OrderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrderAdapter ordringAdapter;
    private DatabaseHelper db;
    private OrderDao dao_order;
    private OrderDetailDao dao_detail ;
    private View box_customer;
    private SlidrInterface slidr;
    private LottieAnimationView lottie;
    private List<Product> orderDetailList;
    private TextView add_order, name_customer ,number_order, total, save_order , noOrder ;
    private Customer customer;
    private CardView card_number;
    private String CODE = String.valueOf(System.currentTimeMillis());
    private RelativeLayout relative_total;
    private String edit ;
    private PersianDatePickerDialog picker;
    private static final String TAG = "OrderActivity";

    private TextView datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        slidr = Slidr.attach(this);
//        if(getIntent().getExtras() != null){
//            edit = getIntent().getStringExtra("edit");
//
//        }

        initDataBase();
        initID();
        initBoxCustomer();
        initBoxProduct();
        initLottie();
        initRecycler();
        initSaveOrder();
        datePicker();
    }



    @Override
    protected void onActivityResult( int requestCode, int resultCode , @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch (requestCode){

                case 100:
                        String json_customer = data.getExtras().getString("json_customer");
                        customer = new Gson().fromJson(json_customer,Customer.class);
                        name_customer.setText(customer.name);
                        break;

                case 200:
                    noOrder.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    relative_total.setVisibility(View.VISIBLE);
                    String json_product = data.getExtras().getString("json_product");
                    Product product = new Gson().fromJson(json_product,Product.class);
                    insertToOrderList(product);
                    break;
            }
        }
    }


    private void insertToOrderList(Product product){
        for (int i = 0; i < orderDetailList.size(); i++) {
            if(orderDetailList.get(i).product_id == product.product_id){
                orderDetailList.get(i).amount = orderDetailList.get(i).amount + 1;
                ordringAdapter.notifyDataSetChanged();
                initCounter();
                return;
            }
        }
        orderDetailList.add(product);
        ordringAdapter.notifyDataSetChanged();
        initCounter();
    }


    private void initRecycler(){
        orderDetailList = new ArrayList<>();
        ordringAdapter = new OrderAdapter(orderDetailList, this, new OrderAdapter.Listener() {
            @Override
            public void onAdded(int pos) {
                orderDetailList.get(pos).amount = orderDetailList.get(pos).amount + 1;
                ordringAdapter.notifyItemChanged(pos);
                initCounter();
            }

            @Override
            public void onRemove(int pos) {
                if (orderDetailList.get(pos).amount > 1){
                    orderDetailList.get(pos).amount = orderDetailList.get(pos).amount - 1;
                    ordringAdapter.notifyItemChanged(pos);
                }else {
                    orderDetailList.remove(pos);
                    ordringAdapter.notifyDataSetChanged();

                }
                initCounter();
            }
        });
        recyclerView.setAdapter(ordringAdapter);
        recyclerView.setHasFixedSize(true);
    }


    private void initDataBase(){
        db = DatabaseHelper.getInstance(getApplicationContext());
        dao_order = db.orderDao();
        dao_detail = db.orderDetailDao();
    }


    private void initID(){
        add_order = findViewById(R.id.add_order);
        box_customer = findViewById(R.id.box_customer);
        name_customer = findViewById(R.id.name);
        lottie = findViewById(R.id.lottie);
        total = findViewById(R.id.tv_total);
        save_order = findViewById(R.id.save_order);
        number_order = findViewById(R.id.text_number_of_order);
        recyclerView = findViewById(R.id.recycler_ordering);
        card_number = findViewById(R.id.card_number);
        relative_total =findViewById(R.id.relative_order);
        noOrder =findViewById(R.id.noOrder);
        datePicker =findViewById(R.id.datePicker);
    }


    private void initBoxCustomer(){
        box_customer.setOnClickListener(v ->{
            Intent intent = new Intent(this, CustomerActivity.class);
            intent.putExtra("for_order", true);
           startActivityForResult(intent,100);
        });
    }


    private void initBoxProduct(){
        add_order.setOnClickListener(v ->{
            Intent intent = new Intent(this, ProductActivity.class);
            intent.putExtra("for_order", true);
            startActivityForResult(intent,200);
        });
    }


    private void initCounter(){
        if(orderDetailList.size() > 0){
            card_number.setVisibility(View.VISIBLE);
            number_order.setText(orderDetailList.size()+"");

        }else {
            card_number.setVisibility(View.GONE);
        }
        total.setText(Tools.getForamtPrice(getTotalPrice()+""));
    }


    private Integer getTotalPrice(){
        int p = 0;
        for (int i = 0; i < orderDetailList.size(); i++) {
            p = p + (Tools.convertToPrice(orderDetailList.get(i).price) * orderDetailList.get(i).amount);
        }
        return p;
    }


    private void initLottie(){
        lottie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(orderDetailList.size() != 0 ){
                    lottie.setRepeatCount(0);
                    lottie.playAnimation();
                    orderDetailList.clear();
                    ordringAdapter.notifyDataSetChanged();
                    relative_total.setVisibility(View.GONE);
                }else {
                    Toast.makeText(OrderActivity.this, "لیست خالی است", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public String getCurrentTime_Date(){
        PersianDate c = new PersianDate();
        PersianDateFormat dateFormat = new PersianDateFormat(" Y/m/d ");
        String datetime = dateFormat.format(c);
        return datetime;
    }

    public String getCurrentTime_time(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss ");
        String datetime = dateFormat.format(c.getTime());
        return datetime;
    }


    private void initSaveOrder(){
        save_order.setOnClickListener(view -> {
                    if( customer == null ){
                        Toast.makeText(this, "مشتری را انتخاب کنید", Toast.LENGTH_SHORT).show();
                    }else {
                        dao_order.insertOrder(new Order(customer.name , CODE , customer.customer_id , 1 ,
                                total.getText()+"" , "با تمام مخلفات " ,getCurrentTime_Date() , getCurrentTime_time() ));

                        for (int i = 0; i < orderDetailList.size(); i++) {

                            dao_detail.insertOrderDetail(new OrderDetail(orderDetailList.get(i).name , orderDetailList.get(i).category ,
                                    String.valueOf(Tools.convertToPrice(orderDetailList.get(i).price) * orderDetailList.get(i).amount)  ,
                                    orderDetailList.get(i).amount ,CODE ));

                            Toast.makeText(OrderActivity.this, " سفارش " + customer.name + " با موفقیت ثبت شد", Toast.LENGTH_SHORT).show();
                        }
//                        db.close();
                        finish();
                    }


        });
    }



    private void datePicker(){
        datePicker.setOnClickListener(v -> {
            picker = new PersianDatePickerDialog(this)
                    .setPositiveButtonString("باشه")
                    .setNegativeButton("بیخیال")
                    .setTodayButton("امروز")
                    .setTodayButtonVisible(true)
                    .setMinYear(1300)
                    .setMaxYear(PersianDatePickerDialog.THIS_YEAR)
                    .setMaxMonth(PersianDatePickerDialog.THIS_MONTH)
                    .setMaxDay(PersianDatePickerDialog.THIS_DAY)
                    .setInitDate(1370, 3, 13)
                    .setActionTextColor(Color.GRAY)
                    .setTitleType(PersianDatePickerDialog.WEEKDAY_DAY_MONTH_YEAR)
                    .setShowInBottomSheet(true)
                    .setListener(new PersianPickerListener() {
                        @Override
                        public void onDateSelected(@NotNull PersianPickerDate persianPickerDate) {
                            Log.d(TAG, "onDateSelected: " + persianPickerDate.getTimestamp());//675930448000
                            Log.d(TAG, "onDateSelected: " + persianPickerDate.getGregorianDate());//Mon Jun 03 10:57:28 GMT+04:30 1991
                            Log.d(TAG, "onDateSelected: " + persianPickerDate.getPersianLongDate());// دوشنبه  13  خرداد  1370
                            Log.d(TAG, "onDateSelected: " + persianPickerDate.getPersianMonthName());//خرداد
                            Log.d(TAG, "onDateSelected: " + PersianCalendarUtils.isPersianLeapYear(persianPickerDate.getPersianYear()));//true
                            Toast.makeText(getApplicationContext() , persianPickerDate.getPersianYear() + "/" + persianPickerDate.getPersianMonth() + "/" + persianPickerDate.getPersianDay(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDismissed() {

                        }
                    });

            picker.show();
        });
    }




//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
////        if(db != null){
////            db.close();
////        }
//    }
}