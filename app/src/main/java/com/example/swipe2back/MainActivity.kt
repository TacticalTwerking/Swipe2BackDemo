package com.example.swipe2back

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.swipe2back.widgets.AdaptiveSpacingItemDecoration


class MainActivity : AppCompatActivity() {

    lateinit var rv:RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv = findViewById(R.id.rv)

        rv.adapter = ListAdapter{position,v->

            val list = arrayOf<Pair<View,String>>(Pair(v.findViewById(R.id.root),"root"))
            val activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,*list)
            val intent = Intent(
                this,
                DetailActivity::class.java
            )
            startActivity (intent, activityOptionsCompat.toBundle())
        }
        rv.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        rv.addItemDecoration(AdaptiveSpacingItemDecoration(30,true))
        

    }

}