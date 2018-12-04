package android.rnita.me.frontpage

import android.os.Bundle
import android.rnita.me.frontpage.databinding.MainActivityBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity)
        val adapter = MainAdapter()
        
    }
}
