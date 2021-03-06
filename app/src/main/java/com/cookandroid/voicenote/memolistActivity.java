package com.cookandroid.voicenote;

//STT
import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
//TTS
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class memolistActivity extends AppCompatActivity {
    private static int buttonOn;
    final int PERMISSION = 1;
    Intent intent;

    SQLiteHelper dbHelper;

    TextToSpeech tts;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;

    Intent i;
    SpeechRecognizer mRecognizer;

    //어플 실행 후 음성인식 자동 실행
    EditText autoSystem;

    //recyclerView에 들어갈 전역변수 List
    List<Memo> memoList;

    Button button;
    Button button3;
    Button helpbutton;
    Button buttonPos;
    Button buttonNeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //화면 세로고정
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.memolist);


        buttonOn=0;

        //음성인식
        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);


        //TTS 객체 생성, 초기화
        tts= new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!= TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });


        if ( Build.VERSION.SDK_INT >= 23 ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION); }

        dbHelper = new SQLiteHelper(memolistActivity.this);
        memoList = dbHelper.selectAll();

        //recyclerView와 recyclerAdapter 연결
        recyclerView= findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);

        //최신 글이 위로 오도록 정렬
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        recyclerAdapter= new RecyclerAdapter(memoList);
        recyclerView.setAdapter(recyclerAdapter);


        //작성하기 버튼
        button= findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                funcVoiceOut("메모 작성 화면으로 이동합니다");

                Intent intent = new Intent(memolistActivity.this, MainActivity.class);
                startActivityForResult(intent, 0);
                //작성하기 버튼 클릭시 음성인식되지 않도록
                ButtonOff();
            }
        });

        //로고버튼으로 음성인식 받기
        button3= findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecognizer.startListening(i);
                setBackground("#ff1f4f");
            }
        });
        autoStart();
    }

    private void autoStart(){
        if(buttonOn!=1) {
            //3.5초 후 자동 음성인식 실행
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setBackground("#ff1f4f");
                    button3.performClick();
                }
            }, 3500);
        }
    }

    private RecognitionListener listener = new RecognitionListener()
    {
        @Override
        public void onReadyForSpeech(Bundle params) {
            String msg="메모의 음성인식을 시작합니다.";
            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onBeginningOfSpeech() {}
        @Override
        public void onRmsChanged(float rmsdB) {}
        @Override
        public void onBufferReceived(byte[] buffer) {}
        @Override public void onEndOfSpeech() {}
        @Override public void onError(int error) {
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    setBackground("#93db58");
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    setBackground("#93db58");
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    setBackground("#93db58");
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    setBackground("#93db58");
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    setBackground("#93db58");
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "시간 초과";
                    setBackground("#93db58");
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    setBackground("#93db58");
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    setBackground("#93db58");
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "시간 초과";
                    setBackground("#93db58");
                    break;
                default:
                    message = "알 수 없는 오류임";
                    setBackground("#93db58");
                    break;
            }
            Toast.makeText(getApplicationContext(), "에러 발생: " +
                    message,Toast.LENGTH_SHORT).show();
            funcVoiceOut("에러 발생 "+message);
        }


        @Override
        public void onResults(Bundle results) {

            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            String resultStr = "";

            for(int i = 0; i < matches.size() ; i++){
                resultStr += matches.get(i);
            }
            if(resultStr.length()<1) return;
            resultStr = resultStr.replace(" ","");
            actionActivity(resultStr);

            //다른 화면 넘어가면 음성인식 실행 하지 않도록
            if (resultStr.indexOf("메모작성")>-1){
                setBackground("#93db58");
            }
            else if(resultStr.indexOf("취소")>-1){
                setBackground("#93db58");
            }
            else if(resultStr.indexOf("전체삭제")>-1){
                setBackground("#93db58");
            }
            else if(resultStr.indexOf("네")>-1){
                setBackground("#93db58");
            }
            else if(resultStr.indexOf("아니요")>-1){
                setBackground("#93db58");
            }
            else if(resultStr.indexOf("검색")>-1){
                setBackground("#93db58");
            }
            else if(resultStr.indexOf("도움말")>-1){
                setBackground("#93db58");
            }
            else autoStart();

        }
        @Override
        public void onPartialResults(Bundle partialResults) {}
        @Override
        public void onEvent(int eventType, Bundle params) {}

        public void actionActivity(String resultStr){
            if(resultStr.indexOf("메모작성")>-1){
                funcVoiceOut("메모 작성 화면으로 이동합니다");
                setBackground("#93db58");
                Intent intent = new Intent(memolistActivity.this, MainActivity.class);
                startActivityForResult(intent, 0);
                //메모작성 후 음성인식 반복 정지
                ButtonOff();
            }
            else if(resultStr.indexOf("전체삭제")>-1){
                setBackground("#93db58");
                dbHelper.deleteAll();
                funcVoiceOut("전체 삭제가 완료되었습니다");
                //이전 화면 닫기
                finish();
                //전체 삭제 반영된 화면 띄우기
                Intent intent = new Intent(getBaseContext(), memolistActivity.class);
                startActivity(intent);
            }
            else if(resultStr.indexOf("취소")>-1){
                setBackground("#93db58");
                funcVoiceOut("음성인식을 취소합니다");
            }
            else if(resultStr.indexOf("검색")>-1){
                setBackground("#93db58");
                funcVoiceOut("검색 화면으로 이동합니다");
                Intent intent = new Intent(memolistActivity.this, searchActivity.class);
                startActivityForResult(intent, 0);
                ButtonOff();
            }
            else if(resultStr.indexOf("도움말")>-1){
                setBackground("#93db58");
                Intent intent = new Intent(memolistActivity.this, List_helpActivity.class);
                startActivityForResult(intent, 1);
                ButtonOff();
                mOnPopupClick(recyclerView);

            }
            else if(resultStr.indexOf("네")>-1){
                setBackground("#93db58");
                funcVoiceOut("삭제되었습니다");
                buttonPos.performClick();
            }
            else if(resultStr.indexOf("아니요")>-1){
                setBackground("#93db58");
                funcVoiceOut("삭제가 취소되었습니다");
                buttonNeg.performClick();
            }

        }
    };


    public static String reverseString(String s){
        return (new StringBuffer(s)).reverse().toString();
    }

    //처음 시작할때나 다른 화면에서 취소, 삭제로 리스트로 이동했을 때에만 음성인식 실행
    public static void ButtonOff(){
        //메모리스트 화면으로 오면 0으로 초기화하여 자동음성인식 가능하게함
        buttonOn=1;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==200){
            //MainActivity에서 입력받은 메모데이터값 받아오기
            String strMain= data.getStringExtra("main");
            String strSub= data.getStringExtra("sub");

            //Gradle Scripts에(앱단위 그래들)에 recyclerView 추가
            Memo memo= new Memo(strMain, strSub,0);
            recyclerAdapter.addItem(memo);
            recyclerAdapter.notifyDataSetChanged();

            dbHelper.insertMemo(memo);

        }
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {
        private List<Memo> listdata;

        public RecyclerAdapter(List<Memo> listdata) {
            this.listdata = listdata;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
            return new ItemViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return listdata.size();
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int i) {
            //데이터 레이아웃에 어떻게 넣어줄지를 결정
            Memo memo = listdata.get(i);

            //remove기능위해 seq가져 오기
            itemViewHolder.maintext.setTag(memo.getSeq());
            itemViewHolder.maintext.setText(memo.getMaintext());
            itemViewHolder.subtext.setText(memo.getSubtext());
        }

        void addItem(Memo memo) {
            listdata.add(memo);
        }

        void removeItem(int position) {
            listdata.remove(position);
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            private TextView maintext;
            private TextView subtext;
            int click=0;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);


                maintext = itemView.findViewById(R.id.item_maintext);
                subtext = itemView.findViewById(R.id.item_subtext);

                itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //LongClick이랑 중복 안될때만 실행
                        if(click==0) {
                            //메모 수정창으로 이동하면 메모리스트음성인식 종료
                            ButtonOff();

                            funcVoiceOut("메모를 수정합니다");
                            int pos = getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                Intent intent = new Intent(getApplicationContext(), Detail.class);

                                intent.putExtra("maintext", maintext.getText());
                                intent.putExtra("subtext", subtext.getText());
                                intent.putExtra("no", (int)maintext.getTag());
                                //intent.putExtra("pos",getAdapterPosition());

                                startActivity(intent);
                            }
                        }
                    }
                });

                //길게 눌러서 삭제
                itemView.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View view) {
                        //LongClick이랑 one Click이랑 겹치지 않게 하기 위함
                        ButtonOff();
                        click=1;

                        funcVoiceOut("정말로 삭제하시겠습니까?");
                        buttonOn=0;
                        autoStart();

                        AlertDialog.Builder builder= new AlertDialog.Builder(memolistActivity.this);
                        builder.setMessage("정말로 삭제하시겠습니까?");
                        builder.setTitle(("삭제알림창"));
                        builder.setCancelable(false);


                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();

                                //다시 one Click가능하게 바꿔줌
                                click=0;
                            }
                        });

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int position=getAdapterPosition();
                                int seq= (int)maintext.getTag();

                                if(position!= RecyclerView.NO_POSITION) {
                                    dbHelper.deleteMemo(seq);
                                    removeItem(position);
                                    notifyDataSetChanged();
                                }
                                //다시 one Click가능하게 바꿔줌
                                click=0;
                            }
                        });
                        AlertDialog alert= builder.create();
                        alert.setTitle("삭제 알림창");
                        alert.show();

                        buttonPos= alert.getButton(DialogInterface.BUTTON_POSITIVE);
                        buttonNeg= alert.getButton(DialogInterface.BUTTON_NEGATIVE);

                        return false;
                    }
                });
            }

        }
    }

    public void mOnPopupClick(View v){
        Intent intent = new Intent(this, List_helpActivity.class);
        startActivityForResult(intent, 1);
        tts.speak("메모리스트 화면입니다\n"+
                        "화면 상단에는 검색, 음성명령 호출, 도움말 버튼이 있습니다. \n" +
                        "리스트 화면의 음성명령 키워드는 취소, 메모작성, 검색, 전체삭제 가 있습니다",
                TextToSpeech.QUEUE_FLUSH,null);
    }

    public void mOnSearchClick(View v){
        Intent intent = new Intent(this, searchActivity.class);
        startActivityForResult(intent, 1);
    }

    public void setBackground(String color){
        button.setBackgroundColor(Color.parseColor(color));
    }

    //음성 문자열 함수에 직접 받아서 음성출력
    public void funcVoiceOut(String OutMsg) {
        if (OutMsg.length() < 1) return;

        if(!tts.isSpeaking()){
            tts.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
