//branch test
package com.cookandroid.voicenote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    EditText editText;
    //TextView et3;
    Button logobutton;
    Button mainbutton;
    int say = 0;

    Intent intent;

    private TextToSpeech tts;

    SpeechRecognizer mRecognizer;
    Intent i;

    final int PERMISSION = 1;

    long delay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //화면 세로고정
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( Build.VERSION.SDK_INT >= 23 ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION); }


        editText = (EditText) findViewById(R.id.editText);

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

        //로고버튼: 음성인식
        logobutton= findViewById(R.id.logobutton);
        logobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecognizer.startListening(i);
                setBackground("#ff1f4f");
            }
        });

        //화면 전환 후 자동 음성인식 실행
        autoStart();

        //메모 완료 후 저장버튼
        // 한번->음성출력
        // 두번->저장 후 뒤로가기
        // 기본 -> 초록 (#93db58), 음성인식 중 -> 빨강 (#ff1f4f), 음성재생 중 -> 파랑 (#56a8db)
        mainbutton= findViewById(R.id.mainbutton);
        mainbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //한번 클릭
                String str=editText.getText().toString();
                if( System.currentTimeMillis() > delay ) {
                    delay = System.currentTimeMillis() + 200;
                    speakOut();
                        return;

                }

                //더블 클릭
                if(System.currentTimeMillis() <= delay) {
                    //메모리스트로 돌아가서 음성인식되지 않도록
                    memolistActivity.ButtonOff();


                    if(str.length()>0) {
                        saveMemo();
                    }
                    else autoStart();
                }
                else {
                    mRecognizer.startListening(i);
                }
            }

            protected void onDestroy(View view){

            }
        });
    }
    private RecognitionListener listener = new RecognitionListener()
    {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(),"메인 음성인식을 시작합니다.",Toast.LENGTH_SHORT).show();
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
                editText.append(matches.get(i));
                resultStr += matches.get(i);
            }
            if (resultStr.length()<1) return;
            resultStr = resultStr.replace(" ","");
            actionActivity(resultStr);

            //"저장", "취소", "삭제"가 아닐 때만 반복
        if (resultStr.indexOf("네")>-1){
            memolistActivity.ButtonOff();
        }
         else if(resultStr.indexOf("취소")>-1){
        }
