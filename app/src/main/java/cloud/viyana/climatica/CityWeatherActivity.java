package cloud.viyana.climatica;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class CityWeatherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_weather);

        final EditText mEditText = findViewById(R.id.city_name_txt);
        ImageButton mImageButton = findViewById(R.id.back_button);

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String newCity = mEditText.getText().toString();
                Intent newCityIntent = new Intent(CityWeatherActivity.this, ClimateActivity.class);
                newCityIntent.putExtra("city", newCity);
                startActivity(newCityIntent);
                return false;
            }
        });


    }
}
