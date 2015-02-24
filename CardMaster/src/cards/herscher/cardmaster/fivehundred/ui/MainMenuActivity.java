package cards.herscher.cardmaster.fivehundred.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cards.herscher.cardmaster.R;
import cards.herscher.cardmaster.fivehundred.comm.HandshakeMessage;
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
        
        
        
        Kryo kryo = new Kryo();
        kryo.register(HandshakeMessage.class);
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Output output = new Output(byteStream);
        HandshakeMessage outMsg = new HandshakeMessage();
        kryo.writeClassAndObject(output, outMsg);
        output.close();
        byte[] outBytes = byteStream.toByteArray();

        Input input = new Input(new ByteArrayInputStream(outBytes));
        Object someObject = kryo.readClassAndObject(input);
        HandshakeMessage inMsg = (HandshakeMessage) someObject;
        input.close();
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
