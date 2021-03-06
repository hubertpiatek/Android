package com.example.matcar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static com.example.matcar.CarAdapter_Final_Repair.final_car;
import static com.example.matcar.CarAdapter_Final_Repair.final_id;
import static com.example.matcar.MechanicAdapter.mechanic_id;
import static com.example.matcar.Repair_Data_Adapter.services;

public class Repairs_Final extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    public DrawerLayout mDrawerLayout;
    public ActionBarDrawerToggle mToggle;
    CalendarView calendarView;
    Button selecthour;
    Button diagnostic_button_confirm;
    static Button choose_mechanic_repair;
    static Button choose_car_button_repair;
    private String date = "";
    private String day_of_month = "";
    private int day_of_week;
    private String month_main = "";
    private String year_main = "";
    private String hour_main = "";
    private String minute_main = "";
    private FirebaseFirestore fstore;
    private EditText diagnostic_additional_informations;
    public static Dialog myDialog_rep;
    public static Dialog myDialog_final;
    private CollectionReference mechanicref;
    private MechanicAdapter mechanicAdapter;
    private CarAdapter_Final_Repair carAdapter_final_repair;
    private CollectionReference cars_ref;
    private DocumentReference documentReference4;
    private ArrayList<Car_Info> car_infos;
    public RecyclerView recycler_cars;
    public Button btn_final_repair_car_back;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Naprawy");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairs__final);
        choose_mechanic_repair = findViewById(R.id.choose_mechanic_button_repair);
        choose_car_button_repair = findViewById(R.id.choose_car_button_repair);
        //POP_UP_WINDOW
        myDialog_rep = new Dialog(this);
        myDialog_final = new Dialog(this);
        //POP_UP_WINDOW
        //FIREBASE
        fstore = FirebaseFirestore.getInstance();
        //BACK BUTTON
        btn_final_repair_car_back = findViewById(R.id.btn_final_repair_car_back);
        btn_final_repair_car_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(R.anim.right_to_left2, R.anim.left_to_right);
                finish();
            }
        });
        //BACK BUTTON
        mechanicref = fstore.collection("mechanics");
        cars_ref = fstore.collection("cars");
        //FIREBASE

        //MENU
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = new Menu(mToggle, navigationView, Repairs_Final.this);
        //END OF MENU

        //DODATKOWE INFORMACJE
        diagnostic_additional_informations = findViewById(R.id.diagnostic_additional_informations);
        //DODATKOWE INFORMACJE

        calendarView = findViewById(R.id.cal);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                day_of_month = String.valueOf(dayOfMonth);
                month_main = String.valueOf(month + 1);
                year_main = String.valueOf(year);
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
                month++;
                if (dayOfMonth < 10 && month < 10) {
                    date = "0" + (dayOfMonth) + " / " + "0" + month + " / " + year;
                } else if (dayOfMonth >= 10 && month < 10) {
                    date = (dayOfMonth) + " / " + "0" + month + " / " + year;
                } else if (month >= 10 && dayOfMonth < 10) {
                    date = "0" + (dayOfMonth) + " / " + month + " / " + year;
                } else {
                    date = (dayOfMonth) + " / " + month + " / " + year;
                }

            }
        });


        selecthour = findViewById(R.id.btn_hour);
        selecthour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "Wybierz godzinę");
            }
        });

        //BUTTON CONFIRM
        diagnostic_button_confirm = findViewById(R.id.diagnostic_button_confirm);
        diagnostic_button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (day_of_month.isEmpty() || month_main.isEmpty() || year_main.isEmpty()) {
                    day_of_month = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                    day_of_week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    month_main = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);
                    year_main = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
                }
                if (hour_main.isEmpty() || minute_main.isEmpty()) {
                    Toast.makeText(Repairs_Final.this, "Nie wybrano godziny", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(day_of_month) < Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                        Integer.parseInt(month_main) <= Calendar.getInstance().get(Calendar.MONTH) + 1 &&
                        Integer.parseInt(year_main) <= Calendar.getInstance().get(Calendar.YEAR)
                ) {
                    Toast.makeText(Repairs_Final.this, "Wybrana data pochodzi z przeszłości. Wybierz poprawną datę", Toast.LENGTH_SHORT).show();
                } else if (day_of_week == 1) {
                    Toast.makeText(Repairs_Final.this, "Firma nie pracuje w niedziele", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(hour_main) < 8 || Integer.parseInt(hour_main) >= 19 && (day_of_week > 1 && day_of_week < 7)) {
                    Toast.makeText(Repairs_Final.this, "Firma w tygodniu pracuje od 8 do 17. Wybierz inną godzinę", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(hour_main) < 8 || Integer.parseInt(hour_main) >= 14 && day_of_week == 7) {
                    Toast.makeText(Repairs_Final.this, "Firma w soboty pracuje od 8 do 14. Wybierz inną godzinę", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(day_of_month) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                        Integer.parseInt(month_main) == Calendar.getInstance().get(Calendar.MONTH) + 1 &&
                        Integer.parseInt(year_main) == Calendar.getInstance().get(Calendar.YEAR) &&
                        !hour_main.isEmpty() && !minute_main.isEmpty() && Integer.parseInt(hour_main) <= Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1
                ) {

                    Toast.makeText(Repairs_Final.this, "Wizyta musi być umówiona co najmniej z około 2-godzinnym wyprzedzeniem", Toast.LENGTH_SHORT).show();
                } else if (choose_mechanic_repair.getText().equals("Wybierz mechanika realizującego usługę")) {
                    Toast.makeText(Repairs_Final.this, "Wybierz mechanika", Toast.LENGTH_SHORT).show();
                } else if (choose_car_button_repair.getText().equals("Wybierz samochód")) {
                    Toast.makeText(Repairs_Final.this, "Wybierz samochód", Toast.LENGTH_SHORT).show();
                } else {
                    if (Integer.parseInt(day_of_month) < 10 && Integer.parseInt(month_main) < 10) {
                        date = "0" + (day_of_month) + " / " + "0" + month_main + " / " + year_main;
                    } else if (Integer.parseInt(day_of_month) >= 10 && Integer.parseInt(month_main) < 10) {
                        date = (day_of_month) + " / " + "0" + month_main + " / " + year_main;
                    } else if (Integer.parseInt(month_main) >= 10 && Integer.parseInt(day_of_month) < 10) {
                        date = "0" + (day_of_month) + " / " + month_main + " / " + year_main;
                    } else {
                        date = (day_of_month) + " / " + month_main + " / " + year_main;
                    }
                    //CHECK IF MECHANIC IS FREE
                    fstore.collection("mechanics_schedule").whereEqualTo("id_mechanic", mechanic_id)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        int count = 0;
                                        int count2 = 0;
                                        int count3 = 0;
                                        int hour_activity = 0;
                                        int minute_activity = 0;
                                        int hour_database = 0;
                                        int minute_database = 0;
                                        int full_time_activity = 0;
                                        int full_time_database = 0;
                                        String get_time[];
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            count++;
                                            if (date.equals(document.get("date"))) {
                                                count2++;
                                                StringBuilder database_time = new StringBuilder(document.get("time").toString());
                                                if (database_time.charAt(3) == '0') {
                                                    database_time.deleteCharAt(3);
                                                }
                                                if (database_time.charAt(0) == '0') {
                                                    database_time.deleteCharAt(0);
                                                }
                                                StringTokenizer tokens = new StringTokenizer(database_time.toString(), ":");
                                                String first = tokens.nextToken();
                                                String second = tokens.nextToken();
                                                hour_database = Integer.parseInt(first);
                                                minute_database = Integer.parseInt(second);
                                                full_time_database = hour_database * 60 + minute_database;

                                                StringBuilder activity_time = new StringBuilder(hour_main + ":" + minute_main);
                                                if (activity_time.charAt(3) == '0') {
                                                    activity_time.deleteCharAt(3);
                                                }
                                                if (activity_time.charAt(0) == '0') {
                                                    activity_time.deleteCharAt(0);
                                                }
                                                StringTokenizer tokens2 = new StringTokenizer(activity_time.toString(), ":");
                                                String first2 = tokens2.nextToken();
                                                String second2 = tokens2.nextToken();
                                                hour_activity = Integer.parseInt(first2);
                                                minute_activity = Integer.parseInt(second2);
                                                full_time_activity = hour_activity * 60 + minute_activity;

                                                if (full_time_activity >= full_time_database + 60 || full_time_activity <= full_time_database - 60) {
                                                } else {
                                                    count3++;
                                                }
                                            }
                                        }
                                        if (count3 != 0)
                                            Toast.makeText(Repairs_Final.this, "Wybrany mechanik w tym dniu w tych godzinach jest zajęty. Spróbuj wybrać inną godzinę. ", Toast.LENGTH_SHORT).show();
                                        if (count3 == 0) Add();
                                    } else {
                                    }
                                }
                            });

                }

            }
        });
        //BUTTON CONFIRM
    }

    public void ShowPopUp4(View v) {
        myDialog_final.setContentView(R.layout.custompopup3);
        Window window = myDialog_final.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.TOP;
        wlp.y = 400;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        myDialog_final.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_background));
        recycler_cars = myDialog_final.findViewById(R.id.repair_final_recycler_view);
        TextView close = myDialog_final.findViewById(R.id.close_repair_total_order);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog_final.dismiss();
            }
        });
        myDialog_final.show();
        setUpRecyclerView2();
    }

    private void setUpRecyclerView2() {
        car_infos = new ArrayList<>();
        fstore.collection("cars").whereEqualTo("user_id", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override

                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                count++;
                                if (document.exists()) {
                                    recycler_cars.setVisibility(View.VISIBLE);
                                    car_infos.add(document.toObject(Car_Info.class));
                                }
                            }
                            carAdapter_final_repair = new CarAdapter_Final_Repair(car_infos);
                            recycler_cars.setHasFixedSize(true);
                            recycler_cars.setLayoutManager(new LinearLayoutManager(Repairs_Final.this));
                            recycler_cars.setAdapter(carAdapter_final_repair);
                        } else {
                        }
                    }
                });
    }

    private void Add() {
        //CHECK IF MECHANIC IS FREE
        DocumentReference documentReference = fstore.collection("repair_orders").document();
        DocumentReference documentReference3 = fstore.collection("history").document();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> diagnostic_to_database = new HashMap<>();
        diagnostic_to_database.put("date", date);
        diagnostic_to_database.put("time", hour_main + ":" + minute_main);
        diagnostic_to_database.put("additional_informations", diagnostic_additional_informations.getText().toString());
        diagnostic_to_database.put("status", "Przyjęto");
        diagnostic_to_database.put("car", final_car);
        diagnostic_to_database.put("car_id", final_id);
        diagnostic_to_database.put("name_of_service", "Naprawa - " + getIntent().getStringExtra("final_order"));
        diagnostic_to_database.put("repair_price", getIntent().getStringExtra("total_price"));
        if (user == null) {
            diagnostic_to_database.put("history_id", "guest");
        } else {
            diagnostic_to_database.put("history_id", documentReference3.getId());
        }
        //CHECK IF USER IS LOGGED
        if (user != null) {
            String uid = user.getUid();
            diagnostic_to_database.put("client_id", uid);
        } else {
            diagnostic_to_database.put("client_id", "guest");
        }
        if (mechanic_id.isEmpty()) diagnostic_to_database.put("mechanic", "guest");
        else diagnostic_to_database.put("mechanic", mechanic_id);
        //ADD TO HISTORY
        if (user != null) {
            Map<String, Object> history_to_database = new HashMap<>();
            String concat_day_of_month, concat_month, concat_year;
            if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) < 10) {
                concat_day_of_month = "0" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            } else {
                concat_day_of_month = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            }
            if (Calendar.getInstance().get(Calendar.MONTH) + 1 < 10) {
                concat_month = "0" + String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);
            } else {
                concat_month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);
            }
            concat_year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
            String data_concat = concat_day_of_month + " / " + concat_month + " / " + concat_year;
            history_to_database.put("document_id", documentReference3.getId().toString());
            history_to_database.put("name_of_service", "Naprawa - " + getIntent().getStringExtra("final_order"));
            history_to_database.put("repair_price", getIntent().getStringExtra("total_price"));
            history_to_database.put("date_of_order", data_concat);
            history_to_database.put("date_and_time", date + ", " + hour_main + ":" + minute_main);
            history_to_database.put("status", "Przyjęto");
            history_to_database.put("client_id", user.getUid());
            history_to_database.put("mechanic", mechanic_id);
            history_to_database.put("car", final_car);
            history_to_database.put("car_id", final_id);
            documentReference3.set(history_to_database).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                }
            });

        }
        //ADD TO HISTORY
        //CHECK IF USER IS LOGGED
        documentReference.set(diagnostic_to_database).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("USER: ", "onSuccess:user Form is sent");
                Toast.makeText(Repairs_Final.this, "Pomyślnie umówiono się na wizytę", Toast.LENGTH_SHORT).show();
            }
        });
        //MECHANIC SCHEDULE
        DocumentReference mechanic_sch = fstore.collection("mechanics_schedule").document();
        Map<String, Object> mechanics_schedule = new HashMap<>();
        mechanics_schedule.put("date", date);
        mechanics_schedule.put("time", hour_main + ":" + minute_main);
        mechanics_schedule.put("id_mechanic", mechanic_id);
        mechanics_schedule.put("car", final_car);
        mechanics_schedule.put("car_id", final_id);
        mechanics_schedule.put("service", "Naprawa - " + getIntent().getStringExtra("final_order"));
        mechanics_schedule.put("repair_price", getIntent().getStringExtra("total_price"));
        if (user == null) {
            mechanics_schedule.put("history_id", "guest");
        } else {
            mechanics_schedule.put("history_id", documentReference3.getId());
        }
        mechanic_sch.set(mechanics_schedule).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        });
        //MECHANIC SCHEDULE
        services.clear();
        Intent i = new Intent(Repairs_Final.this, MainActivity.class);
        startActivity(i);
    }

    private void setUpRecyclerView() {
        Query query = mechanicref.orderBy("name", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Mechanic> options = new FirestoreRecyclerOptions.Builder<Mechanic>().setQuery(query, Mechanic.class).build();
        String act = "Repairs";
        mechanicAdapter = new MechanicAdapter(options, act);
        RecyclerView recyclerView = myDialog_rep.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mechanicAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void ShowPopUp(View v) {
        myDialog_rep.setContentView(R.layout.custompopup);
        Window window = myDialog_rep.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.TOP;
        wlp.y = 400;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        myDialog_rep.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_background));
        TextView close = myDialog_rep.findViewById(R.id.close_popup);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mechanicAdapter.stopListening();
                myDialog_rep.dismiss();
            }
        });
        myDialog_rep.show();
        setUpRecyclerView();
        mechanicAdapter.startListening();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView textView = (TextView) findViewById(R.id.hour);
        hour_main = String.valueOf(hourOfDay);
        minute_main = String.valueOf(minute);
        if (hourOfDay < 10) hour_main = "0" + hour_main;
        if (minute < 10) minute_main = "0" + minute_main;
        textView.setText("Wybrana godzina: " + hour_main + ":" + minute_main);

    }
}
