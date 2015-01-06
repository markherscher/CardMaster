package cards.herscher.cardmaster.fivehundred.ui;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import cards.herscher.cardmaster.R;
import cards.herscher.cardmaster.fivehundred.comm.JoinGameMessage;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends Activity implements View.OnClickListener
{
    private Button host;
    private Button join;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fivehundred_main_menu_activity);

        host = (Button) findViewById(R.id.host);
        join = (Button) findViewById(R.id.join);

        host.setOnClickListener(this);
        join.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if (v == host)
        {
            Intent intent = new Intent(this, HostGameActivity.class);
            startActivity(intent);
        }
        else if (v == join)
        {
            Intent intent = new Intent(this, JoinGameActivity.class);
            startActivity(intent);
        }
    }
}
