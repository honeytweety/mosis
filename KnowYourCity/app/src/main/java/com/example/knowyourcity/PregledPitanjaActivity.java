package com.example.knowyourcity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.TextValueSanitizer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PregledPitanjaActivity extends AppCompatActivity implements View.OnClickListener
{

    private int index;
    private MojObjekat objekat;
    private int numQuestions;
    private int progress;
    private List<Pitanje> questions; //shuffled copy
    private int points;
    private int tacanID; //1,2 ili 3
    private int tacnih;

    private TextView progressTV;
    private ProgressBar progressBar;
    private TextView pointsTV;
    private TextView textTV;
    private Chip odg1,odg2,odg3;
    private ChipGroup chipGroup;
    private ImageButton next;
    private ImageView image;

    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregled_pitanja);

        try
        {
            Intent listIntent = getIntent();
            Bundle positionBundle = listIntent.getExtras();

            if (positionBundle != null)
            {
                index = positionBundle.getInt("position", -1);
                if(index !=- 1)
                {
                    objekat = MojiPodaci.getInstance().getPlace(index);
                }
                else
                {
                    finish();
                    return;
                }
            }
        }
        catch (Exception e)
        {
            finish();
            return;
        }

        progress=0;
        points=0;
        tacnih=0;

        //ternarni op
        numQuestions = objekat.listaPitanja.size() > this.getResources().getInteger(R.integer.maxQuestions)
               ? this.getResources().getInteger(R.integer.maxQuestions)
               : objekat.listaPitanja.size();

        questions = new ArrayList(objekat.listaPitanja);
        Collections.shuffle(questions);

        image = (ImageView) findViewById(R.id.pregledPitanjaImage);
        String link   = objekat.objectPicID;
        Picasso.get().load(link).into(image);

        progressTV = (TextView) findViewById(R.id.pregledPitanjaProgressTV);
        progressBar = (ProgressBar) findViewById(R.id.pregledPitanjaProgressBar);
        progressBar.setMax(numQuestions);
        textTV = (TextView) findViewById(R.id.pregledPitanjaText);
        odg1 = (Chip) findViewById(R.id.pregledPitanjaOdg1Chip);
        odg2 = (Chip) findViewById(R.id.pregledPitanjaOdg2Chip);
        odg3 = (Chip) findViewById(R.id.pregledPitanjaOdg3Chip);

        chipGroup = (ChipGroup) findViewById(R.id.pregledPitanjaChipGroup);
        pointsTV= (TextView) findViewById(R.id.pregledPitanjaPoeniTV);
        next = (ImageButton) findViewById(R.id.pregledPitanjaNextIB);
        next.setOnClickListener(this::onClick);

        random=new Random();


        setQuestion();
    }

    private void setQuestion()
    {
        Pitanje pitanje = questions.get(progress);
        progressTV.setText(String.format("%d/%d", progress + 1, numQuestions));
        progressBar.setProgress(progress);
        pointsTV.setText(Integer.toString(points));
        textTV.setText(pitanje.getText());

        tacanID = random.nextInt(3) + 1;
        switch (tacanID)
        {
            case 1:
                odg1.setText(pitanje.getTacanOdg(),Chip.BufferType.NORMAL);
                odg2.setText(pitanje.getNetacanOdg(),Chip.BufferType.NORMAL);
                odg3.setText(pitanje.getNetacan2Odg(),Chip.BufferType.NORMAL);
                break;
            case 2:
                odg2.setText(pitanje.getTacanOdg(),Chip.BufferType.NORMAL);
                odg1.setText(pitanje.getNetacanOdg(),Chip.BufferType.NORMAL);
                odg3.setText(pitanje.getNetacan2Odg(),Chip.BufferType.NORMAL);
                break;
            case 3:
                odg3.setText(pitanje.getTacanOdg(),Chip.BufferType.NORMAL);
                odg2.setText(pitanje.getNetacanOdg(),Chip.BufferType.NORMAL);
                odg1.setText(pitanje.getNetacan2Odg(),Chip.BufferType.NORMAL);
                break;
        }
        chipGroup.clearCheck();

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.pregledPitanjaNextIB:
                if( chipGroup.getCheckedChipId()==View.NO_ID)
                    Toast.makeText(PregledPitanjaActivity.this,"Niste odgovorili na pitanje", Toast.LENGTH_LONG).show();
                else
                {
                    switch (tacanID)
                    {
                        case 1:
                            if(odg1.isChecked())
                            {
                                Toast.makeText(PregledPitanjaActivity.this, "Tacno", Toast.LENGTH_LONG).show();
                                points+=this.getResources().getInteger(R.integer.tacanOdgovor);
                                tacnih++;
                            }
                            else
                                Toast.makeText(PregledPitanjaActivity.this, "Netacno", Toast.LENGTH_LONG).show();
                            break;
                        case 2:
                            if(odg2.isChecked())
                            {
                                Toast.makeText(PregledPitanjaActivity.this, "Tacno", Toast.LENGTH_LONG).show();
                                points+=this.getResources().getInteger(R.integer.tacanOdgovor);
                                tacnih++;
                            }
                            else
                                Toast.makeText(PregledPitanjaActivity.this, "Netacno", Toast.LENGTH_LONG).show();
                            break;
                        case 3:
                            if(odg3.isChecked())
                            {
                                Toast.makeText(PregledPitanjaActivity.this, "Tacno", Toast.LENGTH_LONG).show();
                                points+=this.getResources().getInteger(R.integer.tacanOdgovor);
                                tacnih++;
                            }
                            else
                                Toast.makeText(PregledPitanjaActivity.this, "Netacno", Toast.LENGTH_LONG).show();
                            break;
                    }

                    progress++;
                    if(progress<numQuestions)
                        setQuestion();
                    else
                    {
                        MojiPodaci.getInstance().updatePoints(points);
                        Intent intent = new Intent(PregledPitanjaActivity.this,KrajKvizaActivity.class);
                        intent.putExtra("tacnih",tacnih);
                        intent.putExtra("netacnih",numQuestions-tacnih);
                        intent.putExtra("osvojeni",points);
                        startActivity(intent);
                    }
                }
                break;
        }
    }
}
