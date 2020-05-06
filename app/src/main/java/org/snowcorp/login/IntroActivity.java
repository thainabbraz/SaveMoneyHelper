package org.snowcorp.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {
    ViewPager viewPager;
    Adapter adapter;
    Animation btnAnim;
    List<Model> models;
    Integer[] colors = null;
    Button btnGetStarted;
    TextView tvSkip;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // when this activity is about to be launch we need to check if its openened before or not

        if (restorePrefData()) {

            Intent loginActivity = new Intent(getApplicationContext(),LoginActivity.class );
            startActivity(loginActivity);
            finish();


        }


        setContentView(R.layout.activity_intro);

        models = new ArrayList<>();
        models.add(new Model(R.drawable.code_icon, "Brochure", "Brochure is an informative paper document (often also used for advertising) that can be folded into a template"));
        models.add(new Model(R.drawable.sleep_icon, "Sticker", "Sticker is a type of label: a piece of printed paper, plastic, vinyl, or other material with pressure sensitive adhesive on one side"));
        models.add(new Model(R.drawable.sleep_icon, "Poster", "Poster is any piece of printed paper designed to be attached to a wall or vertical surface."));
        models.add(new Model(R.drawable.eat_icon, "Namecard", "Business cards are cards bearing business information about a company or individual."));

        adapter = new Adapter(models, this);
        btnGetStarted = findViewById(R.id.btn_get_started);
        tvSkip = findViewById(R.id.skip);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation);


        Integer[] colors_temp = {
                getResources().getColor(R.color.color1),
                getResources().getColor(R.color.color2),
                getResources().getColor(R.color.color3),
                getResources().getColor(R.color.color4)
        };

        colors = colors_temp;

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (position < (adapter.getCount() -1) && position < (colors.length - 1)) {
                    viewPager.setBackgroundColor(

                            (Integer) argbEvaluator.evaluate(
                                    positionOffset,
                                    colors[position],
                                    colors[position + 1]
                            )
                    );
                    btnGetStarted.setVisibility(View.INVISIBLE);
                    tvSkip.setVisibility(View.VISIBLE);

                }

                else if(position == (adapter.getCount() -1) ){
                    btnGetStarted.setVisibility(View.VISIBLE);
                    btnGetStarted.setAnimation(btnAnim);
                    tvSkip.setVisibility(View.INVISIBLE);

                }else{
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                }

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //open main activity

                Intent loginActivity = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(loginActivity);
                // also we need to save a boolean value to storage so next time when the user run the app
                // we could know that he is already checked the intro screen activity
                // i'm going to use shared preferences to that process
                savePrefsData();
                finish();



            }
        });

        // skip button click listener

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(models.size());
            }
        });
    }
    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        boolean isIntroActivityOpenedBefore = pref.getBoolean("isIntroOpnend",false);
        return  isIntroActivityOpenedBefore;
    }

    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpened",true);
        editor.apply();
    }

}
