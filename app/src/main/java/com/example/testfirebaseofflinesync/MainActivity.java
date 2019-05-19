package com.example.testfirebaseofflinesync;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private List<News> listNews = new ArrayList<News>();
    private ListAdapter listAdapter;
    private FirebaseDatabase database;

    private TextView textViewConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewConnected = (TextView) findViewById(R.id.textViewConnected);



        ListView listView = (ListView) findViewById(R.id.listViewNews);
        listAdapter = new ListAdapter(this, R.layout.item_list_view_news, listNews);
        listView.setAdapter(listAdapter);

        database=FirebaseDatabase.getInstance();
        if(!FirebaseApp.getApps(this).isEmpty()) {
            database.setPersistenceEnabled(true);
        }
        database.setPersistenceEnabled(true);
        DatabaseReference myFirebaseRef= database.getReference();
        myFirebaseRef.keepSynced(true);

        handleInternetConnection();
        handleNews(myFirebaseRef);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                News news=new News();
                news.setTitle(new Date().getTime()+"");
                news.setContent(new Date().getTime()+"");
                DatabaseReference databaseReference= database.getReference();
                databaseReference.child("news").child(news.getTitle()).setValue(news);
            }
        });
    }

    public void handleNews(DatabaseReference myFirebaseRef) {
        myFirebaseRef.child("news").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
                String key = dataSnapshot.getKey();
                Map<String, String> value = (Map<String, String>) dataSnapshot.getValue();

                News news = new News();
                news.setKey(key);
                news.setTitle(value.get("title"));
                news.setContent(value.get("content"));
                listNews.add(news);

                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                Map<String, String> value = (Map<String, String>) dataSnapshot.getValue();

                for(News n: listNews) {
                    if(n.getKey().equalsIgnoreCase(key)) {
                        n.setTitle(value.get("title"));
                        n.setContent(value.get("content"));
                    }
                }

                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Map<String, String> value = (Map<String, String>) dataSnapshot.getValue();

                for(int i=0;i<listNews.size();i++) {
                    if(listNews.get(i).getKey().equalsIgnoreCase(key)) {
                        listNews.remove(i);
                    }
                }

                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });
    }

    public void handleInternetConnection() {
        DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d("123123","connected");
                    textViewConnected.setText("Connected : " + connected);
                } else {
                   Log.d("123123","not connected");
                    textViewConnected.setText("Connected : " + connected);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });
    }

    public static class News {
        private String key;
        private String title;
        private String content;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    class ListAdapter extends ArrayAdapter<News> {

        public ListAdapter(Context context, int resource, List<News> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.item_list_view_news, null);
            }

            News p = getItem(position);

            if (p != null) {
                TextView label = (TextView) v.findViewById(R.id.myListItemLabel);
                TextView value = (TextView) v.findViewById(R.id.myListItemValue);

                if (label != null) {
                    label.setText(String.valueOf(p.getTitle()));
                }

                if (value != null) {
                    value.setText(String.valueOf(p.getContent()));
                }

            }

            return v;
        }

    }
}
