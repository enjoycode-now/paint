package cn.copaint.audience

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.copaint.audience.databinding.ActivityTransactionOrderDetialBinding
import cn.copaint.audience.databinding.ItemYuanbeiDetailBinding

class TransactionOrderDetialActivity : AppCompatActivity() {
    lateinit var binding: ActivityTransactionOrderDetialBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionOrderDetialBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}