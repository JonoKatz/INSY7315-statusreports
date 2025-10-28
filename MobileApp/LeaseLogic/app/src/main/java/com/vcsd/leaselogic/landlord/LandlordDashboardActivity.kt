package com.vcsd.leaselogic.landlord

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.R
import com.vcsd.leaselogic.databinding.ActivityLandlordDashboardBinding
import java.text.SimpleDateFormat
import java.util.*

class LandlordDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandlordDashboardBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLandlordDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // ✅ Toolbar setup with Settings menu
        binding.toolbarDashboard.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_settings -> {
                    startActivity(Intent(this, LandlordSettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // ✅ Quick Action Buttons (move outside Firestore)
        binding.cardManageProperties.setOnClickListener {
            startActivity(Intent(this, ManagePropertiesActivity::class.java))
        }

        binding.cardManageTenants.setOnClickListener {
            startActivity(Intent(this, ManageTenantsActivity::class.java))
        }

        binding.cardMaintenance.setOnClickListener {
            startActivity(Intent(this, MaintenanceRequestsActivity::class.java))
        }

        binding.cardReports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        // ✅ Load Firebase Data
        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        binding.chartAvailability.animateY(1000)
        binding.chartIncomeTrend.animateY(1400)
    }

    // 🔹 Fetch dashboard stats from Firestore
    private fun loadDashboardData() {
        val landlordId = auth.currentUser?.uid ?: return

        db.collection("properties")
            .whereEqualTo("landlordId", landlordId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                var totalRent = 0.0
                var available = 0
                var rented = 0
                val monthlyIncome = mutableMapOf<String, Double>()

                for (doc in snapshot) {
                    val price = doc.getDouble("price") ?: 0.0
                    val isRented = doc.getBoolean("isRented") ?: false
                    val timestamp = doc.getTimestamp("dateCreated")?.toDate() ?: Date()

                    totalRent += price
                    if (isRented) rented++ else available++

                    val month = SimpleDateFormat("MMM", Locale.getDefault()).format(timestamp)
                    monthlyIncome[month] = (monthlyIncome[month] ?: 0.0) + price
                }

                binding.txtTotalProperties.text = snapshot.size().toString()
                binding.txtTotalRent.text = "R${"%.2f".format(totalRent)}"

                setupPieChart(binding.chartAvailability, available, rented)
                setupLineChart(binding.chartIncomeTrend, monthlyIncome)
            }
    }

    // 🔹 Availability chart
    private fun setupPieChart(chart: PieChart, available: Int, rented: Int) {
        val entries = listOf(
            PieEntry(available.toFloat(), "Available"),
            PieEntry(rented.toFloat(), "Rented")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#42A5F5"),
                Color.parseColor("#BBDEFB")
            )
            valueTextColor = Color.WHITE
            valueTextSize = 14f
            sliceSpace = 2f
        }

        chart.apply {
            data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            legend.isEnabled = true
            legend.textColor = Color.DKGRAY
            legend.textSize = 13f
            centerText = "Availability"
            setCenterTextColor(Color.parseColor("#1565C0"))
            setCenterTextSize(16f)
            holeRadius = 45f
            transparentCircleRadius = 50f
            animateY(1200, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
            invalidate()
        }
    }

    // 🔹 Monthly income chart
    private fun setupLineChart(chart: LineChart, monthlyIncome: Map<String, Double>) {
        if (monthlyIncome.isEmpty()) return

        val sortedMonths = monthlyIncome.keys.sortedBy {
            SimpleDateFormat("MMM", Locale.getDefault()).parse(it)?.let { date ->
                Calendar.getInstance().apply { time = date }.get(Calendar.MONTH)
            } ?: 0
        }

        val entries = sortedMonths.mapIndexed { index, month ->
            Entry(index.toFloat(), monthlyIncome[month]?.toFloat() ?: 0f)
        }

        val dataSet = LineDataSet(entries, "Monthly Income (R)").apply {
            color = Color.parseColor("#1E88E5")
            lineWidth = 3f
            setCircleColor(Color.parseColor("#1565C0"))
            circleRadius = 5f
            valueTextSize = 11f
            valueTextColor = Color.DKGRAY
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(this@LandlordDashboardActivity, R.drawable.chart_gradient)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        chart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            setScaleEnabled(false)
            animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutCubic)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.DKGRAY
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return sortedMonths.getOrNull(value.toInt()) ?: ""
                    }
                }
                setDrawGridLines(false)
            }

            axisLeft.apply {
                textColor = Color.DKGRAY
                setDrawGridLines(true)
                gridColor = Color.parseColor("#EEEEEE")
            }

            axisRight.isEnabled = false
            invalidate()
        }
    }
}
