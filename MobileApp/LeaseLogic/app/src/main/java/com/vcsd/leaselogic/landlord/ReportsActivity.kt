package com.vcsd.leaselogic.landlord

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.R
import com.vcsd.leaselogic.databinding.ActivityReportsBinding
import com.vcsd.leaselogic.models.MaintenanceRequest
import com.vcsd.leaselogic.models.Property
import java.text.SimpleDateFormat
import java.util.*

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var shimmer: ShimmerFrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        shimmer = findViewById(R.id.shimmerLayout)

        // 🔹 Toolbar
        setSupportActionBar(binding.toolbarReports)
        binding.toolbarReports.title = "Reports"
        binding.btnBack.setOnClickListener { finish() }

        // 🔹 Enter animation
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)

        // 🔹 Refresh button
        val rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh)
        binding.btnRefresh?.setOnClickListener {
            binding.btnRefresh?.isEnabled = false
            binding.btnRefresh?.startAnimation(rotateAnim)
            startShimmer()
            loadDashboardData()
        }

        startShimmer()
        loadDashboardData()
    }

    private fun startShimmer() {
        shimmer?.visibility = View.VISIBLE
        shimmer?.startShimmer()
        binding.progressBar.visibility = View.GONE
        binding.gridOverview.visibility = View.GONE
        binding.chartIncome.visibility = View.GONE
        binding.gridMaintenance.visibility = View.GONE
        binding.chartMaintenance.visibility = View.GONE
    }

    private fun stopShimmer() {
        shimmer?.stopShimmer()
        shimmer?.visibility = View.GONE

        binding.gridOverview.visibility = View.VISIBLE
        binding.chartIncome.visibility = View.VISIBLE
        binding.gridMaintenance.visibility = View.VISIBLE
        binding.chartMaintenance.visibility = View.VISIBLE
    }

    private fun loadDashboardData() {
        val landlordId = auth.currentUser?.uid ?: return

        var totalProperties = 0
        var rentedCount = 0
        var totalRent = 0.0
        val monthlyIncome = mutableMapOf<String, Double>()

        db.collection("properties")
            .whereEqualTo("landlordId", landlordId)
            .get()
            .addOnSuccessListener { propertySnapshot ->
                val properties = propertySnapshot.toObjects(Property::class.java)
                totalProperties = properties.size

                for (property in properties) {
                    if (property.isRented) rentedCount++
                    totalRent += property.price

                    val month = SimpleDateFormat("MMM", Locale.getDefault())
                        .format(Date(property.dateCreated))
                    monthlyIncome[month] = (monthlyIncome[month] ?: 0.0) + property.price
                }

                val vacancyRate = if (totalProperties > 0)
                    (totalProperties - rentedCount) * 100 / totalProperties
                else 0

                binding.apply {
                    cardTotalProperties.txtStatTitle.text = "Properties"
                    cardTotalRent.txtStatTitle.text = "Total Rent"
                    cardVacancy.txtStatTitle.text = "Vacancy"

                    cardTotalProperties.txtStatValue.text = totalProperties.toString()
                    cardTotalRent.txtStatValue.text = "R${"%.2f".format(totalRent)}"
                    cardVacancy.txtStatValue.text = "$vacancyRate%"
                }

                setupBarChart(binding.chartIncome, monthlyIncome)

                // 🔹 Maintenance data
                db.collection("maintenanceRequests")
                    .whereEqualTo("landlordId", landlordId)
                    .get()
                    .addOnSuccessListener { maintenanceSnapshot ->
                        val requests = maintenanceSnapshot.toObjects(MaintenanceRequest::class.java)
                        val pending = requests.count { it.status.equals("Pending", true) }
                        val inProgress = requests.count { it.status.equals("In Progress", true) }
                        val resolved = requests.count { it.status.equals("Resolved", true) }

                        setupPieChart(binding.chartMaintenance, pending, inProgress, resolved)

                        binding.apply {
                            cardPending.txtStatTitle.text = "Pending"
                            cardInProgress.txtStatTitle.text = "In Progress"
                            cardResolved.txtStatTitle.text = "Resolved"

                            cardPending.txtStatValue.text = pending.toString()
                            cardInProgress.txtStatValue.text = inProgress.toString()
                            cardResolved.txtStatValue.text = resolved.toString()

                            val sdf = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
                            txtLastUpdated?.text =
                                "Last Updated: ${sdf.format(Date(System.currentTimeMillis()))}"
                        }

                        // ✅ Stop shimmer and show animations
                        stopShimmer()
                        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_reports)
                        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_chart)
                        val rotateIn = AnimationUtils.loadAnimation(this, R.anim.rotate_in_chart)

                        binding.gridOverview.startAnimation(fadeIn)
                        binding.chartIncome.startAnimation(slideUp)
                        binding.gridMaintenance.startAnimation(fadeIn)
                        binding.chartMaintenance.startAnimation(rotateIn)

                        binding.btnRefresh?.isEnabled = true
                    }
            }
    }

    /** 📊 Bar Chart: Monthly Income */
    private fun setupBarChart(chart: BarChart, monthlyIncome: Map<String, Double>) {
        val sortedMonths = monthlyIncome.keys.sortedBy {
            SimpleDateFormat("MMM", Locale.getDefault()).parse(it)?.let { date ->
                Calendar.getInstance().apply { time = date }.get(Calendar.MONTH)
            } ?: 0
        }

        val entries = sortedMonths.mapIndexed { index, month ->
            BarEntry(index.toFloat(), monthlyIncome[month]?.toFloat() ?: 0f)
        }

        val dataSet = BarDataSet(entries, "Monthly Income (R)").apply {
            color = ContextCompat.getColor(this@ReportsActivity, R.color.blue_500)
            valueTextColor = ContextCompat.getColor(this@ReportsActivity, R.color.black)
            valueTextSize = 10f
        }

        chart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            animateY(1000)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = ContextCompat.getColor(this@ReportsActivity, R.color.black)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return sortedMonths.getOrNull(value.toInt()) ?: ""
                    }
                }
            }

            axisLeft.textColor = ContextCompat.getColor(this@ReportsActivity, R.color.gray)
            axisRight.isEnabled = false
            invalidate()
        }
    }

    /** 🍩 Pie Chart: Maintenance Requests */
    private fun setupPieChart(chart: PieChart, pending: Int, inProgress: Int, resolved: Int) {
        val entries = listOf(
            PieEntry(pending.toFloat(), "Pending"),
            PieEntry(inProgress.toFloat(), "In Progress"),
            PieEntry(resolved.toFloat(), "Resolved")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(this@ReportsActivity, R.color.red_accent),
                ContextCompat.getColor(this@ReportsActivity, R.color.blue_500),
                ContextCompat.getColor(this@ReportsActivity, R.color.green)
            )
            valueTextSize = 12f
            valueTextColor = ContextCompat.getColor(this@ReportsActivity, R.color.white)
        }

        chart.apply {
            data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            centerText = "Maintenance"
            setCenterTextColor(ContextCompat.getColor(this@ReportsActivity, R.color.black))
            setCenterTextSize(16f)
            animateY(1000)
            legend.isEnabled = true
            invalidate()
        }
    }
}
