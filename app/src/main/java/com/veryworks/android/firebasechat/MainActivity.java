package com.veryworks.android.firebasechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText editId,editPw,editUpid,editUpname,editUppw;
    Button btnSignin, btnSignup;
    LinearLayout layoutSignup;

    FirebaseDatabase database;
    DatabaseReference userRef;

    // 사용자 추가후 확인을 위해 사용
    private String upid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("user");

        editId = (EditText) findViewById(R.id.editId);
        editPw = (EditText) findViewById(R.id.editPw);

        layoutSignup = (LinearLayout) findViewById(R.id.layoutSignup);
        editUpid = (EditText) findViewById(R.id.editUpid);
        editUppw = (EditText) findViewById(R.id.editUppw);
        editUpname = (EditText) findViewById(R.id.editUpname);

        // 로그인
        btnSignin = (Button) findViewById(R.id.btnSignin);
        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String id = editId.getText().toString();
                final String pw = editPw.getText().toString();

                // DB 1. 파이어베이스로 child(id) 레퍼런스에 대한 쿼리를 날린다.
                userRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {

                    // DB 2.파이어베이스는 데이터쿼리가 완료되면 스냅샷에 담아서 onDataChange 를 호출해준다
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount() > 0){
                            String fbPw = dataSnapshot.child("password").getValue().toString();
                            String name = dataSnapshot.child("name").getValue().toString();

                            if(fbPw.equals(pw)){
                                Intent intent = new Intent(MainActivity.this, RoomListActivity.class);
                                intent.putExtra("userid",id);
                                intent.putExtra("username",name);
                                startActivity(intent);
                            }else{
                                Toast.makeText(MainActivity.this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(MainActivity.this, "User 가 없습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        // 사용자 추가
        btnSignup = (Button) findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(layoutSignup.getVisibility() == View.GONE){
                    layoutSignup.setVisibility(View.VISIBLE);
                }else {
                    upid = editUpid.getText().toString();
                    final String upname = editUpname.getText().toString();
                    final String uppw = editUppw.getText().toString();

                    // 값이 모두 입력되었으면
                    if (!"".equals(upid) && !"".equals(upname) && !"".equals(uppw)) {

                        // 1. id 가 있는지 체크
                        userRef.child(upid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getChildrenCount() > 0) {
                                    Toast.makeText(getBaseContext()
                                            , "아이디가 이미 있습니다"
                                            , Toast.LENGTH_SHORT).show();
                                } else {
                                    // 2. 사용자 정보생성
                                    Map<String, String> userInfo = new HashMap<>();
                                    userInfo.put("name", upname);
                                    userInfo.put("password", uppw);

                                    // 3. 사용자 키에 사용자정보 추가
                                    Map<String, Object> keyInfo = new HashMap<>();
                                    keyInfo.put(upid, userInfo);

                                    userRef.updateChildren(keyInfo);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getBaseContext()
                                        , "Error:" + databaseError.getMessage()
                                        , Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });

        // 사용자 추가후 결과처리
        userRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String addedId = dataSnapshot.getKey();

                if(addedId.equals(upid)){
                    Toast.makeText(getBaseContext()
                            , "등록되었습니다"
                            , Toast.LENGTH_SHORT).show();

                    upid = "";
                    // 사용자 등록후 등록버튼 잠금
                    layoutSignup.setVisibility(View.GONE);
                    btnSignup.setEnabled(false);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
