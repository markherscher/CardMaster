package cards.herscher.commtester;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import cards.herscher.comm.TcpFrameDestination;
import cards.herscher.comm.TcpFrameSource;
import cards.herscher.comm.message.KryoMessageSerializer;
import cards.herscher.comm.message.Message;
import cards.herscher.comm.message.MessageConnection;
import cards.herscher.comm.message.ResponseMessage;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener
{
    private final static int SERVER_PORT_A = 5555;
    private final static int SERVER_PORT_B = 5556;
    private final static int STRESS_TEST_COUNT = 10;

    private ConnectionTester connectionTesterA;
    private ConnectionTester connectionTesterB;
    private ServerSocket serverSocket;
    private Button initButton;
    private Button sendAButton;
    private Button sendBButton;
    private Button stressButton;
    private TextView output;
    private Handler handler;
    private Random random;
    private ProgressDialog progress;
    private boolean isError;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        initButton = (Button) findViewById(R.id.initButton);
        sendAButton = (Button) findViewById(R.id.sendA);
        sendBButton = (Button) findViewById(R.id.sendB);
        stressButton = (Button) findViewById(R.id.stress);
        output = (TextView) findViewById(R.id.output);
        handler = new Handler();
        random = new Random();

        initButton.setOnClickListener(this);
        sendAButton.setOnClickListener(this);
        sendBButton.setOnClickListener(this);
        stressButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if (v == initButton)
        {
            isError = false;
            progress = ProgressDialog.show(this, "Init", "Initializing...");
            Thread threadB = new Thread(new SetupConnectionTesterB());
            Thread threadA = new Thread(new SetupConnectionTesterA(threadB));

            threadB.start();
            threadA.start();
        }
        else if (v == sendAButton)
        {
            connectionTesterA.sendRandomMessage();
        }
        else if (v == sendBButton)
        {
            connectionTesterB.sendRandomMessage();
        }
        else if (v == stressButton)
        {
            output.setText("");

            for (int i = 0; i < STRESS_TEST_COUNT; i++)
            {
                connectionTesterA.sendRandomMessage();
                connectionTesterB.sendRandomMessage();
            }
        }
    }

    private void cleanup()
    {
        if (connectionTesterA != null)
        {
            connectionTesterA.cleanup();
        }

        if (connectionTesterB != null)
        {
            connectionTesterB.cleanup();
        }

        if (serverSocket != null)
        {
            try
            {
                serverSocket.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    private void handleFatalError(final String text)
    {
        isError = true;

        handler.post(new Runnable()
        {
            public void run()
            {
                if (progress != null)
                {
                    progress.dismiss();
                }

                initButton.setEnabled(true);
                sendAButton.setEnabled(false);
                sendBButton.setEnabled(false);
                stressButton.setEnabled(false);
                output.append(text);
                cleanup();
            }
        });
    }

    private class ConnectionTester implements MessageConnection.Listener
    {
        private final MessageConnection messageConnection;
        private final String name;
        private final Socket clientSocket;

        public ConnectionTester(Socket clientSocket, String name) throws IOException
        {
            this.name = name;
            this.clientSocket = clientSocket;

            TcpFrameSource frameSource = new TcpFrameSource(clientSocket);
            TcpFrameDestination frameDest = new TcpFrameDestination(clientSocket);
            KryoMessageSerializer serializer = new KryoMessageSerializer();
            
            serializer.registerMessageClass(MathQuestionMessage.class);
            serializer.registerMessageClass(MathAnswerMessage.class);

            frameSource.init();
            frameDest.init();

            messageConnection = new MessageConnection(frameSource, frameDest, serializer, handler,
                    this);
            messageConnection.setName(name);
            messageConnection.open();
        }

        public void sendRandomMessage()
        {
            messageConnection.sendMessage(
                    new MathQuestionMessage(random.nextInt(1000), random.nextInt(1000)), true);
        }

        public void cleanup()
        {
            messageConnection.close();

            try
            {
                clientSocket.close();
            }
            catch (Exception e)
            {
            }
        }

        @Override
        public ResponseMessage onMessageReceived(Message message)
        {
            if (message instanceof MathQuestionMessage)
            {
                MathQuestionMessage mathMessage = (MathQuestionMessage) message;
                int answer = mathMessage.getNumber1() + mathMessage.getNumber2();
                MathAnswerMessage answerMessage = new MathAnswerMessage(answer);
                return new ResponseMessage(mathMessage.getId(), answerMessage);
            }
            else
            {
                return null;
            }
        }

        @Override
        public void onMessageResponseReceived(Message originalMessage,
                ResponseMessage responseMessage)
        {
            if (originalMessage instanceof MathQuestionMessage)
            {
                Message actualMessage = responseMessage.getActualMessage();

                if (actualMessage instanceof MathAnswerMessage)
                {
                    final int answer = ((MathAnswerMessage) actualMessage).getAnswer();
                    final int expectedAnswer = ((MathQuestionMessage) originalMessage).getNumber1()
                            + ((MathQuestionMessage) originalMessage).getNumber2();

                    if (answer == expectedAnswer)
                    {
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                output.append(String.format("%s: RX response of %d == %d\n", name,
                                        answer, expectedAnswer));
                            }
                        });
                    }
                    else
                    {
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                output.append(String.format("%s: FAILED! %d != %d\n", name, answer,
                                        expectedAnswer));
                            }
                        });
                    }
                }
            }
        }

        @Override
        public void onNoMessageResponse(Message message)
        {
            handleFatalError(String.format("%s: No response for message %s\n", name,
                    message.toString()));
        }

        @Override
        public void onReceiveError(IOException e)
        {
            handleFatalError(String.format("%s: %s\n", name, e.getMessage()));
        }

        @Override
        public void onSendError(IOException e)
        {
            handleFatalError(String.format("%s: %s\n", name, e.getMessage()));
        }
    }

    private class SetupConnectionTesterA implements Runnable
    {
        private final Thread otherThread;

        public SetupConnectionTesterA(Thread otherThread)
        {
            this.otherThread = otherThread;
        }

        @Override
        public void run()
        {
            try
            {
                Socket clientSocket = new Socket("localhost", SERVER_PORT_B);
                clientSocket.setTcpNoDelay(true);
                connectionTesterA = new ConnectionTester(clientSocket, "ConnA");

                try
                {
                    otherThread.join();
                }
                catch (InterruptedException e)
                {
                }

                if (isError)
                {
                    return;
                }

                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        initButton.setEnabled(false);
                        sendAButton.setEnabled(true);
                        sendBButton.setEnabled(true);
                        stressButton.setEnabled(true);
                        output.setText("");

                        if (progress != null)
                        {
                            progress.dismiss();
                        }
                    }
                });
            }
            catch (IOException e)
            {
                handleFatalError(String.format("Setup A failed: %s\n", e.getMessage()));
                cleanup();
                return;
            }
        }
    }

    private class SetupConnectionTesterB implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(SERVER_PORT_B));

                Socket clientSocket = serverSocket.accept();
                clientSocket.setTcpNoDelay(true);
                connectionTesterB = new ConnectionTester(clientSocket, "ConnB");

            }
            catch (IOException e)
            {
                handleFatalError(String.format("Setup B failed: %s\n", e.getMessage()));
                cleanup();
                return;
            }
        }
    }
}