//        else if(resultStr.indexOf("삭제")>-1){
//        }
        else if(resultStr.indexOf("메모읽기")>-1){}
        else if(resultStr.indexOf("글로쓰기")>-1){
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
            if(resultStr.indexOf("다시쓰기")>-1){
                funcVoiceOut("메모를 처음부터 다시 작성합니다");
                editText.setText(null);
            }
            else if(resultStr.indexOf("절반지우기")>-1){
                String imsi = editText.getText().toString();
                imsi = reverseString(imsi);
                int n = imsi.length();
                imsi = imsi.substring(n/2+5);
                imsi = reverseString(imsi);
                editText.setText(imsi);
            }
            else if(resultStr.indexOf("한자리")>-1){
                String imsi = editText.getText().toString();
                imsi = reverseString(imsi);
                imsi = imsi.substring(5);
                imsi = reverseString(imsi);
                editText.setText(imsi);
            }
            else if(resultStr.indexOf("두자리")>-1){
                String imsi = editText.getText().toString();
                imsi = reverseString(imsi);
                imsi = imsi.substring(6);
                imsi = reverseString(imsi);
                editText.setText(imsi);
            }
            else if(resultStr.indexOf("세자리")>-1){
                String imsi = editText.getText().toString();
                imsi = reverseString(imsi);
                imsi = imsi.substring(7);
                imsi = reverseString(imsi);
                editText.setText(imsi);
            }
            else if(resultStr.indexOf("단어")>-1){
                String imsi = editText.getText().toString();
                imsi = reverseString(imsi);
                int idx = imsi.indexOf(" ");
                //제일뒷단어+띄어쓰기 지운 나머지 내용만 작성
                String imsi1 = imsi.substring(idx+1, imsi.length());
                imsi1 = reverseString(imsi1);
                editText.setText(imsi1);
            }
            else if(resultStr.indexOf("띄어쓰기")>-1){
                String str=editText.getText().toString();
                str = reverseString(str);
                str = str.substring(4);
                str = reverseString(str);
                editText.setText(str+" ");

            }
            else if(resultStr.indexOf("한줄띄우기")>-1){
                String str=editText.getText().toString();
                str = reverseString(str);
                str = str.substring(6);
                str = reverseString(str);
                editText.setText(str+"\n");
            }
            else if(resultStr.indexOf("한줄지우기")>-1){
                String imsi = editText.getText().toString();
                imsi = reverseString(imsi);
                int idx = imsi.indexOf("\n");
                //한줄+엔터 지운 나머지 내용만 작성
                String imsi1 = imsi.substring(idx+1, imsi.length());
                imsi1 = reverseString(imsi1);
                editText.setText(imsi1);
            }
            else if(resultStr.indexOf("메모읽기")>-1) {
                String str=editText.getText().toString();
                str = reverseString(str);
                str = str.substring(5);
                str = reverseString(str);
                editText.setText(str);

                speakOut();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        autoStart();
                    }
                }, 4000);
            }
            else if(resultStr.indexOf("삭제")>-1) {
                funcVoiceOut("정말 삭제하시겠습니까?");
                say = 1;
                String str=editText.getText().toString();
                str = reverseString(str);
                str = str.substring(2);
                str = reverseString(str);
                editText.setText(str);
            }
            else if(resultStr.indexOf("네")>-1) {
                if(say == 1){
                    Intent intent = new Intent(getApplicationContext(), memolistActivity.class);
                    startActivityForResult(intent, 101);
                    funcVoiceOut("메모가 삭제되었습니다");
                }
                else if(say == 2){
                    String str=editText.getText().toString();
                    str = reverseString(str);
                    str = str.substring(1);
                    str = reverseString(str);
                    editText.setText(str);

                    saveMemo();

                    funcVoiceOut("메모가 저장되었습니다");
                }
            }
            else if(resultStr.indexOf("아니요")>-1) {
                String str=editText.getText().toString();
                str = reverseString(str);
                str = str.substring(3);
                str = reverseString(str);
                editText.setText(str);

                if(say==1){
                    editText.append(" 삭제");
                }
                else if( say== 2){
                    editText.append(" 저장");
                }

            }
            else if(resultStr.indexOf("저장")>-1) {
                //"저장"글자까지 저장되지 않도록
                say = 2;
                String str=editText.getText().toString();
                str = reverseString(str);
                str = str.substring(2);
                str = reverseString(str);
                editText.setText(str);
                funcVoiceOut("정말 저장하시겠습니까?");
            }
            else if(resultStr.indexOf("취소")>-1) {
                funcVoiceOut("메모 작성이 취소되었습니다");
                editText.setText(null);//이동은 잘되는데 원래 텍스트+이동 을 자꾸 다시 읽어서 아예 null처리
                Intent intent = new Intent(getApplicationContext(), memolistActivity.class);
                startActivityForResult(intent, 101);
            }
            else if(resultStr.indexOf("글로쓰기")>-1) {
                String str=editText.getText().toString();
                str = reverseString(str);
                str = str.substring(5);
                str = reverseString(str);
                editText.setText(str);

                funcVoiceOut("음성인식을 취소합니다");
            }

            else if(resultStr.indexOf("검색")>-1){
                setBackground("#93db58");
                String imsi = editText.getText().toString();
                imsi = reverseString(imsi);
                imsi = imsi.substring(2);
                imsi = reverseString(imsi);
                editText.setText(imsi);

                funcVoiceOut("검색 화면으로 이동합니다");
                Intent intent = new Intent(MainActivity.this, searchActivity.class);
                startActivityForResult(intent, 0);
            }
            else if(resultStr.indexOf("도움말")>-1){
                setBackground("#93db58");
                String imsi = editText.getText().toString();
                imsi = reverseString(imsi);
                imsi = imsi.substring(3);
                imsi = reverseString(imsi);
                editText.setText(imsi);

                Intent intent = new Intent(MainActivity.this, Detail_helpActivity.class);
                startActivityForResult(intent, 1);
                tts.speak("메모 작성 화면입니다\n" +
                                "화면 상단에는 검색, 음성명령 호출, 도움말 버튼이 있습니다\n"+
                                "화면 하단버튼을 한번 클릭 시 메모 음성출력, 더블 클릭 시 메모 저장이 가능합니다\n"+
                                "음성으로 글로쓰기, 메모 읽기, 삭제, 저장, 취소 명령어를 실행할 수 있습니다. \n" +
                                "수정을 위한 음성 명령 키워드는 다시쓰기, 한 줄 지우기, 절반 지우기\n" +
                                "한 자리, 두 자리, 세 자리, 단어, 띄어쓰기, 한 줄 띄우기 가 있습니다",
                        TextToSpeech.QUEUE_FLUSH,null);
            }

        }

    };
    public static String reverseString(String s){
        return (new StringBuffer(s)).reverse().toString();
    }


    @Override
    public void onDestroy(){
        if(tts!=null){
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
    @Override
    public void onInit(int status) {
        if(status== TextToSpeech.SUCCESS){
            int result=tts.setLanguage(Locale.KOREA);

            if(result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS","This Language is not supported");
            }
        }
        else{
            Log.e("TTS","Initilization Failed!");
        }
    }

    private void speakOut(){
        setBackground("#56a8db");
        String text=editText.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    //자동 음성인식
    private void autoStart(){
        //2초 후 자동 음성인식 실행
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                logobutton.performClick();
                setBackground("#ff1f4f");
            }
        },3000);
    }


    //메모 저장
    private void saveMemo(){
        //입력받은 메모 리스트로 보내기
        String str=editText.getText().toString();

        //날짜
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String substr = sdf.format(date);

        //입력받은 메모 리스트로 보내기
        Intent intent1 = new Intent(getApplicationContext(), memolistActivity.class);
        intent1.putExtra("main", str);
        intent1.putExtra("sub", substr);
        setResult(200, intent1);

        speakOut();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                funcVoiceOut("저장이 완료되었습니다");
            }
        }, 3000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                finish();
            }
        }, 5000);
    }

    public void setBackground(String color){
        mainbutton.setBackgroundColor(Color.parseColor(color));
    }

    //음성 문자열 함수에 직접 받아서 음성출력
    public void funcVoiceOut(String OutMsg) {
        if (OutMsg.length() < 1) return;

        if(!tts.isSpeaking()){
            tts.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void mOnPopupClick(View v){
        Intent intent = new Intent(this, Detail_helpActivity.class);
        startActivityForResult(intent, 1);
        tts.speak("메모 작성 화면입니다\n" +
                        "화면 상단에는 검색, 음성명령 호출, 도움말 버튼이 있습니다\n"+
                        "화면 하단버튼을 한번 클릭 시 메모 음성출력, 더블 클릭 시 메모 저장이 가능합니다\n"+
                        "음성으로 글로쓰기, 메모 읽기, 삭제, 저장, 취소 명령어를 실행할 수 있습니다. \n" +
                        "수정을 위한 음성 명령 키워드는 다시쓰기, 한 줄 지우기, 절반 지우기\n" +
                        "한 자리, 두 자리, 세 자리, 단어, 띄어쓰기, 한 줄 띄우기 가 있습니다",
                TextToSpeech.QUEUE_FLUSH,null);
    }

    public void mOnSearchClick(View v){
        Intent intent = new Intent(this, searchActivity.class);
        startActivityForResult(intent, 1);
    }
}
