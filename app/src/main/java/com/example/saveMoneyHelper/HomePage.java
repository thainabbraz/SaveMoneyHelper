package com.example.saveMoneyHelper;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.ProgressBar;

import android.widget.TextView;

import com.example.saveMoneyHelper.auth.ProfileEditActivity;
import com.example.saveMoneyHelper.categories.Category;
import com.example.saveMoneyHelper.categories.TopCategoriesAdapter;
import com.example.saveMoneyHelper.categories.TopCategoryListViewModel;
import com.example.saveMoneyHelper.firebase.FirebaseElement;
import com.example.saveMoneyHelper.firebase.FirebaseObserver;
import com.example.saveMoneyHelper.firebase.factories.TopWalletEntriesViewModelFactory;

import com.example.saveMoneyHelper.firebase.models.WalletEntry;
import com.example.saveMoneyHelper.firebase.utils.ListDataSet;

import com.example.saveMoneyHelper.settings.PreferencesManager;
import com.example.saveMoneyHelper.settings.UserSettings;
import com.example.saveMoneyHelper.util.CalendarHelper;
import com.example.saveMoneyHelper.util.CategoriesHelper;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;


import java.util.ArrayList;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;


public class HomePage extends Fragment {

    private PieChart pieChart;
    private UserSettings userSettings;
    private FloatingActionButton btnFloattingProfile;
    private ProgressBar progressbar_income_expense;
    private TextView incomesTextView;
    private TextView balance;
    private Calendar dateBegin;
    private Calendar dateEnd;
    private ListView favoriteListView;
    private ListDataSet<WalletEntry> walletEntryListDataSet;
    private TopCategoriesAdapter adapter;
    private ArrayList<TopCategoryListViewModel> categoryModelsHome;
    private ArrayList<Integer> colors = new ArrayList<>();
    private static final int[] PIE_COLORS = {
            rgb("#02c39a"), rgb("#028090"), rgb("#b7e4c7")
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        //MONTHLY BY DEFAULT
        userSettings = new UserSettings();

        if (PreferencesManager.getInstance().getSavedUserSettings(getContext()) != null) {

            dateBegin = CalendarHelper.getStartDate(PreferencesManager.getInstance().getSavedUserSettings(getContext()));
            dateEnd = CalendarHelper.getEndDate(PreferencesManager.getInstance().getSavedUserSettings(getContext()));


        } else {
            dateBegin = CalendarHelper.getStartDate(userSettings);
            dateEnd = CalendarHelper.getEndDate(userSettings);


        }

        categoryModelsHome = new ArrayList<>();
        btnFloattingProfile = view.findViewById(R.id.btn_floatingProfile);
        pieChart = view.findViewById(R.id.pie_chart);
        favoriteListView = view.findViewById(R.id.favourite_categories_list_view);
        progressbar_income_expense = view.findViewById(R.id.progress_bar);
        balance = view.findViewById(R.id.balance);

        adapter = new TopCategoriesAdapter(categoryModelsHome, getContext());
        favoriteListView.setAdapter(adapter);

        //Profile button
        btnFloattingProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), ProfileEditActivity.class);
                startActivity(i);
            }
        });

        //Setting month filter for top 10 expenses
        TopWalletEntriesViewModelFactory.getModel(FirebaseAuth.getInstance().getCurrentUser().getUid(),
        getActivity()).setDateFilter(dateBegin, dateEnd);
        //Observer for TopWalletEntries
        TopWalletEntriesViewModelFactory.getModel(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                getActivity()).observe(this,
                new FirebaseObserver<FirebaseElement<ListDataSet<WalletEntry>>>() {

                    @Override
                    public void onChanged(FirebaseElement<ListDataSet<WalletEntry>> firebaseElement) {
                        if (firebaseElement.hasNoError()) {
                            HomePage.this.walletEntryListDataSet = firebaseElement.getElement();
                            dataUpdated();

                        }
                    }

                });


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    private void dataUpdated() {

        if (walletEntryListDataSet != null) {
            //list of entries
            List<WalletEntry> entryList = new ArrayList<>(walletEntryListDataSet.getList());

            long expensesSumInDateRange = 0;
            long incomesSumInDateRange = 0;

            HashMap<String, Long> categoryModels = new HashMap<>();
            for (WalletEntry walletEntry : entryList) {

                if (walletEntry.categoryID.contains("savings") && walletEntry.balanceDifference > 0) {
                    incomesSumInDateRange += walletEntry.balanceDifference;

                    if (categoryModels.get("poupanças") != null)
                        categoryModels.put("poupanças", categoryModels.get("poupanças") + walletEntry.balanceDifference);
                    else
                        categoryModels.put("poupanças", walletEntry.balanceDifference);

                }

                 if (walletEntry.balanceDifference < 0 && walletEntry.type != null){
                     expensesSumInDateRange += walletEntry.balanceDifference;
                     switch (walletEntry.type){
                         case "wants":
                             if (categoryModels.get("extras") != null)
                                 categoryModels.put("extras", categoryModels.get("extras") + walletEntry.balanceDifference);
                             else
                                 categoryModels.put("extras", walletEntry.balanceDifference);
                             break;
                         case "needs":
                             if (categoryModels.get("necessidades") != null)
                                 categoryModels.put("necessidades", categoryModels.get("necessidades") + walletEntry.balanceDifference);
                             else
                                 categoryModels.put("necessidades", walletEntry.balanceDifference);
                             break;
                     }


                 }
                 
            }

            ArrayList<PieEntry> pieEntries = new ArrayList<>();


            categoryModelsHome.clear();

            int count = 10;
            for (Map.Entry<String, Long> categoryModel : categoryModels.entrySet()) {

                //Populating arrayList<TopCategoryListViewModel>

                if (categoryModel.getValue()<0){
                    pieEntries.add(new PieEntry(-categoryModel.getValue(), categoryModel.getKey()));
                }else{
                    pieEntries.add(new PieEntry(categoryModel.getValue(), categoryModel.getKey()));
                }


            }

            Collections.sort(categoryModelsHome, new Comparator<TopCategoryListViewModel>() {
                @Override
                public int compare(TopCategoryListViewModel o1, TopCategoryListViewModel o2) {
                    return Long.compare(o1.getMoney(), o2.getMoney());
                }
            });
            for (int c :PIE_COLORS)
                colors.add(c);

            //Pie chart
            PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
            pieDataSet.setDrawValues(false);
            pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

            pieDataSet.setValueLinePart1OffsetPercentage(90.f);
            pieDataSet.setValueLinePart1Length(0.5f);
            pieDataSet.setValueLinePart2Length(0.8f);


            pieDataSet.setSliceSpace(2f);
            pieDataSet.setColors(colors);
            adapter.refresh(categoryModelsHome);


            PieData data = new PieData(pieDataSet);

            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(18f);
            data.setValueTextColor(Color.DKGRAY);

            data.setDrawValues(true);
            pieChart.setDrawEntryLabels(false);
            pieChart.setData(data);
            pieChart.setTouchEnabled(true);
            pieChart.getDescription().setEnabled(false);

            pieChart.setUsePercentValues(true);

            pieChart.setDragDecelerationFrictionCoef(0.95f);
            pieChart.setDrawHoleEnabled(false);

            pieChart.setRotationAngle(270);
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(true);

            pieChart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

            pieChart.animateY(1200, Easing.EaseInOutQuad);


            pieChart.invalidate();


            float money = incomesSumInDateRange+expensesSumInDateRange;

            balance.setText(String.valueOf(money + "€"));
            if (money>0)
                balance.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
            else if (money==0)
                balance.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
            else
                balance.setTextColor(ContextCompat.getColor(getContext(), R.color.outcome_color));

        }


    }
}